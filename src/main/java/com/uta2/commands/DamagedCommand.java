package com.uta2.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.uta2.ws.WebSocket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DamagedCommand implements ICommand {
    private static final Gson gson = new Gson();

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("damaged")
            .requires(source -> source.hasPermissionLevel(2))
            .then(argument("player", StringArgumentType.string())
                .then(argument("objective", StringArgumentType.string())
                    .then(argument("source", StringArgumentType.string())
                        .executes(context -> executeDamaged(
                            context.getSource(),
                            StringArgumentType.getString(context, "player"),
                            StringArgumentType.getString(context, "objective"),
                            StringArgumentType.getString(context, "source")
                        )))))
        );
    }

    private int executeDamaged(ServerCommandSource source, String playerName, String objective, String damageSource) {
        try {
            // For now, we'll use a simplified approach and just send the data
            // The actual damage value would need to be retrieved differently
            int damageValue = 0; // Placeholder - would need proper scoreboard access
            
            // Send data to backend
            JsonObject data = new JsonObject();
            data.addProperty("action", "DAMAGE");
            data.addProperty("player", playerName);
            data.addProperty("damage", damageValue);
            data.addProperty("source", damageSource);
            data.addProperty("objective", objective);
            data.addProperty("timestamp", System.currentTimeMillis());
            
            WebSocket.send(gson.toJson(data));

            // Send confirmation
            source.sendFeedback(() -> 
                Text.literal("傷害記錄已發送: " + playerName + " (目標: " + objective + ", 來源: " + damageSource + ")")
                    .formatted(Formatting.GREEN), false);

            return 1;
        } catch (Exception e) {
            source.sendFeedback(() -> 
                Text.literal("傷害記錄發送失敗: " + e.getMessage()).formatted(Formatting.RED), false);
            return 0;
        }
    }
}
