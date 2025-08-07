package com.pikacnu.src.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.pikacnu.UTA2;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import com.pikacnu.src.websocket.WebSocketClient;
import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.data.Message;
import com.pikacnu.src.json.data.Payload;
import com.pikacnu.src.json.data.GameStatus;

public class GameStatusCommand implements ICommand
{
	@Override
	public void register(CommandDispatcher<ServerCommandSource> dispatcher)
	{
		dispatcher.register(CommandManager.literal("game_status")
				.requires(source -> source.hasPermissionLevel(2)) // Requires operator permission level
				.then(CommandManager.argument("status", IntegerArgumentType.integer(0, 6))
					.executes(new ExecuteCommand())));
	}

	private static class ExecuteCommand implements Command<ServerCommandSource>
	{
		@Override
		public int run(CommandContext<ServerCommandSource> context)
		{
			ServerCommandSource source = context.getSource();

			try
			{
				int status = IntegerArgumentType.getInteger(context, "status");

				// Send the game status data
				Payload payload = new Payload();
				payload.data = new GameStatus(status); // Create game status object
				WebSocketClient.sendMessage(new Message(Action.game_status, WebSocketClient.serverSessionId, payload));

				source.sendMessage(Text.literal("Game status event sent successfully!").withColor(0x00FF00));
			}
			catch (Exception e)
			{
				source.sendError(Text.literal("Failed to send game status event!").withColor(0xFF0000));
				UTA2.LOGGER.error("Failed to send game status event!", e);
				return 0;
			}

			return SINGLE_SUCCESS; // Return 1 to indicate success
		}
	}
}