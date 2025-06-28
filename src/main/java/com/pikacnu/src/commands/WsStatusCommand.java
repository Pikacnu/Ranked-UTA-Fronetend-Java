package com.pikacnu.src.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import com.pikacnu.src.WebSocket;

public class WsStatusCommand implements ICommand {
  @Override
  public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    dispatcher.register(
        CommandManager.literal("ws_status").requires(
            source -> source.hasPermissionLevel(2)).executes(context -> {
              try {
                boolean connected = WebSocket.isConnected();
                String status = connected ? "Connected" : "Disconnected";
                int color = connected ? 0x00FF00 : 0xFF0000;

                context.getSource().sendMessage(
                    Text.literal("WebSocket Status: " + status).withColor(color));

                String url = "ws://" + WebSocket.host + ":" + WebSocket.port + WebSocket.path;
                context.getSource().sendMessage(
                    Text.literal("Target URL: " + url).withColor(0xFFFFFF));
              } catch (Exception e) {
                e.printStackTrace();
              }
              return 1;
            }));
  }
}
