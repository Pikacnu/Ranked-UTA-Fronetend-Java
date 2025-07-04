package com.pikacnu.src.json;

/**
 * 定義所有支援的動作類型。
 */
public enum Action {
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

  PLAYER_INFO("player_info"),
  OUTPUT_WIN("output_win"),

  PLAYER_ONLINE_STATUS("player_online_status");

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

  /**
   * 從字串獲取對應的 enum 值（不區分大小寫）
   */
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

  /**
   * 從字串獲取對應的 enum 值，如果找不到則返回 null
   */
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
