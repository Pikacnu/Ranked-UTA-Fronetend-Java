package com.pikacnu.src.json.data;

import com.google.gson.JsonSyntaxException;
import com.pikacnu.src.json.JsonUtils;

/**
 * 表示客戶端 ID 的資料結構。
 */
public class ClientId {
	public String clientId;

	public ClientId() {
	}

	public ClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * 從 JSON 字串解析 ClientId
	 */
	public static ClientId fromJson(String jsonString) {
		try {
			return JsonUtils.getGson().fromJson(jsonString, ClientId.class);
		} catch (JsonSyntaxException e) {
			throw new IllegalArgumentException("Invalid JSON format for ClientId: " + e.getMessage(), e);
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
	public static ClientId fromJsonOrNull(String jsonString) {
		try {
			return JsonUtils.getGson().fromJson(jsonString, ClientId.class);
		} catch (JsonSyntaxException e) {
			return null;
		}
	}
}
