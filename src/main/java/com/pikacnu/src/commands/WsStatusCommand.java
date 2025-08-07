package com.pikacnu.src.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.pikacnu.UTA2;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import com.pikacnu.src.websocket.WebSocketClient;

public class WsStatusCommand implements ICommand {
  @Override
  public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    dispatcher.register(
        CommandManager.literal("ws_status").requires(
            source -> source.hasPermissionLevel(2)).executes(context -> {
              try {
                boolean connected = WebSocketClient.isConnected();
                String status = connected ? "Connected" : "Disconnected";
                int color = connected ? 0x00FF00 : 0xFF0000;

                context.getSource().sendMessage(Text.literal("WebSocket Status: " + status).withColor(color));
              } catch (Exception e) {
                UTA2.LOGGER.error("ws status command failed", e);
              }
              return 1;
            }));
  }
}
