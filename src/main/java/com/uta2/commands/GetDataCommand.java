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

public class GetDataCommand implements ICommand {
    private static final Gson gson = new Gson();

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("get_data")
            .requires(source -> source.hasPermissionLevel(2))
            .then(argument("type", StringArgumentType.string())
                .then(argument("function", StringArgumentType.string())
                    .executes(context -> executeGetData(
                        context.getSource(),
                        StringArgumentType.getString(context, "type"),
                        StringArgumentType.getString(context, "function")
                    ))))
        );
    }

    private int executeGetData(ServerCommandSource source, String type, String function) {
        try {
            // Send request to backend
            JsonObject data = new JsonObject();
            data.addProperty("action", "GET_DATA");
            data.addProperty("type", type);
            data.addProperty("function", function);
            data.addProperty("timestamp", System.currentTimeMillis());
            
            WebSocket.send(gson.toJson(data));

            // Send confirmation
            source.sendFeedback(() -> 
                Text.literal("已向後端請求 " + type + " 數據，將透過 " + function + " function 執行結果")
                    .formatted(Formatting.GREEN), false);

            return 1;
        } catch (Exception e) {
            source.sendFeedback(() -> 
                Text.literal("請求數據時發生錯誤: " + e.getMessage()).formatted(Formatting.RED), false);
            return 0;
        }
    }
}
