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
import com.pikacnu.src.json.data.MapChoose;

public class MapChooseCommand implements ICommand
{
	@Override
	public void register(CommandDispatcher<ServerCommandSource> dispatcher)
	{
		dispatcher.register(CommandManager.literal("map_choose")
				.requires(source -> source.hasPermissionLevel(2)) // Requires operator permission level
				.then(CommandManager.argument("map_index", IntegerArgumentType.integer(0, 18))
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
				int mapId = IntegerArgumentType.getInteger(context, "map_index");

				// Send the map choose data
				Payload payload = new Payload();
				payload.data = new MapChoose(mapId); // Create map choose object
				WebSocketClient.sendMessage(new Message(Action.map_choose, WebSocketClient.serverSessionId, payload));

				source.sendMessage(Text.literal("Map choose event sent successfully!").withColor(0x00FF00));
			}
			catch (Exception e)
			{
				source.sendError(Text.literal("Failed to send map choose event!").withColor(0xFF0000));
				UTA2.LOGGER.error("Failed to send map choose event!", e);
				return 0;
			}

			return SINGLE_SUCCESS; // Return 1 to indicate success
		}
	}
}