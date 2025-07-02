package com.uta2.events;

import com.uta2.ws.WebSocket;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class ServerLifecycleEventsHandler {
    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            System.out.println("[UTA2] Server started, connecting to WebSocket...");
            
            // Set server references for helper classes
            com.uta2.helpers.WhiteListManager.setServer(server);
            com.uta2.helpers.PlayerOnlineChecker.setServer(server);
            com.uta2.helpers.StorageNbtHelper.setServer(server);
            
            WebSocket.connect();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            System.out.println("[UTA2] Server stopping, disconnecting from WebSocket...");
            WebSocket.disconnect();
        });
    }
}
