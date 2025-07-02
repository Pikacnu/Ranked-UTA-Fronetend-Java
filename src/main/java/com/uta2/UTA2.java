package com.uta2;

import com.uta2.commands.CommandInit;
import com.uta2.events.PlayerConnectionEvents;
import com.uta2.events.ServerLifecycleEventsHandler;
import net.fabricmc.api.ModInitializer;

public class UTA2 implements ModInitializer {
    @Override
    public void onInitialize() {
        // Register commands
        CommandInit.registerCommands();

        // Register event handlers
        ServerLifecycleEventsHandler.register();
        PlayerConnectionEvents.register();

        System.out.println("[UTA2] Mod initialized.");
    }
}
