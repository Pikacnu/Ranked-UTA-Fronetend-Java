package com.pikacnu.src.websocket;

import java.net.URI;
import java.net.http.*;
import java.util.ArrayList;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import com.google.gson.JsonSyntaxException;
import com.pikacnu.src.json.data.Message;
import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.Status;
import com.pikacnu.src.json.data.Payload;
import com.pikacnu.src.websocket.handler.*;
import com.pikacnu.Config;
import com.pikacnu.UTA2;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

public class WebSocketClient {
  public static java.net.http.WebSocket webSocketClient;
  public static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private static final AtomicBoolean isConnecting = new AtomicBoolean(false);
  private static final AtomicBoolean shouldReconnect = new AtomicBoolean(true);

  private static final int RECONNECT_DELAY_SECONDS = 5;
  public static String serverSessionId;
  public static MinecraftServer minecraftServer;

  // Handler 管理
  private static final ArrayList<BaseHandler> handlers = new ArrayList<>();

  public static class Task {
    public String action;
    public Identifier function;

    public Task(String action, Identifier function) {
      this.action = action;
      this.function = function;
    }
  }

  public static ArrayList<Task> taskQueue = new ArrayList<>();

  public static void init(MinecraftServer server) {
    if (isConnecting.get()) {
      UTA2.LOGGER.info("WebSocket connection already in progress...");
      return;
    }

    minecraftServer = server;
    shouldReconnect.set(true);

    // 初始化所有 handlers
    initializeHandlers();

    serverSessionId = Config.serverId;

    connectWebSocket();
  }

  private static void initializeHandlers() {
    handlers.clear();
    handlers.add(new HandshakeHandler(minecraftServer));
    handlers.add(new HeartbeatHandler(minecraftServer));
    handlers.add(new CommandHandler(minecraftServer));
    handlers.add(new TeamJoinHandler(minecraftServer));
    handlers.add(new TransferHandler(minecraftServer));
    handlers.add(new WhitelistHandler(minecraftServer));
    handlers.add(new PlayerDataHandler(minecraftServer));
    handlers.add(new QueueMatchHandler(minecraftServer));
    handlers.add(new RequestDataHandler(minecraftServer));
    handlers.add(new ErrorHandler(minecraftServer));
    handlers.add(new DisconnectHandler(minecraftServer));
    handlers.add(new PlayerOnlineStatusHandler(minecraftServer));
    // 可以根據需要添加更多 handlers
  }

  private static void connectWebSocket() {
    // Prevent multiple concurrent connections
    if (isConnecting.get()) {
      UTA2.LOGGER.info("WebSocket connection already in progress, skipping...");
      return;
    }

    isConnecting.set(true);

    HttpClient client = HttpClient.newHttpClient();
    String url = "ws://" + Config.host + ":" + Config.port + Config.path;

    UTA2.LOGGER.info("Connecting to WebSocket: " + url);

    client.newWebSocketBuilder()
        .buildAsync(URI.create(url), new java.net.http.WebSocket.Listener() {
          @Override
          public void onOpen(java.net.http.WebSocket webSocket) {
            UTA2.LOGGER.info("WebSocket connected successfully");

            // Close any existing connection before setting the new one
            if (WebSocketClient.webSocketClient != null && !WebSocketClient.webSocketClient.isOutputClosed()) {
              WebSocketClient.webSocketClient.sendClose(java.net.http.WebSocket.NORMAL_CLOSURE,
                  "Replacing with new connection");
            }

            WebSocketClient.webSocketClient = webSocket;
            isConnecting.set(false);
            webSocket.request(1);
          }

          @Override
          public CompletionStage<?> onText(java.net.http.WebSocket webSocket, CharSequence data, boolean last) {
            String messageText = data.toString();
            // Parse JSON message
            parseAndHandleMessage(messageText);
            webSocket.request(1); // Request next message
            return null;
          }

          @Override
          public void onError(java.net.http.WebSocket webSocket, Throwable error) {
            UTA2.LOGGER.error("WebSocket error: " + error.getMessage());
            isConnecting.set(false);
            shouldReconnect.set(true);
            scheduleReconnect();
          }

          @Override
          public CompletionStage<?> onClose(java.net.http.WebSocket webSocket, int statusCode, String reason) {
            UTA2.LOGGER.info("WebSocket closed: " + statusCode + " - " + reason);
            WebSocketClient.webSocketClient = null;
            isConnecting.set(false);

            if (shouldReconnect.get()) {
              scheduleReconnect();
            }
            return null;
          }
        })
        .whenComplete((ws, ex) -> {
          if (ex != null) {
            UTA2.LOGGER.error("Failed to connect WebSocket: " + ex.getMessage());
            isConnecting.set(false);
            scheduleReconnect();
          }
        });
  }

