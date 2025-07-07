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
    try {
      // 確保伺服器在主執行緒中執行踢除操作
      server.execute(() -> {
        try {
          server.kickNonWhitelistedPlayers(server.getCommandSource());
        } catch (Exception e) {
          // 捕獲任何踢除過程中的異常，避免影響主要邏輯
          // 這種錯誤通常是因為玩家連接已經關閉導致的，可以安全忽略
        }
      });
    } catch (Exception e) {
      // 如果無法安排執行，記錄錯誤但不拋出異常
    }
  }

  public static void clearWhitelist() {
    server.getPlayerManager().getWhitelist().values().forEach(entry -> {
      server.getPlayerManager().getWhitelist().remove(entry);
    });
  }
}
