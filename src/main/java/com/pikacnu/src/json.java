package com.pikacnu.src;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.pikacnu.src.PartyDatabase.PartyData;
import com.pikacnu.src.PlayerDatabase.PlayerData;

/**
 * JSON 處理工具類，負責序列化與反序列化遊戲資料。
 */
public class json {

  /**
   * Gson 實例，用於 JSON 處理。
   */
  private static final Gson gson = new Gson();

  /**
   * 定義所有支援的動作類型。
   */
  public static enum Action {
    CONNECT("connect"),
    DISCONNECT("disconnect"),
    COMMAND("command"),
    ERROR("error"),
    KILL("kill"),
    DAMAGE("damage"),
    GAME_STATUS("game_status"),
    MAP_CHOOSE("map_choose"),
    STORAGE_DATA("storage_data"),
    CLIENT_ID("clientId"),
    TEAM_INFO("team_info"),
    TEAM_JOIN("team_join"),
    HEARTBEAT("heartbeat"),
    HANDSHAKE("handshake"),
    REQUEST_DATA("request_data"),
    GET_PLAYER_DATA("get_player_data"),
    PARTY("party"),
    PARTY_DISBANDED("party_disbanded"),
    QUEUE("queue"),
    QUEUE_LEAVE("queue_leave"),
    QUEUE_MATCH("queue_match"),
    WHILELIST("whitelist"),
    WHILELIST_CHANGE("whitelist_change"),
    WHILELIST_REMOVE("whitelist_remove"),
    WHILELIST_CHECK("whitelist_check"),
    TRANSFER("transfer"),
    PLAYER_ONLINE_STATUS("player_online_status"),
    ;

    /**
     * 動作對應的字串值。
     */
    private final String stringValue;

    Action(String stringValue) {
      this.stringValue = stringValue;
    }

    /**
     * 取得動作字串。
     */
    public String getString() {
      return stringValue;
    }

    // 從字串獲取對應的 enum 值（不區分大小寫）
    public static Action fromString(String text) {
      if (text != null) {
        for (Action action : Action.values()) {
          if (action.stringValue.equalsIgnoreCase(text)) {
            return action;
          }
        }
      }
      throw new IllegalArgumentException("No enum constant for action: " + text);
    }

    // 從字串獲取對應的 enum 值，如果找不到則返回 null
    public static Action fromStringOrNull(String text) {
      if (text != null) {
        for (Action action : Action.values()) {
          if (action.stringValue.equalsIgnoreCase(text)) {
            return action;
          }
        }
      }
      return null;
    }

    @Override
    public String toString() {
      return stringValue;

    }
  }

  /**
   * 定義所有可能的狀態。
   */
  public static enum Status {
    SUCCESS(1),
    ERROR(0);

    private final int value;

    Status(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }

    // 從數值獲取對應的 enum 值
    public static Status fromValue(int value) {
      for (Status status : Status.values()) {
        if (status.value == value) {
          return status;
        }
      }
      throw new IllegalArgumentException("No enum constant for value: " + value);
    }

    // 從數值獲取對應的 enum 值，如果找不到則返回 null
    public static Status fromValueOrNull(int value) {
      for (Status status : Status.values()) {
        if (status.value == value) {
          return status;
        }
      }
      return null;
    }
  }

  /**
   * 定義所有可能的擊殺類型。
   */
  public static enum KillType {
    PLAYER("player"),
    SPARE("spare"),
    VOID("void"),
    MELT("melt");

    private final String stringValue;

    KillType(String stringValue) {
      this.stringValue = stringValue;
    }

    public String getString() {
      return stringValue;
    }

    // 從字串獲取對應的 enum 值（不區分大小寫）
    public static KillType fromString(String text) {
      if (text != null) {
        for (KillType type : KillType.values()) {
          if (type.stringValue.equalsIgnoreCase(text)) {
            return type;
          }
        }
      }
      throw new IllegalArgumentException("No enum constant for string: " + text);
    }

    // 從字串獲取對應的 enum 值，如果找不到則返回 null
    public static KillType fromStringOrNull(String text) {
      if (text != null) {
        for (KillType type : KillType.values()) {
          if (type.stringValue.equalsIgnoreCase(text)) {
            return type;
          }
        }
      }
      return null;
    }

    @Override
    public String toString() {
      return stringValue;
    }
  }

  /**
   * 表示一次擊殺事件的資料結構。
   */
  public static class kill {
    public String target;
    public KillType type;
    public String attacker;
    public String assists;

    public kill() {
    }

    public kill(String target, KillType type, String attacker, String assists) {
      this.target = target;
      this.type = type;
      this.attacker = attacker;
      this.assists = assists;
    }

    public kill(String target, KillType type, String attacker) {
      this(target, type, attacker, null);
    }

    public kill(String target, KillType type) {
      this(target, type, null, null);
    }

