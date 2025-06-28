package com.pikacnu.src.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.pikacnu.src.PlayerDatabase;
import com.pikacnu.src.PlayerDatabase.PlayerData;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ScoreCommand implements ICommand {
  @Override
  public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    dispatcher.register(
        CommandManager.literal("score")
            .executes(context -> {
              String playerUuid = context.getSource().getPlayer().getUuidAsString();
              PlayerData playerData = PlayerDatabase.getPlayerData(playerUuid);
              if (playerData == null) {
                context.getSource().sendError(Text.literal("You must be registered to view your score."));
                return 0; // Return 0 to indicate failure
              }
              context.getSource().sendMessage(Text.literal("Your score is: " + playerData.score));
              return 1; // Return 1 to indicate success
            }));

  }

}
