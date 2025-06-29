package com.pikacnu.src;

import java.net.URI;
import java.net.http.*;
import java.util.ArrayList;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.ParseResults;
import com.pikacnu.src.json.Payload.teamData;
import com.pikacnu.src.PlayerDatabase.PlayerData;
import com.pikacnu.src.json.*;
import com.pikacnu.UTA2;

import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

public class WebSocket {

  public static java.net.http.WebSocket webSocketClient;
  public static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private static final AtomicBoolean isConnecting = new AtomicBoolean(false);
  private static final AtomicBoolean shouldReconnect = new AtomicBoolean(true);

  public static Integer port = 8080;
  public static String host = "localhost";
  public static String path = "/ws";
  private static final int RECONNECT_DELAY_SECONDS = 5;
  public static String serverSessionId;
  public static MinecraftServer minecraftServer;

  public static class task {
    public String action;
    public Identifier function;

    public task(String action, Identifier function) {
      this.action = action;
      this.function = function;
    }
  }

  public static ArrayList<task> taskQueue;

  public static void init(MinecraftServer server) {
    if (isConnecting.get()) {
      UTA2.LOGGER.info("WebSocket connection already in progress...");
      return;
    }

    minecraftServer = server;
    shouldReconnect.set(true);
    connectWebSocket();
  }

