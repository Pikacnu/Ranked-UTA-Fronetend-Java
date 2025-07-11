package com.pikacnu.src;

import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.data.*;
import com.pikacnu.src.PlayerDatabase.PlayerData;
import com.pikacnu.src.websocket.WebSocketClient;

public class PlayerSettingsDatabase {
  public static class PlayerSettingsEntry {
    public String uuid;
    public int N;
    public int Q;
    public int S;
    public int U;
    public int B;

    public PlayerSettingsEntry(String uuid, int N, int Q, int S, int U, int B) {
      this.uuid = uuid;
      this.N = N;
      this.Q = Q;
      this.S = S;
      this.U = U;
      this.B = B;
    }
  }
  /*
   * private static final ArrayList<PlayerSettingsEntry> playerSettings = new
   * ArrayList<>();
   * 
   * 
   * public static void addPlayerSettings(String uuid, int N, int Q, int S, int U,
   * int B) {
   * PlayerSettingsEntry entry = new PlayerSettingsEntry(uuid, N, Q, S, U, B);
   * playerSettings.add(entry);
   * }
   * public static PlayerSettingsEntry getPlayerSettings(String uuid) {
   * for (PlayerSettingsEntry entry : playerSettings) {
   * if (entry.uuid.equals(uuid)) {
   * return entry;
   * }
   * }
   * return null; // 如果找不到，返回 null
   * }
   * public static void updatePlayerSettings(String uuid, int N, int Q, int S, int
   * U, int B) {
   * PlayerSettingsEntry entry = getPlayerSettings(uuid);
   * if (entry != null) {
   * entry.N = N;
   * entry.Q = Q;
   * entry.S = S;
   * entry.U = U;
   * entry.B = B;
   * } else {
   * addPlayerSettings(uuid, N, Q, S, U, B);
   * }
   * }
   * 
   * public static void removePlayerSettings(String uuid) {
   * playerSettings.removeIf(entry -> entry.uuid.equals(uuid));
   * }
   */

  public static void getPlayerSettingsFromServer(String uuid) {
    Payload payload = new Payload();
    PlayerData playerData = new PlayerData(uuid);
    payload.player = playerData;
    WebSocketClient.sendMessage(new Message(Action.get_player_settings, payload));
  }
}
