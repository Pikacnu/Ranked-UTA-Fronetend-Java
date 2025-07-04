package com.pikacnu.src.websocket.handler;

import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.Status;
import com.pikacnu.src.json.data.Payload;
import com.pikacnu.src.json.data.Payload.TeamData;
import com.pikacnu.UTA2;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;

public class TeamJoinHandler extends BaseHandler {

  public TeamJoinHandler(MinecraftServer server) {
    super(server);
  }

  @Override
  public void handle(Action action, Status status, String sessionId, Payload payload) {
    UTA2.LOGGER.info("Received team join message");
    if (payload != null && payload.teamData != null) {
      for (TeamData team : payload.teamData) {
        Integer teamId = team.team() != null ? Integer.parseInt(team.team()) : null;
        for (String name : team.names()) {
          if (name == null || name.isEmpty()) {
            UTA2.LOGGER.warn("Received empty UUID in team join message for team: " + team.team());
            continue;
          }
          /*
           * ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
           * if (player == null) {
           * UTA2.LOGGER.warn("Player not found or not online for UUID: " + uuid +
           * " in team join message");
           * continue;
           * }
           * String ScoreboardName = player.getNameForScoreboard();
           */
          ServerScoreboard scoreboardManager = server.getScoreboard();
          /*
           * ScoreHolder holder = scoreboardManager.getKnownScoreHolders().stream()
           * .filter(h -> h.getNameForScoreboard().equals(ScoreboardName))
           * .findFirst()
           * .orElse(null);
           */
          ScoreHolder holder = ScoreHolder.fromName(name);
          ScoreboardObjective objective = scoreboardManager.getObjectives().stream()
              .filter(obj -> obj.getName().equals("tid")).findFirst().orElse(null);
          if (holder != null && objective != null) {
            scoreboardManager.getOrCreateScore(holder, objective).setScore(teamId);
          } else {
            UTA2.LOGGER.warn("No ScoreHolder or objective found for UUID: " + name + " in team join message");
          }
        }
      }
    } else {
      UTA2.LOGGER.error("Received team join message with no data");
    }
  }

  @Override
  public Action getActionType() {
    return Action.TEAM_JOIN;
  }
}
