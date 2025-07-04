package com.pikacnu.src.json.data;

import com.google.gson.JsonSyntaxException;
import com.pikacnu.src.json.JsonUtils;

/**
 * 表示佇列資訊的資料結構。
 */
public class Queue {
  public String queue_name;
  public String uuid;
  public String mode;

  public Queue() {
  }

  public Queue(String queue_name, String uuid, String mode) {
    this.queue_name = queue_name;
    this.uuid = uuid;
    this.mode = mode;
  }

  public Queue(String queue_name, String uuid) {
    this(queue_name, uuid, null);
  }

  /**
   * 從 JSON 字串解析 Queue
   */
  public static Queue fromJson(String jsonString) {
    try {
      return JsonUtils.getGson().fromJson(jsonString, Queue.class);
    } catch (JsonSyntaxException e) {
      throw new IllegalArgumentException("Invalid JSON format for Queue: " + e.getMessage(), e);
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
  public static Queue fromJsonOrNull(String jsonString) {
    try {
      return JsonUtils.getGson().fromJson(jsonString, Queue.class);
    } catch (JsonSyntaxException e) {
      return null;
    }
  }
}
