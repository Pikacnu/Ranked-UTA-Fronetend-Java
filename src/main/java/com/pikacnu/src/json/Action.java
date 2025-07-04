package com.pikacnu.src.json;

/**
 * 定義所有支援的動作類型。
 */
public enum Action {
  connect,
  disconnect,
  command,
  error,
  kill,
  damage,
  map_choose,
  storage_data,
  client_id,
  team_info,
  team_join,
  heartbeat,
  handshake,
  request_data,
  get_player_data,
  party,
  party_disbanded,
  queue,
  queue_leave,
  queue_match,
  whitelist,
  whitelist_change,
  whitelist_remove,
  whitelist_check,
  transfer,

  player_info,
  output_win,
  game_status,

  player_online_status;

  /**
   * 從字串獲取對應的 enum 值（不區分大小寫）
   */
  public static Action fromString(String text) {
    if (text == null)
      throw new IllegalArgumentException("No enum constant for null action");

    for (Action action : Action.values())
      if (action.name().equalsIgnoreCase(text))
        return action;

    throw new IllegalArgumentException("No enum constant for action: " + text);
  }

  /**
   * 從字串獲取對應的 enum 值，如果找不到則返回 null
   */
  public static Action fromStringOrNull(String text) {
    if (text != null) {
      for (Action action : Action.values()) {
        if (action.name().equalsIgnoreCase(text)) {
          return action;
        }
      }
    }
    return null;
  }
}
