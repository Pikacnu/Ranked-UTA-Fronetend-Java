package com.uta2.commands;

import com.uta2.config.Config;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CommandInit {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // Universal commands
            new WsStatusCommand().register(dispatcher);

            if (Config.isLobby) {
                // Lobby commands
                new PartyCommand().register(dispatcher);
                new QueueCommand().register(dispatcher);
                new ScoreCommand().register(dispatcher);
            } else {
                // Game commands
                new PlayerKilledCommand().register(dispatcher);
                new DamagedCommand().register(dispatcher);
                new GameStatusCommand().register(dispatcher);
                new MapChooseCommand().register(dispatcher);
                new SendDataCommand().register(dispatcher);
                new GetDataCommand().register(dispatcher);
            }
        });
    }
}
