package com.pikacnu.src.websocket.handler;

import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.Status;
import com.pikacnu.src.json.data.Message;
import com.pikacnu.src.json.data.Payload;
import com.pikacnu.src.json.data.Payload.handshakeData;
import com.pikacnu.src.websocket.WebSocketClient;
import com.pikacnu.Config;
import com.pikacnu.UTA2;
import net.minecraft.server.MinecraftServer;

public class HandshakeHandler extends BaseHandler {

  public HandshakeHandler(MinecraftServer server) {
    super(server);
  }

  @Override
  public void handle(Action action, Status status, String sessionId, Payload payload) {
    if (!(sessionId == null || sessionId.isEmpty())) {
      if (Config.serverId.equals("null")) {
        WebSocketClient.serverSessionId = sessionId;
        Config.setServerId(sessionId);
      }
      UTA2.LOGGER.info("Handshake received, server ID: " + sessionId);

      Payload handshakePayload = new Payload();
      handshakePayload.handshake = new handshakeData(Config.isLobby, Config.minecraftServerIP,
          Config.minecraftServerPort, Config.serverId);
      Message handshakeMessage = new Message(Action.HANDSHAKE, sessionId, handshakePayload);
      WebSocketClient.sendMessage(handshakeMessage);
    } else {
      UTA2.LOGGER.error("Handshake message missing serverId");
    }
  }

  @Override
  public Action getActionType() {
    return Action.HANDSHAKE;
  }
}
