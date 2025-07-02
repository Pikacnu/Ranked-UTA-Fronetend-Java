package com.pikacnu.src.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import com.mojang.brigadier.arguments.StringArgumentType;

import com.pikacnu.src.WebSocket;
import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.KillType;
import com.pikacnu.src.json.Message;
import com.pikacnu.src.json.Payload;
import com.pikacnu.src.json.kill;

public class PlayerKilledCommand implements ICommand {
  @Override
  public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    dispatcher.register(
        CommandManager.literal("player_killed").requires(
            source -> source.hasPermissionLevel(2) // Requires operator permission level
        ).then(
            CommandManager.argument("attack_target", EntityArgumentType.player())
                .then(
                    CommandManager.argument("attacker_type", StringArgumentType.string())
                        .suggests((context, builder) -> {
                          // Suggest attacker types
                          for (KillType type : KillType.values()) {
                            builder.suggest(type.getString());
                          }
                          return builder.buildFuture();
                        })
                        .then(CommandManager.argument("attacker", EntityArgumentType.player())
                            .executes(context -> {
                              try {
                                // Get the arguments
                                String target = EntityArgumentType.getEntity(context, "attack_target").getUuid()
                                    .toString();
                                String attackerType = StringArgumentType.getString(context, "attacker_type");
                                String attacker;
                                try {
                                  attacker = EntityArgumentType.getEntity(context, "attacker").getUuidAsString();
                                } catch (Exception e) {
                                  attacker = "none"; // Default to "none" if attacker not found
                                }

                                if (attacker == null || attacker.isEmpty()) {
                                  context.getSource().sendError(
                                      Text.literal("Attacker cannot be empty!").withColor(0xFF0000));
                                  return 0; // Return 0 to indicate failure
                                }

                                try {
                                  KillType.fromString(attackerType); // Validate attacker type
                                } catch (IllegalArgumentException e) {
                                  context.getSource().sendError(
                                      Text.literal("Invalid attacker type!").withColor(0xFF0000));
                                  return 0;
                                }

                                // Create kill object
                                kill killData = new kill(target, KillType.fromString(attackerType), attacker);

                                // Send the kill data
                                Payload payload = new Payload();
                                payload.data = killData;
                                Message wsMessage = new Message(Action.KILL, WebSocket.serverSessionId, payload);
                                WebSocket.sendMessage(wsMessage);

                                context.getSource().sendMessage(Text
                                    .literal("Player killed event sent successfully!").withColor(0x00FF00));
                              } catch (Exception e) {
                                e.printStackTrace();
                                context.getSource().sendError(
                                    Text.literal("Failed to send player killed event!").withColor(0xFF0000));
                              }
                              return 1; // Return 1 to indicate success
                            })
                            .then(
                                CommandManager.argument("assists", EntityArgumentType.players()).executes(context -> {
                                  try {
                                    // Get the arguments
                                    String target = EntityArgumentType.getEntity(context, "attack_target")
                                        .getUuid().toString();
                                    String attackerType = StringArgumentType.getString(context, "attacker_type");
                                    String attacker = null;

                                    try {
                                      attacker = EntityArgumentType.getEntity(context, "attacker").getUuidAsString();
                                    } catch (Exception e) {
                                      attacker = "none"; // Default to "none" if attacker not found
                                    } finally {
                                      if (attacker == null || attacker.isEmpty()) {
                                        context.getSource().sendError(
                                            Text.literal("Attacker cannot be empty!").withColor(0xFF0000));
                                        return 0; // Return 0 to indicate failure
                                      }
                                    }

                                    String assistsInput = null;

                                    try {
                                      assistsInput = EntityArgumentType.getPlayers(context, "assists").stream()
                                          .map(player -> player.getUuidAsString()).reduce((a, b) -> a + "," + b)
                                          .orElse("none");
                                    } catch (Exception e) {
                                      assistsInput = "none"; // Default to "none" if no assists found
                                    } finally {
                                      if (assistsInput == null || assistsInput.isEmpty()) {
                                        context.getSource().sendError(
                                            Text.literal("Assists cannot be empty!").withColor(0xFF0000));
                                        return 0; // Return 0 to indicate failure
                                      }
                                    }

                                    try {
                                      KillType.fromString(attackerType); // Validate attacker type
                                    } catch (IllegalArgumentException e) {
                                      context.getSource().sendError(
                                          Text.literal("Invalid attacker type!").withColor(0xFF0000));
                                      return 0;
                                    }

                                    // Create kill object
                                    kill killData = new kill(target,
                                        KillType.fromString(attackerType), attacker, assistsInput);

                                    // Send the kill data
                                    Payload payload = new Payload();
                                    payload.data = killData;
                                    Message wsMessage = new Message(Action.KILL, WebSocket.serverSessionId, payload);
                                    WebSocket.sendMessage(wsMessage);

                                    context.getSource().sendMessage(Text
                                        .literal("Player killed event sent successfully!").withColor(0x00FF00));
                                  } catch (Exception e) {
                                    e.printStackTrace();
                                    context.getSource().sendError(
                                        Text.literal("Failed to send player killed event!").withColor(0xFF0000));
                                  }
                                  return 1; // Return 1 to indicate success
                                }))))));
  }
}
