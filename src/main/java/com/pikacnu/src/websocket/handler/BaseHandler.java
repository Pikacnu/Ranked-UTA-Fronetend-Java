package com.pikacnu.src.websocket.handler;

import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.Status;
import com.pikacnu.src.json.data.Payload;
import net.minecraft.server.MinecraftServer;

public abstract class BaseHandler {
  protected MinecraftServer server;

  public BaseHandler(MinecraftServer server) {
    this.server = server;
  }

  /**
   * 處理特定 Action 的邏輯
   * 
   * @param action    操作類型
   * @param status    狀態
   * @param sessionId 會話 ID
   * @param payload   資料載荷
   */
  public abstract void handle(Action action, Status status, String sessionId, Payload payload);

  /**
   * 取得此 Handler 處理的 Action 類型
   * 
   * @return Action 類型
   */
  public abstract Action getActionType();

  /**
   * 檢查是否可以處理指定的 Action
   * 
   * @param action 要檢查的 Action
   * @return 如果可以處理則返回 true
   */
  public boolean canHandle(Action action) {
    return getActionType().equals(action);
  }
}
