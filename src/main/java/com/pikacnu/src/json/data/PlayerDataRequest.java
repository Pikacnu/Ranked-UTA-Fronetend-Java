package com.pikacnu.src.json.data;

import com.google.gson.JsonSyntaxException;
import com.pikacnu.src.json.JsonUtils;

/**
 * 表示玩家資料請求的資料結構。
 */
public class PlayerDataRequest {
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

  /**
   * 從 JSON 字串解析 PlayerDataRequest
   */
  public static PlayerDataRequest fromJson(String jsonString) {
    try {
      return JsonUtils.getGson().fromJson(jsonString, PlayerDataRequest.class);
    } catch (JsonSyntaxException e) {
      throw new IllegalArgumentException("Invalid JSON format for PlayerDataRequest: " + e.getMessage(), e);
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
  public static PlayerDataRequest fromJsonOrNull(String jsonString) {
    try {
      return JsonUtils.getGson().fromJson(jsonString, PlayerDataRequest.class);
    } catch (JsonSyntaxException e) {
      return null;
    }
  }
}
