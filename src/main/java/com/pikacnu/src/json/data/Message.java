package com.pikacnu.src.json.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.JsonUtils;
import com.pikacnu.src.json.Status;

/**
 * 表示訊息的資料結構。
 */
public class Message {
	public Action action;
	public String sessionId;
	public Payload payload;
	public long timestamp;
	public Status status;

	public Message(Action action, String sessionId, Payload payload) {
		this.action = action;
		this.sessionId = sessionId;
		this.payload = payload;
		this.timestamp = System.currentTimeMillis();
	}

	public Message(Action action, String sessionId) {
		this(action, sessionId, null);
	}

	/**
	 * 從 JSON 字串解析 Message
	 */
	public static Message fromJson(String jsonString) {
		try {
			return JsonUtils.getGson().fromJson(jsonString, Message.class);
		} catch (JsonSyntaxException e) {
			throw new IllegalArgumentException("Invalid JSON format for Message: " + e.getMessage(), e);
		}
	}

	/**
	 * 轉換為 JSON 字串
	 */
	public String toJson() {
		// Convert to JSON, then parse and modify action field
		String json = JsonUtils.getGson().toJson(this);
		JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

		if (this.action != null) {
			jsonObject.addProperty("action", this.action.toString().toLowerCase());
		}

		return jsonObject.toString();
	}

	/**
	 * 從 JSON 字串解析並返回 null 如果失敗
	 */
	public static Message fromJsonOrNull(String jsonString) {
		try {
			return JsonUtils.getGson().fromJson(jsonString, Message.class);
		} catch (JsonSyntaxException e) {
			return null;
		}
	}
}
