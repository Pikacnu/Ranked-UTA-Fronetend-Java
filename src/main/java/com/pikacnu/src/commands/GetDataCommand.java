package com.pikacnu.src.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pikacnu.UTA2;
import net.minecraft.command.argument.CommandFunctionArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import com.pikacnu.src.websocket.WebSocketClient;
import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.data.Message;
import com.pikacnu.src.json.data.Payload;

public class GetDataCommand implements ICommand
{
	@Override
	public void register(CommandDispatcher<ServerCommandSource> dispatcher)
	{
		dispatcher.register(CommandManager.literal("get_data")
				.requires(source -> source.hasPermissionLevel(2))
				.then(CommandManager.argument("type", StringArgumentType.word())
					.then(CommandManager.argument("function", CommandFunctionArgumentType.commandFunction())
						.executes(new ExecuteFunction()))));
	}

	private static class ExecuteFunction implements Command<ServerCommandSource>
	{
		@Override
		public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
		{
			try
			{
				String type = StringArgumentType.getString(context, "type");
				Identifier function = CommandFunctionArgumentType.getFunctionOrTag(context, "function").getFirst();

				WebSocketClient.addTask(type, function);

				Payload payload = new Payload();
				payload.request_target = type;

				WebSocketClient.sendMessage(new Message(Action.request_data, WebSocketClient.serverSessionId, payload));

			}
			catch (Exception e)
			{
				UTA2.LOGGER.error("Failed to run function data event!", e);
				context.getSource().sendError(Text.literal("Failed to run function data event!").withColor(0xFF0000));
				return 0;
			}

			return 1; // Return 1 to indicate success
		}
	}
}