package com.pikacnu.src.websocket.handler;

import java.util.UUID;
import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.Status;
import com.pikacnu.src.json.data.Payload;
import com.pikacnu.UTA2;
import net.minecraft.network.packet.s2c.common.ServerTransferS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class TransferHandler extends BaseHandler {

  public TransferHandler(MinecraftServer server) {
    super(server);
  }

  @Override
  public void handle(Action action, Status status, String sessionId, Payload payload) {
    if (payload == null || payload.transferData == null) {
      UTA2.LOGGER.error("Received TRANSFER message with invalid payload");
      return;
    }

    String targetServer = payload.transferData.targetServer;
    Integer targetPort = payload.transferData.targetPort;
    if (targetServer == null || targetServer.isEmpty() || targetPort == null) {
      UTA2.LOGGER.error("Received TRANSFER message with invalid target server or port");
      return;
    }

    payload.transferData.uuids.stream().forEach(uuid -> {
      ServerPlayerEntity sendTarget = server.getPlayerManager()
          .getPlayer(UUID.fromString(uuid));
      if (sendTarget != null) {
        sendTarget.networkHandler.sendPacket(new ServerTransferS2CPacket(targetServer, targetPort));
      } else {
        UTA2.LOGGER.warn("No player found for UUID: " + uuid);
      }
    });
  }

  @Override
  public Action getActionType() {
    return Action.TRANSFER;
  }
}