    // 從 JSON 字串解析 kill
    public static kill fromJson(String jsonString) {
      try {
        return gson.fromJson(jsonString, kill.class);
      } catch (JsonSyntaxException e) {
        throw new IllegalArgumentException("Invalid JSON format for kill: " + e.getMessage(), e);
      }
    }

    // 轉換為 JSON 字串
    public String toJson() {
      return gson.toJson(this);
    }

    // 從 JSON 字串解析並返回 null 如果失敗
    public static kill fromJsonOrNull(String jsonString) {
      try {
        return gson.fromJson(jsonString, kill.class);
      } catch (JsonSyntaxException e) {
        return null;
      }
    }
  }

  /**
   * 表示一次傷害事件的資料結構。
   */
  public static class damage {
    public String target;
    public String attacker;
    public int damage;

    public damage() {
    }

    public damage(String target, String attacker, int damage) {
      this.target = target;
      this.attacker = attacker;
      this.damage = damage;
    }

    public damage(String target, String attacker) {
      this(target, attacker, 0);
    }

    // 從 JSON 字串解析 damage
    public static damage fromJson(String jsonString) {
      try {
        return gson.fromJson(jsonString, damage.class);
      } catch (JsonSyntaxException e) {
        throw new IllegalArgumentException("Invalid JSON format for damage: " + e.getMessage(), e);
      }
    }

    // 轉換為 JSON 字串
    public String toJson() {
      return gson.toJson(this);
    }

    // 從 JSON 字串解析並返回 null 如果失敗
    public static damage fromJsonOrNull(String jsonString) {
      try {
        return gson.fromJson(jsonString, damage.class);
      } catch (JsonSyntaxException e) {
        return null;
      }
    }
  }

  /**
   * 表示遊戲狀態的資料結構。
   */
  public static class game_status {
    public Integer status;

    public game_status() {
    }

    public game_status(Integer status) {
      this.status = status;
    }

    // 從 JSON 字串解析 game_status
    public static game_status fromJson(String jsonString) {
      try {
        return gson.fromJson(jsonString, game_status.class);
      } catch (JsonSyntaxException e) {
        throw new IllegalArgumentException("Invalid JSON format for game_status: " + e.getMessage(), e);
      }
    }

    // 轉換為 JSON 字串
    public String toJson() {
      return gson.toJson(this);
    }

    // 從 JSON 字串解析並返回 null 如果失敗
    public static game_status fromJsonOrNull(String jsonString) {
      try {
        return gson.fromJson(jsonString, game_status.class);
      } catch (JsonSyntaxException e) {
        return null;
      }
    }
  }

  /**
   * 表示地圖選擇的資料結構。
   */
  public static class map_choose {
    public Integer map;

    public map_choose() {
    }

    public map_choose(Integer map) {
      this.map = map;
    }

    // 從 JSON 字串解析 map_choose
    public static map_choose fromJson(String jsonString) {
      try {
        return gson.fromJson(jsonString, map_choose.class);
      } catch (JsonSyntaxException e) {
        throw new IllegalArgumentException("Invalid JSON format for map_choose: " + e.getMessage(), e);
      }
    }

    // 轉換為 JSON 字串
    public String toJson() {
      return gson.toJson(this);
    }

    // 從 JSON 字串解析並返回 null 如果失敗
    public static map_choose fromJsonOrNull(String jsonString) {
      try {
        return gson.fromJson(jsonString, map_choose.class);
      } catch (JsonSyntaxException e) {
        return null;
      }
    }
  }

  /**
   * 表示儲存資料的資料結構。
   */
  public static class storage_data {
    public String storage;
    public String key;
    public Object[] data;

    public storage_data() {
    }

    public storage_data(String storage, String key, Object[] data) {
      this.storage = storage;
      this.key = key;
      this.data = data;
    }

    public storage_data(String storage, String key) {
      this(storage, key, null);
    }

    // 從 JSON 字串解析 storage_data
    public static storage_data fromJson(String jsonString) {
      try {
        return gson.fromJson(jsonString, storage_data.class);
      } catch (JsonSyntaxException e) {
        throw new IllegalArgumentException("Invalid JSON format for storage_data: " + e.getMessage(), e);
      }
    }

    // 轉換為 JSON 字串
    public String toJson() {
      return gson.toJson(this);
    }

    // 從 JSON 字串解析並返回 null 如果失敗
    public static storage_data fromJsonOrNull(String jsonString) {
      try {
        return gson.fromJson(jsonString, storage_data.class);
      } catch (JsonSyntaxException e) {
        return null;
      }
    }
  }

  /**
   * 表示客戶端 ID 的資料結構。
   */
  public static class clientId {
    public String clientId;

    public clientId() {
    }

    public clientId(String clientId) {
      this.clientId = clientId;
    }

