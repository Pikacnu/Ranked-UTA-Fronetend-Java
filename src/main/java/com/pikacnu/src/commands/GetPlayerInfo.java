package com.pikacnu.src.commands;

import com.google.gson.Gson;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.pikacnu.src.PlayerDatabase;
import com.pikacnu.src.PartyDatabase;
import com.pikacnu.src.PartyDatabase.PartyData;
import com.pikacnu.src.PlayerDatabase.PlayerData;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class GetPlayerInfo implements ICommand {

  @Override
  public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    dispatcher.register(CommandManager.literal("get_player_info").requires(
        source -> source.hasPermissionLevel(4) // Requires operator permission level
    ).executes(new ExecuteCommand()));
  }

  private static class ExecuteCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) {
      ServerCommandSource source = context.getSource();
      PlayerEntity player = source.getPlayer();
      Gson gson = new Gson();
      PlayerData playerData = PlayerDatabase.getPlayerData(player.getUuidAsString());
      PartyData partyData = PartyDatabase.getPartyData(player.getUuidAsString());
      source.sendMessage(Text.literal("Player Info \n").withColor(0x00FF00).append(
          Text.literal(gson.toJson(playerData)).withColor(0xFFFFFF).append(
              Text.literal("\n").withColor(0xFFFF00)).append(Text.literal("Party Info \n").withColor(0x00FF00)).append(
                  Text.literal(gson.toJson(partyData)).withColor(0xFFFFFF))));
      return 1; // Return success
    }
  }
}
