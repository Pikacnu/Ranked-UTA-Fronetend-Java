package com.pikacnu.src.json.data;

import java.util.ArrayList;
import com.google.gson.JsonSyntaxException;
import com.pikacnu.src.json.JsonUtils;
import com.pikacnu.src.PartyDatabase.PartyData;
import com.pikacnu.src.PlayerDatabase.PlayerData;

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

    public LobbyData(boolean isLobby) {
      this.isLobby = isLobby;
    }
  }

  public LobbyData lobby;

  public record TeamData(String team, String[] names) {
  }

  public TeamData[] teamData;

  public record QueueData(String queueName, String uuid, ArrayList<ArrayList<PartyData>> parties) {
  }

  public QueueData queue;

  public record WhitelistEntry(String uuid, String minecraftId) {
  }

  public ArrayList<WhitelistEntry> whitelist;

  public record handshakeData(boolean isLobby, String minecraftServerIP, Integer minecraftServerPort, String serverId) {
  }

  public handshakeData handshake;

  public static class PlayerOnlineStatus {
    public ArrayList<String> uuids;

    public enum Connection {
      CONNECTED, DISCONNECTED
    }

    public Connection connection;

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

    public TransferData(String targetServer, Integer port, ArrayList<String> uuids) {
      this.targetServer = targetServer;
      this.targetPort = port;
      this.uuids = new ArrayList<>();
    }
  }

  public TransferData transferData;

  public Payload(String command, String[] commands, String message, String[] data) {
    this.command = command;
    this.commands = commands;
    this.message = message;
    this.data = data;
  }

  public Payload() {
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
