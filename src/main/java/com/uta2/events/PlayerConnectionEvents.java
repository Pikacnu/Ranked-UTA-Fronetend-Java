package com.uta2.events;

import com.uta2.data.PlayerDatabase;
import com.uta2.ws.WebSocket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class PlayerConnectionEvents {
    private static final Gson gson = new Gson();
    
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            // Add player to local database
            PlayerDatabase.addPlayer(new com.uta2.data.PlayerData(handler.player.getUuid(), handler.player.getName().getString()));

            // Notify backend
            JsonObject data = new JsonObject();
            data.addProperty("action", "PLAYER_JOIN");
            data.addProperty("uuid", handler.player.getUuid().toString());
            data.addProperty("name", handler.player.getName().getString());
            WebSocket.send(gson.toJson(data));
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            // Remove player from local database
            PlayerDatabase.removePlayer(handler.player.getUuid());

            // Notify backend
            JsonObject data = new JsonObject();
            data.addProperty("action", "PLAYER_LEAVE");
            data.addProperty("uuid", handler.player.getUuid().toString());
            WebSocket.send(gson.toJson(data));
        });
    }
}
