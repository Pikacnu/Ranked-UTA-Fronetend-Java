package com.pikacnu.src.json.data;

import com.google.gson.JsonSyntaxException;
import com.pikacnu.Config;
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

	public Message(Action action, Payload payload) {
		this(action, Config.serverId, payload);
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
		// 現在使用自定義的 ActionTypeAdapter，不需要手動處理 action 字段
		return JsonUtils.getGson().toJson(this);
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
