package com.pikacnu.src.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import com.pikacnu.src.PartyDatabase;
import com.pikacnu.src.PlayerDatabase;
import com.pikacnu.src.PlayerDatabase.PlayerData;
import com.pikacnu.src.WebSocket;
import com.pikacnu.src.PartyDatabase.PartyData;
import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.Message;
import com.pikacnu.src.json.Payload;
import com.pikacnu.src.json.Payload.QueueData;

public class QueueCommand implements ICommand {
  @Override
  public void register(CommandDispatcher<ServerCommandSource> dispatcher) {

    dispatcher.register(
        CommandManager.literal("queue")
            .then(CommandManager.argument("action", StringArgumentType.word())
                .suggests((context, builder) -> {
                  String[] actions = { "leave", "solo", "duo", "squad", "siege" };
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
                            "指定的操作無效。請使用 /queue <操作>，其中操作為以下之一：leave、solo、duo、squad、siege")
                            .withColor(0xFF0000));
                    return 0; // Return 0 to indicate failure
                  }

                  Payload payload = new Payload();

                  String[] validActions = { "leave", "solo", "duo", "squad", "siege" };
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
                    QueueData queueData = new QueueData("leave", context.getSource().getPlayer().getUuidAsString());
                    PartyData party = PartyDatabase.getPartyData(context.getSource().getPlayer().getUuidAsString());
                    if (party != null) {
                      party.isInQueue = false;
                      PartyDatabase.updatePartyData(party);
                      if (party.partyMembers.size() == 1) {
                        party.disbandParty();
                        context.getSource().sendFeedback(() -> Text.literal("您的隊伍已解散。"), false);
                      } else {
                        context.getSource().sendFeedback(() -> Text.literal("您的隊伍已離開佇列。"), false);
                      }
                    }
                    payload.queue = queueData;
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

                    context.getSource().sendFeedback(() -> Text.literal("您已加入 " + action + " 佇列。"),
                        false);
                    QueueData queueData = new QueueData(action,
                        context.getSource().getPlayer().getUuidAsString());
                    payload.queue = queueData;
                  }
                  Action actionType;
                  if (action.equals("leave")) {
                    actionType = Action.QUEUE_LEAVE;
                  } else {
                    actionType = Action.QUEUE;
                  }
                  Message wsMessage = new Message(actionType, WebSocket.serverSessionId, payload);
                  WebSocket.sendMessage(wsMessage);
                  return 1; // Return 1 to indicate success
                })));
  }
}
