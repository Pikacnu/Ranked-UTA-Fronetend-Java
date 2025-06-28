package com.pikacnu.src.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import com.pikacnu.src.WebSocket;
import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.Message;
import com.pikacnu.src.json.Payload;
import com.pikacnu.src.json.game_status;

public class GameStatusCommand implements ICommand {
  @Override
  public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    dispatcher.register(
        CommandManager.literal("game_status").requires(
            source -> source.hasPermissionLevel(2) // Requires operator permission level
        ).then(
            CommandManager.argument("status", IntegerArgumentType.integer(0, 6))
                .executes(context -> {
                  try {
                    Integer status = IntegerArgumentType.getInteger(context, "status");

                    // Create game status object
                    game_status gameStatusData = new game_status(status);

                    // Send the game status data
                    Payload payload = new Payload();
                    payload.data = gameStatusData;
                    Message wsMessage = new Message(Action.GAME_STATUS, WebSocket.serverSessionId, payload);
                    WebSocket.sendMessage(wsMessage);

                    context.getSource().sendMessage(Text
                        .literal("Game status event sent successfully!").withColor(0x00FF00));
                  } catch (Exception e) {
                    e.printStackTrace();
                    context.getSource().sendError(
                        Text.literal("Failed to send game status event!").withColor(0xFF0000));
                  }
                  return 1; // Return 1 to indicate success
                })));
  }
}
