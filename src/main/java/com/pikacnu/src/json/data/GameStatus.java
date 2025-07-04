package com.pikacnu.src.json.data;

import com.google.gson.JsonSyntaxException;
import com.pikacnu.src.json.JsonUtils;

/**
 * 表示遊戲狀態的資料結構。
 */
public class GameStatus {
  public Integer status;

  public GameStatus() {
  }

  public GameStatus(Integer status) {
    this.status = status;
  }

  /**
   * 從 JSON 字串解析 GameStatus
   */
  public static GameStatus fromJson(String jsonString) {
    try {
      return JsonUtils.getGson().fromJson(jsonString, GameStatus.class);
    } catch (JsonSyntaxException e) {
      throw new IllegalArgumentException("Invalid JSON format for GameStatus: " + e.getMessage(), e);
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
  public static GameStatus fromJsonOrNull(String jsonString) {
    try {
      return JsonUtils.getGson().fromJson(jsonString, GameStatus.class);
    } catch (JsonSyntaxException e) {
      return null;
    }
  }
}
