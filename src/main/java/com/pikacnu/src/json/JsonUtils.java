package com.pikacnu.src.json;

import com.google.gson.Gson;

/**
 * JSON 處理工具類，提供共用的 Gson 實例。
 */
public class JsonUtils {

  /**
   * Gson 實例，用於 JSON 處理。
   */
  private static final Gson gson = new Gson();

  /**
   * 取得 Gson 實例
   */
  public static Gson getGson() {
    return gson;
  }
}
