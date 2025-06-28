package com.pikacnu.src.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.slf4j.Logger;

import com.pikacnu.UTA2;
import com.pikacnu.src.WebSocket;
import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.Message;
import com.pikacnu.src.json.Payload;

public class SendWsCommand implements ICommand {
  @Override
  public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    dispatcher.register(
        CommandManager.literal("send_ws").requires(
            source -> source.hasPermissionLevel(2) // Requires operator permission level
        ).then(
            CommandManager.argument("message", StringArgumentType.string())
                .executes(context -> {
                  String message = StringArgumentType.getString(context, "message");
                  try {
                    if (message == null || message.isEmpty()) {
                      context.getSource().sendError(Text.literal("Message cannot be empty!").withColor(0xFF0000));
                      return 0; // Return 0 to indicate failure
                    }
                    Logger LOGGER = UTA2.LOGGER; // Use the mod's logger
                    LOGGER.info("Sending message: " + message); // Log the message being sent

                    // Create structured message
                    Payload payload = new Payload();
                    payload.message = message;
                    Message wsMessage = new Message(Action.COMMAND, WebSocket.serverSessionId, payload);
                    WebSocket.sendMessage(wsMessage);

                    context.getSource().sendMessage(Text.literal("Message sent: " + message).withColor(0x00FF00));
                  } catch (Exception e) {
                    e.printStackTrace();
                  }

                  return 1; // Return 1 to indicate success
                }))); // Register the command with the dispatcher
  }
}