  private static void scheduleReconnect() {
    if (!shouldReconnect.get())
      return;

    UTA2.LOGGER.info("Scheduling WebSocket reconnect in " + RECONNECT_DELAY_SECONDS + " seconds...");
    scheduler.schedule(() -> {
      if (shouldReconnect.get() && !isConnected()) {
        UTA2.LOGGER.info("Attempting to reconnect WebSocket...");
        connectWebSocket();
      }
    }, RECONNECT_DELAY_SECONDS, TimeUnit.SECONDS);
  }

  private static void parseAndHandleMessage(String messageText) {
    try {
      if (messageText == null || messageText.isEmpty()) {
        UTA2.LOGGER.error("Received empty or null message");
        return;
      }

      Message message = Message.fromJson(messageText);
      if (message == null || message.action == null) {
        UTA2.LOGGER.error("Failed to parse message: " + messageText);
        return;
      }

      Action action = message.action;
      Status status = message.status;
      String sessionId = message.sessionId;
      Payload payload = message.payload;

      if (action != Action.HEARTBEAT) {
        UTA2.LOGGER.info("Parsed message - Action: " + action + ", Status: " + status);
      }

      if (status == Status.ERROR && payload != null) {
        UTA2.LOGGER.error("Error status received: " + payload.message + ". In Action: " + action);
        return; // Stop processing if error status
      } else if (status == Status.ERROR) {
        UTA2.LOGGER.error("Error status received with no payload. Action: " + action);
        return; // Stop processing if error status without payload
      }

      // Handle different message types using handlers
      handleIncomingMessage(action, status, sessionId, payload);

    } catch (JsonSyntaxException e) {
      UTA2.LOGGER.error("Failed to parse JSON message: " + messageText + " - Error: " + e.getMessage());
    }
  }

  private static void handleIncomingMessage(Action action, Status status, String sessionId, Payload payload) {
    // 找到對應的 handler 來處理訊息
    BaseHandler handler = handlers.stream()
        .filter(h -> h.canHandle(action))
        .findFirst()
        .orElse(null);

    if (handler != null) {
      handler.handle(action, status, sessionId, payload);
    } else {
      UTA2.LOGGER.info("No handler found for action: " + action);
    }
  }

  public static void sendMessage(Message message) {
    if (message == null) {
      UTA2.LOGGER.error("Cannot send null message");
      return;
    }
    sendMessage(message.toJson());
  }

  public static void sendMessage(String message) {
    if (webSocketClient != null && !webSocketClient.isOutputClosed()) {
      webSocketClient.sendText(message, true)
          .whenComplete((result, ex) -> {
            if (ex != null) {
              UTA2.LOGGER.error("Failed to send message: " + ex.getMessage());
            }
          });
    } else {
      UTA2.LOGGER.warn("WebSocket is not connected, cannot send message: " + message);
      // Only attempt reconnect if not already connecting
      if (!isConnecting.get()) {
        UTA2.LOGGER.info("Attempting to reconnect WebSocket before sending message...");
        connectWebSocket();
      }
    }
  }

  public static boolean isConnected() {
    return webSocketClient != null && !webSocketClient.isOutputClosed();
  }

  public static void disconnect() {
    shouldReconnect.set(false);
    if (webSocketClient != null) {
      webSocketClient.sendClose(java.net.http.WebSocket.NORMAL_CLOSURE, "Disconnect By Client");
    }
  }

  public static void shutdown() {
    disconnect();
    scheduler.shutdown();
    try {
      if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
        scheduler.shutdownNow();
      }
    } catch (InterruptedException e) {
      scheduler.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  public static void addTask(String action, Identifier function) {
    taskQueue.add(new Task(action, function));
    UTA2.LOGGER.info("Task added: " + action + " with function " + function);
  }
}
