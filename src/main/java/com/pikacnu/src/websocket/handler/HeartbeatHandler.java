package com.pikacnu.src.websocket.handler;

import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.Status;
import com.pikacnu.src.json.data.Message;
import com.pikacnu.src.json.data.Payload;
import com.pikacnu.src.websocket.WebSocketClient;
import net.minecraft.server.MinecraftServer;

public class HeartbeatHandler extends BaseHandler {

  public HeartbeatHandler(MinecraftServer server) {
    super(server);
  }

  @Override
  public void handle(Action action, Status status, String sessionId, Payload payload) {
    Message heartbeatMessage = new Message(Action.HEARTBEAT, WebSocketClient.serverSessionId);
    WebSocketClient.sendMessage(heartbeatMessage);
  }

  @Override
  public Action getActionType() {
    return Action.HEARTBEAT;
  }
}
