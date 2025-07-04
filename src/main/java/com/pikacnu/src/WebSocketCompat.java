package com.pikacnu.src;

import com.pikacnu.src.websocket.WebSocketClient;
import com.pikacnu.src.json.data.Message;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

/**
 * 向後相容的 WebSocket 類別包裝器
 * 將所有呼叫轉發到新的 WebSocketClient 類別
 * 
 * @deprecated 使用 com.pikacnu.src.websocket.WebSocketClient 替代
 */
@Deprecated
public class WebSocketCompat {

  /**
   * @deprecated 使用 WebSocketClient.init() 替代
   */
  @Deprecated
  public static void init(MinecraftServer server) {
    WebSocketClient.init(server);
  }

  /**
   * @deprecated 使用 WebSocketClient.sendMessage() 替代
   */
  @Deprecated
  public static void sendMessage(Message message) {
    WebSocketClient.sendMessage(message);
  }

  /**
   * @deprecated 使用 WebSocketClient.sendMessage() 替代
   */
  @Deprecated
  public static void sendMessage(String message) {
    WebSocketClient.sendMessage(message);
  }

  /**
   * @deprecated 使用 WebSocketClient.isConnected() 替代
   */
  @Deprecated
  public static boolean isConnected() {
    return WebSocketClient.isConnected();
  }

  /**
   * @deprecated 使用 WebSocketClient.disconnect() 替代
   */
  @Deprecated
  public static void disconnect() {
    WebSocketClient.disconnect();
  }

  /**
   * @deprecated 使用 WebSocketClient.shutdown() 替代
   */
  @Deprecated
  public static void shutdown() {
    WebSocketClient.shutdown();
  }

  /**
   * @deprecated 使用 WebSocketClient.addTask() 替代
   */
  @Deprecated
  public static void addTask(String action, Identifier function) {
    WebSocketClient.addTask(action, function);
  }

  /**
   * @deprecated 使用 WebSocketClient.Task 替代
   */
  @Deprecated
  public static class task extends WebSocketClient.Task {
    public task(String action, Identifier function) {
      super(action, function);
    }
  }
}
