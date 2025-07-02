package com.pikacnu.src;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WhitelistEntry;

public class WhiteListManager {
  public static MinecraftServer server;

  public static boolean isPlayerWhitelisted(String uuid) {
    return server.getPlayerManager().getWhitelist().isAllowed(
        server.getPlayerManager().getPlayer(UUID.fromString(uuid)).getGameProfile());
  }

  public static void addPlayerToWhitelist(String uuid, String name) {
    server.getPlayerManager().getWhitelist().add(new WhitelistEntry(new GameProfile(UUID.fromString(uuid), name)));
  }

  public static void addPlayerToWhitelist(WhitelistEntry entry) {
    server.getPlayerManager().getWhitelist().add(entry);
  }

  public static void removePlayerFromWhitelist(String uuid) {
    server.getPlayerManager().getWhitelist().remove(
        server.getPlayerManager().getPlayer(UUID.fromString(uuid)).getGameProfile());
  }

  public static void kickPlayerNotInWhitelist() {
    server.kickNonWhitelistedPlayers(
        server.getCommandSource());
  }

  public static void clearWhitelist() {
    server.getPlayerManager().getWhitelist().values().forEach(entry -> {
      server.getPlayerManager().getWhitelist().remove(entry);
    });
  }
}