  private static void connectWebSocket() {
    // Prevent multiple concurrent connections
    if (isConnecting.get()) {
      UTA2.LOGGER.info("WebSocket connection already in progress, skipping...");
      return;
    }

    isConnecting.set(true);

    HttpClient client = HttpClient.newHttpClient();
    String url = "ws://" + host + ":" + port + path;

    UTA2.LOGGER.info("Connecting to WebSocket: " + url);

    client.newWebSocketBuilder()
        .buildAsync(URI.create(url), new java.net.http.WebSocket.Listener() {
          @Override
          public void onOpen(java.net.http.WebSocket webSocket) {
            UTA2.LOGGER.info("WebSocket connected successfully");

            // Close any existing connection before setting the new one
            if (WebSocket.webSocketClient != null && !WebSocket.webSocketClient.isOutputClosed()) {
              WebSocket.webSocketClient.sendClose(java.net.http.WebSocket.NORMAL_CLOSURE,
                  "Replacing with new connection");
            }

            WebSocket.webSocketClient = webSocket;
            isConnecting.set(false);
            webSocket.request(1);

          }

          @Override
          public CompletionStage<?> onText(java.net.http.WebSocket webSocket, CharSequence data, boolean last) {
            String messageText = data.toString();
            // UTA2.LOGGER.info("Received WebSocket message: " + messageText);

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
            WebSocket.webSocketClient = null;
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
      Message message = Message.fromJson(messageText);

      Action action = message.action;
      Status status = message.status;
      String sessionId = message.sessionId;
      Payload payload = message.payload;

      if (action != Action.HEARTBEAT) {
        UTA2.LOGGER.info("Parsed message - Action: " + action + ", Status: " + status);
      }

      if (status == Status.ERROR && payload != null) {
        UTA2.LOGGER.error("Error status received: " + payload != null ? payload.message
            : "No message in payload" + ". In Action: " + action);
        return; // Stop processing if error status
      } else if (status == Status.ERROR) {
        UTA2.LOGGER.error("Error status received with no payload. Action: " + action);
        return; // Stop processing if error status without payload
      }

      // Handle different message types
      handleIncomingMessage(action, status, sessionId, payload);

    } catch (JsonSyntaxException e) {
      UTA2.LOGGER.error("Failed to parse JSON message: " + messageText + " - Error: " + e.getMessage());
    }
  }

  private static void handleIncomingMessage(Action action, Status status, String sessionId, Payload payload) {
    switch (action) {
      case Action.HANDSHAKE:
        if (!(sessionId == null || sessionId.isEmpty())) {
          serverSessionId = sessionId;
          UTA2.LOGGER.info("Handshake received, server ID: " + sessionId);
        } else {
          UTA2.LOGGER.error("Handshake message missing serverId");
        }
        break;
      case Action.HEARTBEAT:
        Message heartbeatMessage = new Message(Action.HEARTBEAT, serverSessionId);
        sendMessage(heartbeatMessage);
        break;
      case Action.DISCONNECT:
        UTA2.LOGGER.info("Received disconnect message");
        // Only reconnect if we're not already connecting
        if (!isConnecting.get()) {
          disconnect();
          connectWebSocket();
        }
        break;
      case Action.COMMAND:
        String command = payload != null ? payload.command : "";
        if (command.isEmpty()) {
          UTA2.LOGGER.error("Received command message with empty command");
          return;
        }
        UTA2.LOGGER.info("Received command: " + command);
        if (minecraftServer == null) {
          UTA2.LOGGER.error("Minecraft server instance is null, cannot execute command.");
          return;
        }
        minecraftServer.execute(() -> {
          try {
            ParseResults<ServerCommandSource> parsedResult = minecraftServer.getCommandManager().getDispatcher()
                .parse(command, minecraftServer.getCommandSource().withLevel(2));
            minecraftServer.getCommandManager().execute(parsedResult, command);

          } catch (Exception e) {
            UTA2.LOGGER.error("Error executing command: " + e.getMessage());
          }
        });
        break;

      case Action.REQUEST_DATA:
        String requestTarget = payload != null ? payload.request_target : "";
        if (requestTarget.isEmpty()) {
          UTA2.LOGGER.error("Received request_data message with empty request_target");
          return;
        }

        task target = taskQueue.stream()
            .filter(t -> t.action.equals(requestTarget))
            .findFirst()
            .orElse(null);
        if (target != null) {
          UTA2.LOGGER.info("Found task for action: " + target.action + " with function " + target.function);
          try {
            String functionString = target.function.toString() + " " + payload.data.toString();

            ParseResults<ServerCommandSource> parsedResult = minecraftServer.getCommandManager().getDispatcher()
                .parse(functionString, minecraftServer.getCommandSource().withLevel(2));
            minecraftServer.getCommandManager().execute(parsedResult, functionString);
          } catch (Exception e) {
            UTA2.LOGGER.error("Error executing task function: " + e.getMessage());
          }
          taskQueue.remove(target); // Remove task after execution
        } else {
          UTA2.LOGGER.warn("No task found for action: " + requestTarget);
        }

        break;

      case Action.TEAM_JOIN:
        UTA2.LOGGER.info("Received team join message");
        if (payload != null && payload.teamData != null) {
          for (teamData team : payload.teamData) {
            Integer teamId = team.team != null ? Integer.parseInt(team.team) : null;
            for (String uuid : team.uuids) {
              if (uuid == null || uuid.isEmpty()) {
                UTA2.LOGGER.warn("Received empty UUID in team join message for team: " + team.team);
                continue;
              }
              String ScoreboardName = minecraftServer.getPlayerManager().getPlayer(uuid).getNameForScoreboard();
              ServerScoreboard scoreboardManager = minecraftServer.getScoreboard();
              ScoreHolder holder = scoreboardManager.getKnownScoreHolders().stream()
                  .filter(h -> h.getNameForScoreboard().equals(ScoreboardName))
                  .findFirst()
                  .orElse(null);
              ScoreboardObjective objective = scoreboardManager.getObjectives().stream()
                  .filter(obj -> obj.getName().equals("tid")).findFirst().orElse(null);
              scoreboardManager.getOrCreateScore(holder, objective).setScore(teamId);
              if (holder == null) {
                UTA2.LOGGER.warn("No ScoreHolder found for UUID: " + uuid + " in team join message");
                continue;
              }
            }
          }
        } else {
          UTA2.LOGGER.error("Received team join message with no data");
        }

        break;
      case Action.ERROR:
        String errorMessage = payload != null ? payload.message : "Unknown error";
        UTA2.LOGGER.error("Received error message: " + errorMessage);
        break;
      case Action.GET_PLAYER_DATA:
        if (payload != null && payload.player instanceof PlayerData) {
          PlayerData result = (PlayerData) payload.player;
          String uuid = result.uuid;
          if (uuid.isEmpty() || uuid.isBlank()) {
            UTA2.LOGGER.error("Received GET_PLAYER_DATA message with invalid payload");
          }

          UTA2.LOGGER.info("Received request for player data: " + uuid);
          PlayerDatabase.updatePlayerData(result);
        } else {
          UTA2.LOGGER.error("Received GET_PLAYER_DATA message with invalid payload");
        }
        break;
      case Action.QUEUE_MATCH:
        
        break;
      default:
        UTA2.LOGGER.info("Received unknown action: " + action);
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
            } else if (!message.contains("heartbeat")) {
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
    if (taskQueue == null) {
      taskQueue = new ArrayList<>();
    }
    taskQueue.add(new task(action, function));
    UTA2.LOGGER.info("Task added: " + action + " with function " + function);
  }
}
