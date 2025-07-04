package com.pikacnu.src.websocket.handler;

import com.mojang.brigadier.ParseResults;
import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.Status;
import com.pikacnu.src.json.data.Payload;
import com.pikacnu.UTA2;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

public class CommandHandler extends BaseHandler {

  public CommandHandler(MinecraftServer server) {
    super(server);
  }

  @Override
  public void handle(Action action, Status status, String sessionId, Payload payload) {
    String command = payload != null ? payload.command : "";
    if (command.isEmpty()) {
      UTA2.LOGGER.error("Received command message with empty command");
      return;
    }

    UTA2.LOGGER.info("Received command: " + command);
    if (server == null) {
      UTA2.LOGGER.error("Minecraft server instance is null, cannot execute command.");
      return;
    }

    server.execute(() -> {
      try {
        ParseResults<ServerCommandSource> parsedResult = server.getCommandManager().getDispatcher()
            .parse(command, server.getCommandSource().withLevel(2));
        server.getCommandManager().execute(parsedResult, command);
      } catch (Exception e) {
        UTA2.LOGGER.error("Error executing command: " + e.getMessage());
      }
    });
  }

  @Override
  public Action getActionType() {
    return Action.COMMAND;
  }
}
