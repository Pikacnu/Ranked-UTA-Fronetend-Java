package com.pikacnu.src.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import com.pikacnu.UTA2;
import com.pikacnu.src.PartyDatabase;
import com.pikacnu.src.PartyDatabase.PartyData;
import com.pikacnu.src.PartyDatabase.PartyResultMessage;

public class PartyCommand implements ICommand
{
	private final String[] partyActions =
	{
		"leave",
		"invite",
		"accept",
		"reject",
		"list",
		"disband"
	};

	@Override
	public void register(CommandDispatcher<ServerCommandSource> dispatcher)
	{
		dispatcher.register(CommandManager.literal("party")
				.then(CommandManager.argument("action", StringArgumentType.string())
					.suggests((context, builder) ->
					{
						for (String action : partyActions)
							builder.suggest(action);
						return builder.buildFuture();
					})
					.executes(new ExecuteAction())
					.then(CommandManager.argument("target", EntityArgumentType.player())
						.executes(new ExecuteTarget()))));
	}

	private static class ExecuteAction implements Command<ServerCommandSource>
	{
		@Override
		public int run(CommandContext<ServerCommandSource> context)
		{
			ServerCommandSource source = context.getSource();

			String action = StringArgumentType.getString(context, "action");
			if (action.isEmpty())
			{
				source.sendError(Text.literal("Action cannot be empty!").withColor(0xFF0000));
				return 0;
			}

			PlayerEntity player = source.getPlayer();
			if (player == null)
			{
				source.sendError(Text.literal("Can't found player!").withColor(0xFF0000));
				return 0;
			}
			String playerUuid = player.getUuidAsString();
			PartyDatabase.PartyResultMessage message;
			PartyData party;
			switch (action)
			{
				case "leave":
					party = PartyDatabase.getPartyData(playerUuid);
					if (party == null)
					{
						source.sendError(Text.literal(PartyResultMessage.PLAYER_NOT_IN_PARTY.toString()).withColor(0xFF0000));
						return 0;
					}
					message = party.removePlayer(playerUuid);
					break;

				case "list":
					party = PartyDatabase.getPartyData(playerUuid);
					if (party == null)
					{
						source.sendError(Text.literal(PartyResultMessage.PLAYER_NOT_IN_PARTY.toString()).withColor(0xFF0000));
						return 0;
					}
					StringBuilder partyList = new StringBuilder("在隊伍中的玩家: ");
					synchronized (party.partyMembers)
					{
						for (PartyDatabase.PartyPlayer member : party.partyMembers)
							partyList.append(member.minecraftId).append(", ");
					}
					source.sendMessage(Text.literal(partyList.toString()).withColor(0x00FF00));
					return 1;

				case "disband":
					party = PartyDatabase.getPartyData(playerUuid);
					if (party == null)
					{
						source.sendError(Text.literal(PartyResultMessage.PLAYER_NOT_IN_PARTY.toString()).withColor(0xFF0000));
						return 0;
					}
					party.disbandParty();
					message = PartyDatabase.PartyResultMessage.PARTY_DISBANDED;
					break;

				default:
					source.sendError(Text.literal("無效的動作！請使用 'leave' 而不需要目標。").withColor(0xFF0000));
					return 0;
			}

			if (message == null)
			{
				source.sendError(Text.literal("執行隊伍動作失敗！").withColor(0xFF0000));
				return 0;
			}

			source.sendMessage(Text.literal(message.getMessage().replace("{target}", String.valueOf(player.getDisplayName()))).withColor(0x00FF00));
			return 1;
		}
	}

	private static class ExecuteTarget implements Command<ServerCommandSource>
	{
		@Override
		public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
		{
			ServerCommandSource source = context.getSource();

			String action = StringArgumentType.getString(context, "action");
			String target = EntityArgumentType.getPlayer(context, "target").getUuidAsString();
			if (action.isEmpty() || target.isEmpty())
			{
				source.sendError(Text.literal("動作和目標不能為空！").withColor(0xFF0000));
				return 0;
			}

			PlayerEntity player = source.getPlayer();
			if (player == null)
			{
				source.sendError(Text.literal("Can't found player!").withColor(0xFF0000));
				return 0;
			}
			String playerUuid = player.getUuidAsString();
			PartyDatabase.PartyResultMessage message;

			switch (action)
			{
				case "invite" ->
				{
					if (PartyDatabase.getInvitation(playerUuid) != null)
					{
						source.sendError(Text.literal("您已經收到邀請。請先拒絕它後再邀請。").withColor(0xFF0000));
						return 0;
					}

					if (target.equals(playerUuid))
					{
						source.sendError(Text.literal("你不能邀請你自己！").withColor(0xFF0000));
						return 0;
					}

					if (PartyDatabase.getPartyData(target) != null)
					{
						source.sendError(Text.literal("該玩家已在隊伍中!").withColor(0xFF0000));
						return 0;
					}

					PartyData party = PartyDatabase.getPartyData(playerUuid);
					if (party == null)
					{
						party = PartyDatabase.createParty(playerUuid);
						if (party == null)
						{
							source.sendError(Text.literal("建立派對失敗!").withColor(0xFF0000));
							return 0;
						}
					}
					if (party.isInQueue)
						source.sendError(Text.literal("你無法在隊列時邀請。").withColor(0xFF0000));

					if (!party.isPartyLeader(playerUuid))
					{
						source.sendError(Text.literal("你需要是派對隊長才可以邀請玩家").withColor(0xFF0000));
						return 0;
					}
					message = PartyDatabase.createInvitation(party.partyId, playerUuid, target);
				}

				case "accept" -> message = PartyDatabase.AcceptInvitation(playerUuid, target);

				case "reject" -> message = PartyDatabase.RejectInvitation(playerUuid, target);

				default ->
				{
					source.sendError(Text.literal("無效的動作！請使用 'invite'、'accept' 或 'reject' 並指定目標。").withColor(0xFF0000));
					return 0;
				}
			}
			PartyDatabase.partyList.forEach(partyData -> UTA2.LOGGER.info("Party ID: {}, Members: {}", partyData.partyId, partyData.partyMembers.size()));
			PartyDatabase.partyInvitions.forEach(inv -> UTA2.LOGGER.info("Invitation: {} -> {}", inv.inviterUuid, inv.targetUuid));

			if (message == null)
			{
				source.sendError(Text.literal("執行隊伍動作失敗！").withColor(0xFF0000));
				return 0;
			}

			source.sendMessage(
					Text.literal(message.getMessage()
							.replace("{target}", String.valueOf(EntityArgumentType.getPlayer(context, "target").getDisplayName()))
							.replace("{inviter}", String.valueOf(player.getDisplayName()))).withColor(0x00FF00));
			return 1;
		}
	}
}
