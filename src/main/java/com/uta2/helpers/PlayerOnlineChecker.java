package com.uta2.helpers;

import com.uta2.ws.WebSocket;
import net.minecraft.server.MinecraftServer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import java.util.List;
import java.util.UUID;

public class PlayerOnlineChecker {
    private static final Gson gson = new Gson();
    private static MinecraftServer server;

    public static void setServer(MinecraftServer minecraftServer) {
        server = minecraftServer;
    }

    public static boolean isPlayerOnlineLocally(String playerName) {
        if (server == null) return false;
        return server.getPlayerManager().getPlayer(playerName) != null;
    }

    public static boolean isPlayerOnlineLocally(UUID uuid) {
        if (server == null) return false;
        return server.getPlayerManager().getPlayer(uuid) != null;
    }

    public static void queryPlayerOnlineStatus(List<String> playerNames) {
        JsonObject data = new JsonObject();
        data.addProperty("action", "PLAYER_ONLINE_QUERY");
        
        JsonArray playersArray = new JsonArray();
        for (String playerName : playerNames) {
            playersArray.add(playerName);
        }
        data.add("players", playersArray);
        
        WebSocket.send(gson.toJson(data));
    }

    public static void queryPlayerOnlineStatus(String playerName) {
        JsonObject data = new JsonObject();
        data.addProperty("action", "PLAYER_ONLINE_QUERY");
        data.addProperty("player", playerName);
        
        WebSocket.send(gson.toJson(data));
    }
}
