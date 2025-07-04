package com.pikacnu.src.json.data;

import com.google.gson.JsonSyntaxException;
import com.pikacnu.src.json.JsonUtils;
import com.pikacnu.src.json.KillType;

/**
 * 表示一次擊殺事件的資料結構。
 */
public class Kill {
  public String target;
  public KillType type;
  public String attacker;
  public String assists;

  public Kill() {
  }

  public Kill(String target, KillType type, String attacker, String assists) {
    this.target = target;
    this.type = type;
    this.attacker = attacker;
    this.assists = assists;
  }

  public Kill(String target, KillType type, String attacker) {
    this(target, type, attacker, null);
  }

  public Kill(String target, KillType type) {
    this(target, type, null, null);
  }

  /**
   * 從 JSON 字串解析 Kill
   */
  public static Kill fromJson(String jsonString) {
    try {
      return JsonUtils.getGson().fromJson(jsonString, Kill.class);
    } catch (JsonSyntaxException e) {
      throw new IllegalArgumentException("Invalid JSON format for Kill: " + e.getMessage(), e);
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
  public static Kill fromJsonOrNull(String jsonString) {
    try {
      return JsonUtils.getGson().fromJson(jsonString, Kill.class);
    } catch (JsonSyntaxException e) {
      return null;
    }
  }
}
