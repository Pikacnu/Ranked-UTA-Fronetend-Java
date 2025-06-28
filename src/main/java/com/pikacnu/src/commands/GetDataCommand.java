package com.pikacnu.src.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.argument.CommandFunctionArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import com.pikacnu.src.WebSocket;
import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.Message;
import com.pikacnu.src.json.Payload;

public class GetDataCommand implements ICommand {
  @Override
  public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    dispatcher.register(
        CommandManager.literal("get_data").requires(
            source -> source.hasPermissionLevel(2)).then(
                CommandManager.argument("type", StringArgumentType.word()).then(
                    CommandManager.argument("function", CommandFunctionArgumentType.commandFunction())
                        .executes(context -> {
                          try {
                            String type = StringArgumentType.getString(context, "type");
                            Identifier function = CommandFunctionArgumentType.getFunctionOrTag(context, "function")
                                .getFirst();

                            WebSocket.addTask(type, function);

                            Payload payload = new Payload();
                            payload.request_target = type;

                            WebSocket.sendMessage(
                                new Message(Action.REQUEST_DATA, WebSocket.serverSessionId, payload));

                          } catch (Exception e) {
                            e.printStackTrace();
                            context.getSource().sendError(
                                Text.literal("Failed to run function data event!").withColor(0xFF0000));
                          }
                          return 1; // Return 1 to indicate success
                        }))));
  }
}
