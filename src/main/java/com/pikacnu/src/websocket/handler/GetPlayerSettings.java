package com.pikacnu.src.websocket.handler;

import java.util.UUID;

import com.pikacnu.UTA2;
import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.Status;
import com.pikacnu.src.json.data.Payload;

import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class GetPlayerSettings extends BaseHandler {

  public GetPlayerSettings(MinecraftServer server) {
    super(server);
  }

  @Override
  public void handle(Action action, Status status, String sessionId, Payload payload) {
    if (payload == null || payload.playerSetting == null) {
      UTA2.LOGGER.error("GetPlayerSettings: Payload or playerSettings is null");
      return;
    }

    String uuid = payload.playerSetting.uuid;
    if (uuid == null || uuid.isEmpty()) {
      UTA2.LOGGER.error("GetPlayerSettings: UUID is null or empty");
      return;
    }

    try {
      ServerPlayerEntity player = server.getPlayerManager().getPlayer(UUID.fromString(uuid));
      if (player == null) {
        UTA2.LOGGER.warn("GetPlayerSettings: Player not found for UUID: {}", uuid);
        return;
      }

      ServerScoreboard scoreboardManager = server.getScoreboard();

      // 更新計分板數值
      updateObjectiveScore(scoreboardManager, player, "N", payload.playerSetting.N);
      updateObjectiveScore(scoreboardManager, player, "Q", payload.playerSetting.Q);
      updateObjectiveScore(scoreboardManager, player, "S", payload.playerSetting.S);
      updateObjectiveScore(scoreboardManager, player, "U", payload.playerSetting.U);
      updateObjectiveScore(scoreboardManager, player, "B", payload.playerSetting.B);

      // 日誌輸出
      UTA2.LOGGER.info("GetPlayerSettings: Player settings updated for UUID: {}", uuid);

    } catch (IllegalArgumentException e) {
      UTA2.LOGGER.error("GetPlayerSettings: Invalid UUID format: {}", uuid);
    } catch (Exception e) {
      UTA2.LOGGER.error("GetPlayerSettings: Error updating player settings for UUID: {}", uuid, e);
    }
  }

  private void updateObjectiveScore(ServerScoreboard scoreboardManager, ServerPlayerEntity player, String objectiveName,
      int value) {
    scoreboardManager.getObjectives().stream()
        .filter(obj -> objectiveName.equals(obj.getName()))
        .findFirst()
        .ifPresentOrElse(
            obj -> {
              scoreboardManager.getOrCreateScore(player, obj).setScore(value);
              UTA2.LOGGER.debug("Updated objective {} to value {} for player {}", objectiveName, value,
                  player.getName().getString());
            },
            () -> UTA2.LOGGER.warn("GetPlayerSettings: Objective {} not found", objectiveName));
  }

  @Override
  public Action getActionType() {
    return Action.get_player_settings;
  }
}
