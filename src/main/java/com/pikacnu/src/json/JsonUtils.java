package com.pikacnu.src.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * JSON 處理工具類，提供共用的 Gson 實例。
 */
public class JsonUtils {

  /**
   * Gson 實例，用於 JSON 處理。
   * 包含自定義的 Action enum 序列化器以確保正確的 JSON 處理。
   */
  private static final Gson gson = new GsonBuilder()
      .registerTypeAdapter(Action.class, new ActionTypeAdapter())
      .create();

  /**
   * 取得 Gson 實例
   */
  public static Gson getGson() {
    return gson;
  }
}
