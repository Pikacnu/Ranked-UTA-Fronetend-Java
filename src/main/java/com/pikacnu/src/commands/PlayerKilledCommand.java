package com.pikacnu.src.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pikacnu.UTA2;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import com.mojang.brigadier.arguments.StringArgumentType;

import com.pikacnu.src.websocket.WebSocketClient;
import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.KillType;
import com.pikacnu.src.json.data.Message;
import com.pikacnu.src.json.data.Payload;
import com.pikacnu.src.json.data.Kill;

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
                            builder.suggest(type.toString().toLowerCase());
                          }
                          return builder.buildFuture();
                        })
                        .then(CommandManager.argument("attacker", EntityArgumentType.player())
                            .executes(new AttackerArgument())
                            .then(
                                CommandManager.argument("assists", EntityArgumentType.players()).executes(new AssistsArgument()))))));
  }

  private static class AttackerArgument implements Command<ServerCommandSource>
  {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
    {
      try
      {
        // Get the arguments
        String target = EntityArgumentType.getEntity(context, "attack_target").getUuid().toString();
        String attackerType = StringArgumentType.getString(context, "attacker_type");
        String attacker;
        try
        {
          attacker = EntityArgumentType.getEntity(context, "attacker").getUuidAsString();
        }
        catch (Exception e)
        {
          attacker = "none"; // Default to "none" if attacker not found
        }

        if (attacker == null || attacker.isEmpty())
        {
          context.getSource().sendError(Text.literal("Attacker cannot be empty!").withColor(0xFF0000));
          return 0; // Return 0 to indicate failure
        }

        KillType killType;
        try
        {
          killType = KillType.fromString(attackerType); // Validate attacker type
        }
        catch (IllegalArgumentException e)
        {
          context.getSource().sendError(Text.literal("Invalid attacker type!").withColor(0xFF0000));
          return 0;
        }

        // Create kill object
        Kill killData = new Kill(target, killType, attacker);

        // Send the kill data
        Payload payload = new Payload();
        payload.data = killData;
        Message wsMessage = new Message(Action.kill, WebSocketClient.serverSessionId, payload);
        WebSocketClient.sendMessage(wsMessage);

        context.getSource().sendMessage(Text
                .literal("Player killed event sent successfully!").withColor(0x00FF00));
      }
      catch (Exception e)
      {
        context.getSource().sendError(Text.literal("Failed to send player killed event!").withColor(0xFF0000));
        UTA2.LOGGER.error("Failed to send player killed event!", e);
      }
      return SINGLE_SUCCESS; // Return 1 to indicate success
    }
  }

  public static class AssistsArgument implements Command<ServerCommandSource>
  {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
    {
      try
      {
        // Get the arguments
        String target = EntityArgumentType.getEntity(context, "attack_target").getUuid().toString();
        String attackerType = StringArgumentType.getString(context, "attacker_type");
        String attacker;

        try
        {
          attacker = EntityArgumentType.getEntity(context, "attacker").getUuidAsString();
        }
        catch (Exception e)
        {
          attacker = "none"; // Default to "none" if attacker not found
        }
        if (attacker == null || attacker.isEmpty())
        {
          context.getSource().sendError( Text.literal("Attacker cannot be empty!").withColor(0xFF0000));
          return 0; // Return 0 to indicate failure
        }

        String assistsInput;
        try
        {
          assistsInput = EntityArgumentType.getPlayers(context, "assists")
                  .stream()
                  .map(Entity::getUuidAsString)
                  .reduce((a, b) -> a + "," + b)
                  .orElse("none");
        }
        catch (Exception e)
        {
          assistsInput = "none"; // Default to "none" if no assists found
        }
        if (assistsInput.isEmpty())
        {
          context.getSource().sendError( Text.literal("Assists cannot be empty!").withColor(0xFF0000));
          return 0; // Return 0 to indicate failure
        }

        KillType killType;
        try
        {
          killType = KillType.fromString(attackerType); // Validate attacker type
        }
        catch (IllegalArgumentException e)
        {
          context.getSource().sendError(
                  Text.literal("Invalid attacker type!").withColor(0xFF0000));
          return 0;
        }

        // Create kill object
        Kill killData = new Kill(target, killType, attacker, assistsInput);

        // Send the kill data
        Payload payload = new Payload();
        payload.data = killData;
        Message wsMessage = new Message(Action.kill, WebSocketClient.serverSessionId,
                payload);
        WebSocketClient.sendMessage(wsMessage);

        context.getSource().sendMessage(Text.literal("Player killed event sent successfully!").withColor(0x00FF00));
      }
      catch (Exception e)
      {
        context.getSource().sendError(Text.literal("Failed to send player killed event!").withColor(0xFF0000));
        UTA2.LOGGER.error("Failed to send player killed event!", e);
      }
      return SINGLE_SUCCESS; // Return 1 to indicate success
    }
  }
}
