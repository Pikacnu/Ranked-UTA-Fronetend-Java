package com.pikacnu.src.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.pikacnu.UTA2;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ScoreHolderArgumentType;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import com.pikacnu.src.websocket.WebSocketClient;
import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.data.Message;
import com.pikacnu.src.json.data.Payload;
import com.pikacnu.src.json.data.Damage;

public class DamagedCommand implements ICommand
{
	@Override
	public void register(CommandDispatcher<ServerCommandSource> dispatcher)
	{
		dispatcher.register(CommandManager.literal("damaged")
				.requires(source -> source.hasPermissionLevel(2)) // Requires operator permission level)
				.then(CommandManager.argument("target", EntityArgumentType.player())
					.then(CommandManager.argument("attacker", EntityArgumentType.player())
						.then(CommandManager.argument("name", ScoreHolderArgumentType.scoreHolder())
							.then(CommandManager.argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective())
								.executes(new ExecuteCommand()))))));
	}

	private static class ExecuteCommand implements Command<ServerCommandSource>
	{
		@Override
		public int run(CommandContext<ServerCommandSource> context)
		{
			ServerCommandSource source = context.getSource();

			try
			{
				// Get the arguments
				String target = EntityArgumentType.getEntity(context, "target").getUuidAsString();
				String attacker;

				try
				{
					attacker = EntityArgumentType.getEntity(context, "attacker").getUuidAsString();
				}
				catch (Exception e)
				{
					attacker = "system"; // Default to "none" if attacker not found
				}

				if (attacker == null || attacker.isEmpty())
				{
					source.sendError(Text.literal("Attacker cannot be empty!").withColor(0xFF0000));
					return 0; // Return 0 to indicate failure
				}

				ScoreHolder name = ScoreHolderArgumentType.getScoreHolder(context, "name");
				ScoreboardObjective objective = ScoreboardObjectiveArgumentType.getObjective(context, "objective");
				int damage = source.getServer()
						.getScoreboard()
						.getOrCreateScore(name, objective)
						.getScore();

				// Send the damage data
				Payload payload = new Payload();
				payload.data = new Damage(target, attacker, damage); // Create damage object
				WebSocketClient.sendMessage(new Message(Action.damage, WebSocketClient.serverSessionId, payload));

				source.sendMessage(Text.literal("Player damaged event sent successfully!").withColor(0x00FF00));
			}
			catch (Exception e)
			{
				source.sendError(Text.literal("Failed to send player damaged event!").withColor(0xFF0000));
				UTA2.LOGGER.error("Failed to send player damaged event!", e);
				return 0;
			}

			return 1; // Return 1 to indicate success
		}
	}
}
