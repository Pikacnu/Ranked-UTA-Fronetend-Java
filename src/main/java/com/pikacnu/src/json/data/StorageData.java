package com.pikacnu.src.json.data;

import com.google.gson.JsonSyntaxException;
import com.pikacnu.src.json.JsonUtils;

/**
 * 表示儲存資料的資料結構。
 */
public class StorageData {
  public String storage;
  public String key;
  public Object[] data;

  public StorageData() {
  }

  public StorageData(String storage, String key, Object[] data) {
    this.storage = storage;
    this.key = key;
    this.data = data;
  }

  public StorageData(String storage, String key) {
    this(storage, key, null);
  }

  /**
   * 從 JSON 字串解析 StorageData
   */
  public static StorageData fromJson(String jsonString) {
    try {
      return JsonUtils.getGson().fromJson(jsonString, StorageData.class);
    } catch (JsonSyntaxException e) {
      throw new IllegalArgumentException("Invalid JSON format for StorageData: " + e.getMessage(), e);
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
  public static StorageData fromJsonOrNull(String jsonString) {
    try {
      return JsonUtils.getGson().fromJson(jsonString, StorageData.class);
    } catch (JsonSyntaxException e) {
      return null;
    }
  }
}
