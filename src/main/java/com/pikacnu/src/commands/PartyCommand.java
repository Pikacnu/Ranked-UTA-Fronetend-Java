package com.pikacnu.src.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import com.pikacnu.UTA2;
import com.pikacnu.src.PartyDatabase;
import com.pikacnu.src.PartyDatabase.PartyData;
import com.pikacnu.src.PartyDatabase.PartyInvition;

public class PartyCommand implements ICommand {
  @Override
  public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    dispatcher.register(
        CommandManager.literal("party")
            .then(
                CommandManager.argument("action", StringArgumentType.string())
                    .suggests((context, builder) -> {
                      String[] actions = { "leave", "invite", "accept", "reject", "list", "disband" };
                      for (String action : actions) {
                        builder.suggest(action);
                      }
                      return builder.buildFuture();
                    })
                    .executes(context -> {
                      String action = StringArgumentType.getString(context, "action");
                      if (action == null || action.isEmpty()) {
                        context.getSource()
                            .sendError(Text.literal("Action cannot be empty!").withColor(0xFF0000));
                        return 0;
                      }

                      String playerUuid = context.getSource().getPlayer().getUuidAsString();
                      PartyDatabase.PartyResultMessage message = null;

                      switch (action) {
                        case "leave":
                          PartyData party = PartyDatabase.getPartyData(playerUuid);
                          if (party == null) {
                            context.getSource()
                                .sendError(Text.literal("You are not in a party!").withColor(0xFF0000));
                            return 0;
                          }
                          message = party.removePlayer(playerUuid);
                          break;
                        case "list":
                          party = PartyDatabase.getPartyData(playerUuid);
                          if (party == null) {
                            context.getSource()
                                .sendError(Text.literal("You are not in a party!").withColor(0xFF0000));
                            return 0;
                          }
                          StringBuilder partyList = new StringBuilder("Party Members: ");
                          synchronized (party.partyMembers) {
                            for (PartyDatabase.PartyPlayer member : party.partyMembers) {
                              partyList.append(member.minecraftId).append(", ");
                            }
                          }
                          context.getSource()
                              .sendMessage(Text.literal(partyList.toString()).withColor(0x00FF00));
                          return 1;
                        case "disband":
                          party = PartyDatabase.getPartyData(playerUuid);
                          if (party == null) {
                            context.getSource()
                                .sendError(Text.literal("You are not in a party!").withColor(0xFF0000));
                            return 0;
                          }
                          party.disbandParty();
                          message = PartyDatabase.PartyResultMessage.PARTY_DISBANDED;
                          break;
                        default:
                          context.getSource()
                              .sendError(Text.literal("Invalid action! Use 'leave' without target.")
                                  .withColor(0xFF0000));
                          return 0;
                      }
                      if (message == null) {
                        context.getSource()
                            .sendError(Text.literal("Failed to execute party action!").withColor(0xFF0000));
                        return 0;
                      }

                      context.getSource()
                          .sendMessage(Text.literal(message.getMessage()).withColor(0x00FF00));
                      return 1;
                    })
                    .then(CommandManager.argument("target", EntityArgumentType.player())
                        .executes(context -> {
                          String action = StringArgumentType.getString(context, "action");
                          String target = EntityArgumentType.getPlayer(context, "target").getUuidAsString();
                          if (action == null || action.isEmpty() || target == null || target.isEmpty()) {
                            context.getSource()
                                .sendError(Text.literal("Action and target cannot be empty!").withColor(0xFF0000));
                            return 0;
                          }

                          String playerUuid = context.getSource().getPlayer().getUuidAsString();
                          PartyDatabase.PartyResultMessage message = null;

                          switch (action) {
                            case "invite":
                              PartyInvition invitation = PartyDatabase.getInvitation(playerUuid);
                              if (invitation != null) {
                                context.getSource()
                                    .sendError(
                                        Text.literal("You have already received an invitation. Please reject it first.")
                                            .withColor(0xFF0000));
                                return 0;
                              }

                              if (target.equals(playerUuid)) {
                                context.getSource()
                                    .sendError(Text.literal("You cannot invite yourself!").withColor(0xFF0000));
                                return 0;
                              }

                              PartyData targetParty = PartyDatabase.getPartyData(target);
                              if (targetParty != null) {
                                context.getSource()
                                    .sendError(Text.literal("Player is already in a party!").withColor(0xFF0000));
                                return 0;
                              }

                              PartyData party = PartyDatabase.getPartyData(playerUuid);
                              if (party == null) {
                                party = PartyDatabase.createParty(playerUuid);
                                if (party == null) {
                                  context.getSource()
                                      .sendError(Text.literal("Failed to create party!").withColor(0xFF0000));
                                  return 0;
                                }
                              }
                              if (party.isInQueue) {
                                context.getSource()
                                    .sendError(Text.literal("You can't invite while in queue.").withColor(0xFF0000));
                              }
                              if (!party.isPartyLeader(playerUuid)) {
                                context.getSource()
                                    .sendError(Text.literal("You must be the party leader to invite players.")
                                        .withColor(0xFF0000));
                                return 0;
                              }
                              message = PartyDatabase.createInvitation(party.partyId, playerUuid, target);
                              break;
                            case "accept":
                              message = PartyDatabase.AcceptInvitation(playerUuid, target);
                              break;
                            case "reject":
                              message = PartyDatabase.RejectInvitation(playerUuid, target);
                              break;
                            default:
                              context.getSource()
                                  .sendError(Text
                                      .literal(
                                          "Invalid action! Use 'join', 'invite', 'accept', or 'reject' with target.")
                                      .withColor(0xFF0000));
                              return 0;
                          }
                          PartyDatabase.partyList.forEach(partyData -> {
                            UTA2.LOGGER.info("Party ID: " + partyData.partyId + ", Members: "
                                + partyData.partyMembers.size());
                          });
                          PartyDatabase.partyInvitions.forEach(inv -> {
                            UTA2.LOGGER.info("Invitation: " + inv.inviterUuid + " -> " + inv.targetUuid);
                          });

                          if (message == null) {
                            context.getSource()
                                .sendError(Text.literal("Failed to execute party action!").withColor(0xFF0000));
                            return 0;
                          }

                          context.getSource()
                              .sendMessage(Text.literal(message.getMessage()).withColor(0x00FF00));
                          return 1;
                        }))));
  }
}
