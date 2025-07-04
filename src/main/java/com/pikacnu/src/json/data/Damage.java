package com.pikacnu.src.json.data;

import com.google.gson.JsonSyntaxException;
import com.pikacnu.src.json.JsonUtils;

/**
 * 表示一次傷害事件的資料結構。
 */
public class Damage {
	public String target;
	public String attacker;
	public int damage;

	public Damage(String target, String attacker, int damage) {
		this.target = target;
		this.attacker = attacker;
		this.damage = damage;
	}

	public Damage(String target, String attacker) {
		this(target, attacker, 0);
	}

	/**
	 * 從 JSON 字串解析 Damage
	 */
	public static Damage fromJson(String jsonString) {
		try {
			return JsonUtils.getGson().fromJson(jsonString, Damage.class);
		} catch (JsonSyntaxException e) {
			throw new IllegalArgumentException("Invalid JSON format for Damage: " + e.getMessage(), e);
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
	public static Damage fromJsonOrNull(String jsonString) {
		try {
			return JsonUtils.getGson().fromJson(jsonString, Damage.class);
		} catch (JsonSyntaxException e) {
			return null;
		}
	}
}
