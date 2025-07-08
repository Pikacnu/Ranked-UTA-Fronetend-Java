package com.pikacnu.src;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.pikacnu.UTA2;

import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ActionBarController {
  private static ScheduledExecutorService actionBarExecutor;
  private static boolean isActionBarEnabled = true;

  public static class ActionBarData {
    public Text message;
    public String playerUUID;

    public ActionBarData(Text message, String playerUUID) {
      this.message = message;
      this.playerUUID = playerUUID;
    }

  }

  private static ArrayList<ActionBarData> actionBarQueue = new ArrayList<>();
  private static MinecraftServer server;

  public static void initialize(MinecraftServer serverInstance) {
    actionBarExecutor = Executors.newScheduledThreadPool(1);
    server = serverInstance;
    actionBarExecutor.scheduleAtFixedRate(() -> {
      if (isActionBarEnabled && !actionBarQueue.isEmpty()) {
        actionBarQueue.forEach(data -> {
          Text message = data.message;
          String playerUUID = data.playerUUID;
          ServerPlayerEntity player = server.getPlayerManager().getPlayer(UUID.fromString(playerUUID));
          OverlayMessageS2CPacket packet = new OverlayMessageS2CPacket(message);
          try {
            player.networkHandler.sendPacket(packet);
          } catch (Exception e) {
            UTA2.LOGGER.error("Failed to send action bar message to player {}: {}", playerUUID, e.getMessage());
          }

        });
      }
    }, 0, 1, java.util.concurrent.TimeUnit.SECONDS);
  }

  public static void shutdown() {
    if (actionBarExecutor != null && !actionBarExecutor.isShutdown()) {
      actionBarExecutor.shutdown();
    }
    actionBarQueue.clear();
  }

  public static void addActionBarMessage(Text message, String playerUUID) {
    if (isActionBarEnabled) {
      actionBarQueue.add(new ActionBarData(message, playerUUID));
    }
  }

  public static void removeActionBarMessage(String playerUUID) {
    actionBarQueue.removeIf(data -> data.playerUUID.equals(playerUUID));
  }

  public static void setActionBarEnabled(boolean enabled) {
    isActionBarEnabled = enabled;
    if (!enabled) {
      actionBarQueue.clear(); // Clear the queue when disabling
    }
  }

  public static void clearActionBarMessages() {
    actionBarQueue.clear();
  }
}
