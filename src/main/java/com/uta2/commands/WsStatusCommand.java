package com.uta2.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.uta2.ws.WebSocket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

public class WsStatusCommand implements ICommand {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("ws_status")
            .requires(source -> source.hasPermissionLevel(2))
            .executes(context -> {
                ServerCommandSource source = context.getSource();
                
                if (WebSocket.isConnected()) {
                    source.sendFeedback(() -> 
                        Text.literal("WebSocket Status: ")
                            .append(Text.literal("Connected").formatted(Formatting.GREEN)), 
                        false);
                } else {
                    source.sendFeedback(() -> 
                        Text.literal("WebSocket Status: ")
                            .append(Text.literal("Disconnected").formatted(Formatting.RED)), 
                        false);
                }
                
                return 1;
            })
        );
    }
}
