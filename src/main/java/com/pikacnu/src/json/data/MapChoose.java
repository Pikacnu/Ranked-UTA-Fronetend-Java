package com.pikacnu.src.json.data;

import com.google.gson.JsonSyntaxException;
import com.pikacnu.src.json.JsonUtils;

/**
 * 表示地圖選擇的資料結構。
 */
public class MapChoose {
  public Integer map;

  public MapChoose() {
  }

  public MapChoose(Integer map) {
    this.map = map;
  }

  /**
   * 從 JSON 字串解析 MapChoose
   */
  public static MapChoose fromJson(String jsonString) {
    try {
      return JsonUtils.getGson().fromJson(jsonString, MapChoose.class);
    } catch (JsonSyntaxException e) {
      throw new IllegalArgumentException("Invalid JSON format for MapChoose: " + e.getMessage(), e);
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
  public static MapChoose fromJsonOrNull(String jsonString) {
    try {
      return JsonUtils.getGson().fromJson(jsonString, MapChoose.class);
    } catch (JsonSyntaxException e) {
      return null;
    }
  }
}
