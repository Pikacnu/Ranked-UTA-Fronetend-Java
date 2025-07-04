package com.pikacnu.src.json.data;

import java.util.ArrayList;
import com.google.gson.JsonSyntaxException;
import com.pikacnu.src.json.JsonUtils;
import com.pikacnu.src.PartyDatabase.PartyData;
import com.pikacnu.src.PlayerDatabase.PlayerData;

/**
 * 表示伺服器傳輸的有效載荷。
 */
public class Payload {
  public String command;
  public String[] commands;
  public String message;
  public Object data;
  public String request_target;
  public PlayerData player;
  public PartyData party;

  public static class LobbyData {
    public boolean isLobby;

    public LobbyData() {
    }

    public LobbyData(boolean isLobby) {
      this.isLobby = isLobby;
    }
  }

  public LobbyData lobby;

  public static class TeamData {
    public String team;
    public String[] uuids;

    public TeamData() {
    }
  }

  public TeamData[] teamData;

  public static class QueueData {
    public String queue_name;
    public String uuid;
    public ArrayList<ArrayList<PartyData>> parties;

    public QueueData() {
    }

    public QueueData(String queue_name, String uuid) {
      this.queue_name = queue_name;
      this.uuid = uuid;
    }
  }

  public QueueData queue;

  public static class WhitelistEntry {
    public String uuid;
    public String minecraftId;

    public WhitelistEntry() {
    }

    public WhitelistEntry(String uuid, String minecraftId) {
      this.uuid = uuid;
      this.minecraftId = minecraftId;
    }
  }

  public ArrayList<WhitelistEntry> whitelist;

  public static class PlayerOnlineStatus {
    public ArrayList<String> uuids;

    public static enum Connection {
      CONNECTED, DISCONNECTED
    }

    public Connection connection;

    public PlayerOnlineStatus() {
    }

    public PlayerOnlineStatus(ArrayList<String> uuids, Connection connection) {
      this.uuids = uuids;
      this.connection = connection;
    }
  }

  public PlayerOnlineStatus playerOnlineStatus;

  public static class TransferData {
    public String targetServer;
    public Integer targetPort;
    public ArrayList<String> uuids;

    public TransferData() {
    }

    public TransferData(String targetServer, Integer port, ArrayList<String> uuids) {
      this.targetServer = targetServer;
      this.targetPort = port;
      this.uuids = new ArrayList<>();
    }
  }

  public TransferData transferData;

  public Payload() {
  }

  public Payload(String command, String[] commands, String message, String[] data) {
    this.command = command;
    this.commands = commands;
    this.message = message;
    this.data = data;
  }

  public Payload(String command) {
    this(command, null, null, null);
  }

  public Payload(String[] commands) {
    this(null, commands, null, null);
  }

  public Payload(String message, String[] data) {
    this(null, null, message, data);
  }

  /**
   * 從 JSON 字串解析 Payload
   */
  public static Payload fromJson(String jsonString) {
    try {
      return JsonUtils.getGson().fromJson(jsonString, Payload.class);
    } catch (JsonSyntaxException e) {
      throw new IllegalArgumentException("Invalid JSON format for Payload: " + e.getMessage(), e);
    }
  }

  /**
   * 轉換為 JSON 字串
   */
  public String toJson() {
    return JsonUtils.getGson().toJson(this);
  }

  /**
   * 從 JSON 字串解析並返回 null 如果失敗
   */
  public static Payload fromJsonOrNull(String jsonString) {
    try {
      return JsonUtils.getGson().fromJson(jsonString, Payload.class);
    } catch (JsonSyntaxException e) {
      return null;
    }
  }
}
