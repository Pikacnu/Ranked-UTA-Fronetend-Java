package com.pikacnu.src.websocket.handler;

import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.Status;
import com.pikacnu.src.json.data.Message;
import com.pikacnu.src.json.data.Payload;
import com.pikacnu.src.json.data.Payload.LobbyData;
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
      WebSocketClient.serverSessionId = sessionId;
      UTA2.LOGGER.info("Handshake received, server ID: " + sessionId);

      Payload handshakePayload = new Payload();
      handshakePayload.lobby = new LobbyData(Config.isLobby);
      Message handshakeMessage = new Message(Action.HANDSHAKE, WebSocketClient.serverSessionId, handshakePayload);
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
