package com.pikacnu.src;

import java.util.ArrayList;

import com.pikacnu.src.json.*;
import com.pikacnu.src.json.data.*;
import com.pikacnu.src.websocket.WebSocketClient;

public class PlayerDatabase {
  public static class PlayerData {
    public String uuid;
    public String minecraftId;
    public Integer score;
    public boolean isInParty;
    public boolean isInQueue;
    public Integer partyId;

    public PlayerData(String uuid, String minecraftId, Integer score, boolean isInParty, boolean isInQueue,
        Integer partyId) {
      this.uuid = uuid;
      this.minecraftId = minecraftId;
      this.score = score;
      this.isInParty = isInParty;
      this.isInQueue = isInQueue;
      this.partyId = partyId;
    }

    public PlayerData(String uuid) {
      this.uuid = uuid;
      this.minecraftId = null; // Default to null if not provided
      this.score = null; // Default to null if not provided
      this.isInParty = false; // Default to false if not provided
      this.isInQueue = false; // Default to false if not provided
      this.partyId = null; // Default to null if not provided
    }

    public PlayerData(String uuid, String minecraftId, Integer score) {
      this(uuid, minecraftId, score, false, false, null);
    }
  }

  public static ArrayList<PlayerData> playerList = new ArrayList<>();

  public static PlayerData getPlayerData(String uuid) {
    for (PlayerData player : playerList) {
      if (player.uuid.equals(uuid)) {
        return player;
      }
    }
    return null;
  }

  public static void addPlayerData(PlayerData playerData) {
    playerList.add(playerData);
  }

  public static void removePlayerData(String uuid) {
    playerList.removeIf(player -> player.uuid.equals(uuid));
  }

  public static void updatePlayerData(PlayerData playerData) {
    for (int i = 0; i < playerList.size(); i++) {
      if (playerList.get(i).uuid.equals(playerData.uuid)) {
        playerList.set(i, playerData);
        return;
      }
    }
    playerList.add(playerData);
  }

  public static void clear() {
    playerList.clear();
  }

  public static boolean isPlayerInQueue(String uuid) {
    for (PlayerData player : playerList) {
      if (player.uuid.equals(uuid) && player.isInQueue) {
        return true;
      }
    }
    return false;
  }

  public static boolean isPlayerInParty(String uuid) {
    for (PlayerData player : playerList) {
      if (player.uuid.equals(uuid) && player.isInParty) {
        return true;
      }
    }
    return false;
  }

  public static void updatePlayerDataFromServer(String uuid, String minecraftId) {
    Payload payload = new Payload();
    payload.player = new PlayerData(uuid, minecraftId, null);
    Message getPlayerDataMessage = new Message(Action.GET_PLAYER_DATA, WebSocketClient.serverSessionId, payload);
    WebSocketClient.sendMessage(getPlayerDataMessage);
  }
  /*
   * public static boolean isTwoPlayerScoreGapTooLarge(String uuid1, String uuid2)
   * {
   * PlayerData player1 = getPlayerData(uuid1);
   * PlayerData player2 = getPlayerData(uuid2);
   * 
   * if (player1 == null || player2 == null) {
   * throw new
   * IllegalArgumentException("One or both players not found in database.");
   * }
   * 
   * int score1 = player1.score != null ? player1.score : 0;
   * int score2 = player1.score != null ? player1.score : 0;
   * 
   * return Math.abs(score1 - score2) > 300; // Example threshold of 1000
   * }
   */
}
