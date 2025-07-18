package com.pikacnu.src.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import com.pikacnu.src.ActionBarController;
import com.pikacnu.src.PartyDatabase;
import com.pikacnu.src.PlayerDatabase;
import com.pikacnu.src.QueueDatabase;
import com.pikacnu.src.PlayerDatabase.PlayerData;
import com.pikacnu.src.QueueDatabase.QueueType;
import com.pikacnu.src.PartyDatabase.PartyData;

public class QueueCommand implements ICommand {
  @Override
  public void register(CommandDispatcher<ServerCommandSource> dispatcher) {

    dispatcher.register(
        CommandManager.literal("queue")
            .then(CommandManager.argument("action", StringArgumentType.word())
                .suggests((context, builder) -> {
                  String[] actions = { "leave", "solo", "duo", "trio", "siege" };
                  for (String action : actions) {
                    builder.suggest(action);
                  }
                  return builder.buildFuture();
                })
                .executes(context -> {
                  String action = StringArgumentType.getString(context, "action");
                  if (action == null || action.isEmpty()) {
                    context.getSource()
                        .sendError(Text.literal(
                            "指定的操作無效。請使用 /queue <操作>，其中操作為以下之一：leave、solo、duo、trio、siege")
                            .withColor(0xFF0000));
                    return 0; // Return 0 to indicate failure
                  }

                  String[] validActions = { "leave", "solo", "duo", "trio", "siege" };
                  boolean isValidAction = false;
                  for (String validAction : validActions) {
                    if (validAction.equals(action)) {
                      isValidAction = true;
                      break;
                    }
                  }

                  if (!isValidAction) {
                    context.getSource().sendError(Text.literal("指定的操作無效。"));
                    return 0; // Return 0 to indicate failure
                  }

                  // Create queue object
                  if (action.equals("leave")) {
                    context.getSource().sendFeedback(() -> Text.literal("您已離開佇列。"), false);
                    PartyData party = PartyDatabase.getPartyData(context.getSource().getPlayer().getUuidAsString());
                    if (party != null) {
                      party.isInQueue = false;
                      party.partyMembers.stream().forEach(member -> {
                        PlayerData memberData = PlayerDatabase.getPlayerData(member.uuid);
                        if (memberData != null) {
                          memberData.isInQueue = false;
                          ActionBarController.removeActionBarMessage(member.uuid);
                          PlayerDatabase.updatePlayerData(memberData);
                        }
                      });
                      PartyDatabase.updatePartyData(party);
                      if (party.partyMembers.size() == 1) {
                        party.disbandParty();
                        context.getSource().sendFeedback(() -> Text.literal("您的隊伍已解散。"), false);
                      } else {
                        context.getSource().sendFeedback(() -> Text.literal("您的隊伍已離開佇列。"), false);
                      }
                    }
                    QueueDatabase.updateQueueData(party.partyId, QueueType.leave,
                        context.getSource().getPlayer().getUuidAsString());
                    QueueDatabase.removeQueueData(party.partyId);
                  } else {
                    PlayerData player = PlayerDatabase.getPlayerData(context.getSource().getPlayer().getUuidAsString());

                    if (player == null) {
                      context.getSource().sendError(Text.literal("您必須先註冊才能加入佇列。"));
                      return 0; // Return 0 to indicate failure
                    }

                    PartyData party = PartyDatabase.getPartyData(player.uuid);
                    if (party == null) {
                      party = PartyDatabase.createParty(context.getSource().getPlayer().getUuidAsString());
                    } else if (party.isPartyLeader(player.uuid)) {
                      if (party.isInQueue) {
                        context.getSource().sendError(Text.literal("您的隊伍已經在佇列中。"));
                        return 0; // Return 0 to indicate failure
                      }
                    } else {
                      context.getSource().sendError(Text.literal("您必須是隊長才能加入佇列。"));
                      return 0; // Return 0 to indicate failure
                    }

                    int playerCount = party.getPartySize();
                    if (playerCount < 1 || playerCount > 4) {
                      context.getSource()
                          .sendError(Text.literal("隊伍人數無效。必須在 1 到 4 名玩家之間。"));
                      return 0; // Return 0 to indicate failure
                    }

                    if (action.equals("solo") && playerCount > 1) {
                      context.getSource().sendError(Text.literal("您不能在隊伍中使用 solo 佇列。"));
                      return 0; // Return 0 to indicate failure
                    }
                    if (action.equals("duo") && playerCount > 2) {
                      context.getSource().sendError(Text.literal("您不能在隊伍中使用 duo 佇列。"));
                      return 0; // Return 0 to indicate failure
                    }
                    if (action.equals("trio") && playerCount > 3) {
                      context.getSource().sendError(Text.literal("您不能在隊伍中使用 trio 佇列。"));
                      return 0; // Return 0 to indicate failure
                    }
                    if (action.equals("siege") && playerCount > 4) {
                      context.getSource().sendError(Text.literal("您不能在隊伍中使用 siege 佇列。"));
                      return 0; // Return 0 to indicate failure
                    }

                    context.getSource().sendFeedback(() -> Text.literal("您已加入 " + action + " 佇列。"),
                        false);

                    party.isInQueue = true;
                    PartyDatabase.updatePartyData(party);

                    party.partyMembers.stream().forEach(member -> {
                      PlayerData memberData = PlayerDatabase.getPlayerData(member.uuid);
                      if (memberData != null) {
                        memberData.isInQueue = true;
                        ActionBarController.addActionBarMessage(
                            Text.literal("您的隊伍已加入佇列: " + action), member.uuid);
                        PlayerDatabase.updatePlayerData(memberData);
                      }
                    });

                    QueueDatabase.addQueueData(party.partyId, QueueDatabase.QueueType.valueOf(action));
                    QueueDatabase.updateQueueData(party.partyId, QueueDatabase.QueueType.valueOf(action),
                        context.getSource().getPlayer().getUuidAsString());
                  }
                  return 1; // Return 1 to indicate success
                })));
  }
}
