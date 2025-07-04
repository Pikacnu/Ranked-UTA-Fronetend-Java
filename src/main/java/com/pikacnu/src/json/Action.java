package com.pikacnu.src.json;

/**
 * 定義所有支援的動作類型。
 */
public enum Action {
	CONNECT(),
	DISCONNECT(),
	COMMAND(),
	ERROR(),
	KILL(),
	DAMAGE(),
	GAME_STATUS(),
	MAP_CHOOSE(),
	STORAGE_DATA(),
	CLIENT_ID(),
	TEAM_INFO(),
	TEAM_JOIN(),
	HEARTBEAT(),
	HANDSHAKE(),
	REQUEST_DATA(),
	GET_PLAYER_DATA(),
	PARTY(),
	PARTY_DISBANDED(),
	QUEUE(),
	QUEUE_LEAVE(),
	QUEUE_MATCH(),
	WHITELIST(),
	WHITELIST_CHANGE(),
	WHITELIST_REMOVE(),
	WHITELIST_CHECK(),
	TRANSFER(),

	PLAYER_INFO(),
	OUTPUT_WIN(),

	PLAYER_ONLINE_STATUS();

	/**
	 * 從字串獲取對應的 enum 值（不區分大小寫）
	 */
	public static Action fromString(String text)
	{
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
