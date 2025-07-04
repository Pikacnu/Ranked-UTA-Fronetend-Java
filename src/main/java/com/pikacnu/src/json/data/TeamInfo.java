package com.pikacnu.src.json.data;

import com.google.gson.JsonSyntaxException;
import com.pikacnu.src.json.JsonUtils;

/**
 * 表示隊伍資訊的資料結構。
 */
public class TeamInfo {
  public String team;
  public String UUID;

  public TeamInfo() {
  }

  public TeamInfo(String team, String UUID) {
    this.team = team;
    this.UUID = UUID;
  }

  /**
   * 從 JSON 字串解析 TeamInfo
   */
  public static TeamInfo fromJson(String jsonString) {
    try {
      return JsonUtils.getGson().fromJson(jsonString, TeamInfo.class);
    } catch (JsonSyntaxException e) {
      throw new IllegalArgumentException("Invalid JSON format for TeamInfo: " + e.getMessage(), e);
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
  public static TeamInfo fromJsonOrNull(String jsonString) {
    try {
      return JsonUtils.getGson().fromJson(jsonString, TeamInfo.class);
    } catch (JsonSyntaxException e) {
      return null;
    }
  }
}
