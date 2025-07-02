package com.uta2.helpers;

import net.minecraft.server.MinecraftServer;

public class WhiteListManager {
    private static MinecraftServer server;

    public static void setServer(MinecraftServer minecraftServer) {
        server = minecraftServer;
    }

    public static boolean add(String playerName) {
        if (server == null) return false;
        
        try {
            // Add to whitelist
            var gameProfile = server.getUserCache().findByName(playerName);
            if (gameProfile.isPresent()) {
                server.getPlayerManager().getWhitelist().add(
                    new net.minecraft.server.WhitelistEntry(gameProfile.get())
                );
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("[UTA2] Failed to add player to whitelist: " + e.getMessage());
            return false;
        }
    }

    public static boolean remove(String playerName) {
        if (server == null) return false;
        
        try {
            // Remove from whitelist
            var gameProfile = server.getUserCache().findByName(playerName);
            if (gameProfile.isPresent()) {
                server.getPlayerManager().getWhitelist().remove(gameProfile.get());
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("[UTA2] Failed to remove player from whitelist: " + e.getMessage());
            return false;
        }
    }

    public static boolean isAllowed(String playerName) {
        if (server == null) return false;
        
        try {
            var gameProfile = server.getUserCache().findByName(playerName);
            if (gameProfile.isPresent()) {
                return server.getPlayerManager().getWhitelist().isAllowed(gameProfile.get());
            }
            return false;
        } catch (Exception e) {
            System.err.println("[UTA2] Failed to check whitelist status: " + e.getMessage());
            return false;
        }
    }
}
