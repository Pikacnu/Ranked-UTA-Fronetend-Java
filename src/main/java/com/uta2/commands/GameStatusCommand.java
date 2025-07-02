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

public class GameStatusCommand implements ICommand {
    private static final Gson gson = new Gson();
    private static final String[] STATUS_NAMES = {
        "等待中", "準備中", "進行中", "暫停", "結束", "重置", "錯誤"
    };

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("game_status")
            .requires(source -> source.hasPermissionLevel(2))
            .then(argument("status", IntegerArgumentType.integer(0, 6))
                .executes(context -> executeGameStatus(
                    context.getSource(),
                    IntegerArgumentType.getInteger(context, "status")
                )))
        );
    }

    private int executeGameStatus(ServerCommandSource source, int status) {
        // Validate status
        if (status < 0 || status >= STATUS_NAMES.length) {
            source.sendFeedback(() -> 
                Text.literal("無效的遊戲狀態代碼。有效範圍: 0-" + (STATUS_NAMES.length - 1))
                    .formatted(Formatting.RED), false);
            return 0;
        }

        // Send data to backend
        JsonObject data = new JsonObject();
        data.addProperty("action", "GAME_STATUS");
        data.addProperty("status", status);
        data.addProperty("statusName", STATUS_NAMES[status]);
        data.addProperty("timestamp", System.currentTimeMillis());
        
        WebSocket.send(gson.toJson(data));

        // Send confirmation
        source.sendFeedback(() -> 
            Text.literal("遊戲狀態已更新為: " + STATUS_NAMES[status] + " (" + status + ")")
                .formatted(Formatting.GREEN), false);

        return 1;
    }
}
