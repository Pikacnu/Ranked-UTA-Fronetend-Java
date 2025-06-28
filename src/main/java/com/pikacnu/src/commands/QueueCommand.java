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
import com.pikacnu.src.json.Queue;

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
                            "Invalid action specified. Use /queue <action> where action is one of: leave, solo, duo, squad, siege")
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
                    context.getSource().sendError(Text.literal("Invalid action specified."));
                    return 0; // Return 0 to indicate failure
                  }

                  // Create queue object
                  if (action.equals("leave")) {
                    context.getSource().sendFeedback(() -> Text.literal("You have left the queue."), false);
                    Queue queueData = new Queue("leave", context.getSource().getPlayer().getUuidAsString());
                    payload.data = queueData;
                  } else {
                    PlayerData player = PlayerDatabase.getPlayerData(context.getSource().getPlayer().getUuidAsString());

                    if (player == null) {
                      context.getSource().sendError(Text.literal("You must be registered to join the queue."));
                      return 0; // Return 0 to indicate failure
                    }

                    PartyData party = PartyDatabase.getPartyData(player.uuid);
                    if (party == null) {
                      party = PartyDatabase.createParty(context.getSource().getPlayer().getUuidAsString());
                    } else if (party.isPartyLeader(player.uuid)) {
                      if (party.isInQueue) {
                        context.getSource().sendError(Text.literal("Your party is already in the queue."));
                        return 0; // Return 0 to indicate failure
                      }
                    } else {
                      context.getSource().sendError(Text.literal("You must be the party leader to join the queue."));
                      return 0; // Return 0 to indicate failure
                    }

                    int playerCount = party.getPartySize();
                    if (playerCount < 1 || playerCount > 4) {
                      context.getSource()
                          .sendError(Text.literal("Invalid party size. Must be between 1 and 4 players."));
                      return 0; // Return 0 to indicate failure
                    }

                    context.getSource().sendFeedback(() -> Text.literal("You have joined the queue as " + action + "."),
                        false);
                    Queue queueData = new Queue(action, context.getSource().getPlayer().getUuidAsString());
                    payload.data = queueData;
                  }
                  Message wsMessage = new Message(Action.QUEUE, WebSocket.serverSessionId, payload);
                  WebSocket.sendMessage(wsMessage);
                  return 1; // Return 1 to indicate success
                })));
  }
}
