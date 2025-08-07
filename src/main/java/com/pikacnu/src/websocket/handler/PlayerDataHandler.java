package com.pikacnu.src.websocket.handler;

import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.Status;
import com.pikacnu.src.json.data.Payload;
import com.pikacnu.src.PlayerDatabase;
import com.pikacnu.src.PlayerDatabase.PlayerData;
import com.pikacnu.UTA2;
import net.minecraft.server.MinecraftServer;

public class PlayerDataHandler extends BaseHandler {

  public PlayerDataHandler(MinecraftServer server) {
    super(server);
  }

  @Override
  public void handle(Action action, Status status, String sessionId, Payload payload) {
    if (payload != null && payload.player instanceof PlayerData) {
      PlayerData result = payload.player;
      String uuid = result.uuid;
      if (uuid.isEmpty() || uuid.isBlank()) {
        UTA2.LOGGER.error("Received GET_PLAYER_DATA message with invalid payload");
        return;
      }

      UTA2.LOGGER.info("Received request for player data: {}", uuid);
      PlayerDatabase.updatePlayerData(result);
    } else {
      UTA2.LOGGER.error("Received GET_PLAYER_DATA message with invalid payload");
    }
  }

  @Override
  public Action getActionType() {
    return Action.get_player_data;
  }
}
