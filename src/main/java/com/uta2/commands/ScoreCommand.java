package com.uta2.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.uta2.data.PlayerDatabase;
import com.uta2.data.PlayerData;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

public class ScoreCommand implements ICommand {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("score")
            .requires(source -> source.hasPermissionLevel(0))
            .executes(context -> {
                ServerCommandSource source = context.getSource();
                
                if (source.getEntity() instanceof ServerPlayerEntity player) {
                    PlayerData playerData = PlayerDatabase.getPlayer(player.getUuid());
                    
                    if (playerData != null) {
                        source.sendFeedback(() -> 
                            Text.literal("你的分數: ")
                                .append(Text.literal(String.valueOf(playerData.getScore())).formatted(Formatting.YELLOW)), 
                            false);
                    } else {
                        source.sendFeedback(() -> 
                            Text.literal("找不到你的分數資料").formatted(Formatting.RED), 
                            false);
                    }
                } else {
                    source.sendFeedback(() -> 
                        Text.literal("只有玩家可以執行此指令").formatted(Formatting.RED), 
                        false);
                }
                
                return 1;
            })
        );
    }
}
