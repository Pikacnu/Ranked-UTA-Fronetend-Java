package com.pikacnu.src;

import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;

import com.pikacnu.UTA2;
import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.data.Message;
import com.pikacnu.src.json.data.Payload;
import com.pikacnu.src.json.data.Payload.PlayerOnlineStatus;
import com.pikacnu.src.json.data.Payload.PlayerOnlineStatus.Connection;
import com.pikacnu.src.websocket.WebSocketClient;

public class PlayerOnlineChecker {

  public static class sendQueryEntry {
    public String uuid;
    public long time;
    public Connection connection;

    public sendQueryEntry(String uuid, long time, Connection connection) {
      this.uuid = uuid;
      this.time = time;
      this.connection = connection;
    }
  }

  public static ArrayList<String> playerList = new ArrayList<>();
  public static ArrayList<sendQueryEntry> sendQueryList = new ArrayList<>();
  public static ScheduledExecutorService scheduler = java.util.concurrent.Executors.newScheduledThreadPool(1);

  public static void send(String uuid, Connection connection) {
    ArrayList<String> players = new ArrayList<>();
    players.add(uuid);

    Payload payload = new Payload();
    payload.playerOnlineStatus = new PlayerOnlineStatus(players, connection);
    Message message = new Message(Action.player_online_status, WebSocketClient.serverSessionId, payload);
    WebSocketClient.sendMessage(message);
  }

  public static void addPlayer(String uuid) {
    UTA2.LOGGER.info("Adding player to online list: " + uuid);
    if (isPlayerOnline(uuid)) {
      return; // Player is already online
    }
    send(uuid, Connection.CONNECTED);
    playerList.add(uuid);
  }

  public static void removePlayer(String uuid) {
    UTA2.LOGGER.info("Removing player from online list: " + uuid);
    playerList.remove(uuid);
    send(uuid, Connection.DISCONNECTED);
  }

  public static boolean isPlayerOnline(String uuid) {
    if (playerList.contains(uuid)) {
      return true;
    }
    return false;
  }

}
