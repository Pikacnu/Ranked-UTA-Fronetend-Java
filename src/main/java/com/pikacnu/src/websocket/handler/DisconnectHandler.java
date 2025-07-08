package com.pikacnu.src.websocket.handler;

import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.Status;
import com.pikacnu.src.json.data.Payload;
import com.pikacnu.src.websocket.WebSocketClient;

import com.pikacnu.UTA2;
import net.minecraft.server.MinecraftServer;

public class DisconnectHandler extends BaseHandler {

  public DisconnectHandler(MinecraftServer server) {
    super(server);
  }

  @Override
  public void handle(Action action, Status status, String sessionId, Payload payload) {
    UTA2.LOGGER.info("Received disconnect message");
    WebSocketClient.disconnect();
    // Note: 在重構後，reconnect 邏輯應該由 WebSocketClient 處理
    // 這裡只記錄訊息，實際的重連邏輯在 WebSocketClient 中
  }

  @Override
  public Action getActionType() {
    return Action.disconnect;
  }
}
