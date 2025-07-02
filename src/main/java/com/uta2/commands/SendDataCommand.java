package com.uta2.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.uta2.helpers.StorageNbtHelper;
import com.uta2.ws.WebSocket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SendDataCommand implements ICommand {
    private static final Gson gson = new Gson();

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("send_data")
            .requires(source -> source.hasPermissionLevel(2))
            .then(argument("type", StringArgumentType.string())
                .then(argument("storage", StringArgumentType.string())
                    .then(argument("nbt", StringArgumentType.greedyString())
                        .executes(context -> executeSendData(
                            context.getSource(),
                            StringArgumentType.getString(context, "type"),
                            StringArgumentType.getString(context, "storage"),
                            StringArgumentType.getString(context, "nbt")
                        )))))
        );
    }

    private int executeSendData(ServerCommandSource source, String type, String storage, String nbtPath) {
        try {
            // Read NBT data
            String nbtData = StorageNbtHelper.readNbtData(storage, nbtPath);
            
            if (nbtData == null) {
                source.sendFeedback(() -> 
                    Text.literal("無法從 " + storage + " 讀取 NBT 路徑: " + nbtPath).formatted(Formatting.RED), false);
                return 0;
            }

            // Send data to backend
            JsonObject data = new JsonObject();
            data.addProperty("action", "SEND_DATA");
            data.addProperty("type", type);
            data.addProperty("storage", storage);
            data.addProperty("nbtPath", nbtPath);
            data.addProperty("nbtData", nbtData);
            data.addProperty("timestamp", System.currentTimeMillis());
            
            WebSocket.send(gson.toJson(data));

            // Send confirmation
            source.sendFeedback(() -> 
                Text.literal("已發送 " + type + " 數據到後端").formatted(Formatting.GREEN), false);

            return 1;
        } catch (Exception e) {
            source.sendFeedback(() -> 
                Text.literal("發送數據時發生錯誤: " + e.getMessage()).formatted(Formatting.RED), false);
            return 0;
        }
    }
}
