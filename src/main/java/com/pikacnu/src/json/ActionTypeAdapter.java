package com.pikacnu.src.json;

import com.google.gson.*;
import java.lang.reflect.Type;

/**
 * Action enum 的自定義 Gson 序列化/反序列化器
 */
public class ActionTypeAdapter implements JsonSerializer<Action>, JsonDeserializer<Action> {

  @Override
  public JsonElement serialize(Action src, Type typeOfSrc, JsonSerializationContext context) {
    // 序列化為小寫字串
    return new JsonPrimitive(src.name().toLowerCase());
  }

  @Override
  public Action deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
      String actionName = json.getAsString();
      Action action = Action.fromStringOrNull(actionName);
      if (action != null) {
        return action;
      }
      throw new JsonParseException("Unknown action: " + actionName);
    }
    throw new JsonParseException("Expected string for Action, got: " + json);
  }
}
