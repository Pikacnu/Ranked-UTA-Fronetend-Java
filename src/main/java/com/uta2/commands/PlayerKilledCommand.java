package com.uta2.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.uta2.data.KillType;
import com.uta2.ws.WebSocket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PlayerKilledCommand implements ICommand {
    private static final Gson gson = new Gson();

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("player_killed")
            .requires(source -> source.hasPermissionLevel(2))
            .then(argument("killer", StringArgumentType.string())
                .then(argument("victim", StringArgumentType.string())
                    .then(argument("kill_type", StringArgumentType.string())
                        .executes(context -> executePlayerKilled(
                            context.getSource(),
                            StringArgumentType.getString(context, "killer"),
                            StringArgumentType.getString(context, "victim"),
                            StringArgumentType.getString(context, "kill_type")
                        )))))
        );
    }

    private int executePlayerKilled(ServerCommandSource source, String killerName, String victimName, String killTypeStr) {
        // Validate kill type
        KillType killType = KillType.fromString(killTypeStr);
        
        // Send data to backend
        JsonObject data = new JsonObject();
        data.addProperty("action", "PLAYER_KILLED");
        data.addProperty("killer", killerName);
        data.addProperty("victim", victimName);
        data.addProperty("killType", killType.name());
        data.addProperty("timestamp", System.currentTimeMillis());
        
        WebSocket.send(gson.toJson(data));

        // Send confirmation
        source.sendFeedback(() -> 
            Text.literal("擊殺記錄已發送: " + killerName + " -> " + victimName + " (" + killType.getDisplayName() + ")")
                .formatted(Formatting.GREEN), false);

        return 1;
    }
}