    public static clientId fromJson(String jsonString) {
      try {
        return gson.fromJson(jsonString, clientId.class);
      } catch (JsonSyntaxException e) {
        throw new IllegalArgumentException("Invalid JSON format for clientId: " + e.getMessage(), e);
      }
    }

    // 轉換為 JSON 字串
    public String toJson() {
      return gson.toJson(this);
    }

    // 從 JSON 字串解析並返回 null 如果失敗
    public static clientId fromJsonOrNull(String jsonString) {
      try {
        return gson.fromJson(jsonString, clientId.class);
      } catch (JsonSyntaxException e) {
        return null;
      }
    }
  }

  /**
   * 表示隊伍資訊的資料結構。
   */
  public static class team_info {
    public String team;
    public String UUID;

    public team_info() {
    }

    public team_info(String team, String UUID) {
      this.team = team;
      this.UUID = UUID;
    }

    // 從 JSON 字串解析 team_info
    public static team_info fromJson(String jsonString) {
      try {
        return gson.fromJson(jsonString, team_info.class);
      } catch (JsonSyntaxException e) {
        throw new IllegalArgumentException("Invalid JSON format for team_info: " + e.getMessage(), e);
      }
    }

    // 轉換為 JSON 字串
    public String toJson() {
      return gson.toJson(this);
    }

    // 從 JSON 字串解析並返回 null 如果失敗
    public static team_info fromJsonOrNull(String jsonString) {
      try {
        return gson.fromJson(jsonString, team_info.class);
      } catch (JsonSyntaxException e) {
        return null;
      }
    }
  }

  /**
   * 表示佇列資訊的資料結構。
   */
  public static class Queue {
    public String queue_name;
    public String uuid;
    public String mode;

    public Queue() {
    }

    public Queue(String queue_name, String uuid, String mode) {
      this.queue_name = queue_name;
      this.uuid = uuid;
      this.mode = mode;
    }

    public Queue(String queue_name, String uuid) {
      this(queue_name, uuid, null);
    }
  }

  /**
   * 表示玩家資料請求的資料結構。
   */
  public static class PlayerDataRequest {
    public String uuid;
    public String minecraftId;

    public PlayerDataRequest() {
    }

    public PlayerDataRequest(String uuid, String minecraftId) {
      this.uuid = uuid;
      this.minecraftId = minecraftId;
    }

    public PlayerDataRequest(String uuid) {
      this(uuid, null);
    }

    // 從 JSON 字串解析 PlayerDataRequest
    public static PlayerDataRequest fromJson(String jsonString) {
      try {
        return gson.fromJson(jsonString, PlayerDataRequest.class);
      } catch (JsonSyntaxException e) {
        throw new IllegalArgumentException("Invalid JSON format for PlayerDataRequest: " + e.getMessage(), e);
      }
    }

    // 轉換為 JSON 字串
    public String toJson() {
      return gson.toJson(this);
    }
  }

  /**
   * 表示伺服器傳輸的有效載荷。
   */
  public static class Payload {
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

    public static class teamData {
      public String team;
      public String[] uuids;

      public teamData() {
      }
    }

    public teamData[] teamData;

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

    public static Payload fromJson(String jsonString) {
      try {
        return gson.fromJson(jsonString, Payload.class);
      } catch (JsonSyntaxException e) {
        throw new IllegalArgumentException("Invalid JSON format for Payload: " + e.getMessage(), e);
      }
    }
  }

  public static class Message {
    public Action action;
    public String sessionId;
    public Payload payload;
    public long timestamp;

    public Status status;

    public Message() {
    }

    public Message(Action action, String sessionId, Payload payload) {
      this.action = action;
      this.sessionId = sessionId;
      this.payload = payload;
      timestamp = System.currentTimeMillis();
    }

    public Message(Action action, String sessionId) {
      this(action, sessionId, null);
    }

    public static Message fromJson(String jsonString) {
      try {
        return gson.fromJson(jsonString, Message.class);
      } catch (JsonSyntaxException e) {
        throw new IllegalArgumentException("Invalid JSON format for Message: " + e.getMessage(), e);
      }
    }

    public String toJson() {
      Message copy = new Message();
      copy.action = this.action;
      copy.sessionId = this.sessionId;
      copy.payload = this.payload;
      copy.timestamp = this.timestamp;
      copy.status = this.status;

      // Convert to JSON, then parse and modify action field
      String json = gson.toJson(copy);
      com.google.gson.JsonObject jsonObject = com.google.gson.JsonParser.parseString(json).getAsJsonObject();

      if (this.action != null) {
        jsonObject.addProperty("action", this.action.getString());
      }

      return jsonObject.toString();
    }

    // 從 JSON 字串解析並返回 null 如果失敗
    public static Message fromJsonOrNull(String jsonString) {
      try {
        return gson.fromJson(jsonString, Message.class);
      } catch (JsonSyntaxException e) {
        return null;
      }
    }
  }
}
