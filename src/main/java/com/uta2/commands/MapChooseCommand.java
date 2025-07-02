package com.uta2.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.uta2.ws.WebSocket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class MapChooseCommand implements ICommand {
    private static final Gson gson = new Gson();

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("map_choose")
            .requires(source -> source.hasPermissionLevel(2))
            .then(argument("map_index", IntegerArgumentType.integer(0))
                .executes(context -> executeMapChoose(
                    context.getSource(),
                    IntegerArgumentType.getInteger(context, "map_index")
                )))
        );
    }

    private int executeMapChoose(ServerCommandSource source, int mapIndex) {
        // Send data to backend
        JsonObject data = new JsonObject();
        data.addProperty("action", "MAP_CHOOSE");
        data.addProperty("mapIndex", mapIndex);
        data.addProperty("timestamp", System.currentTimeMillis());
        
        WebSocket.send(gson.toJson(data));

        // Send confirmation
        source.sendFeedback(() -> 
            Text.literal("已選擇地圖: " + mapIndex).formatted(Formatting.GREEN), false);

        return 1;
    }
}
