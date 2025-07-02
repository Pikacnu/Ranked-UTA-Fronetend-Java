package com.uta2.data;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDatabase {
    private static final ConcurrentHashMap<UUID, PlayerData> players = new ConcurrentHashMap<>();

    public static PlayerData getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public static void addPlayer(PlayerData playerData) {
        players.put(playerData.getUuid(), playerData);
    }

    public static void removePlayer(UUID uuid) {
        players.remove(uuid);
    }

    public static void updatePlayer(PlayerData playerData) {
        players.put(playerData.getUuid(), playerData);
    }

    public static ConcurrentHashMap<UUID, PlayerData> getPlayers() {
        return players;
    }
}
