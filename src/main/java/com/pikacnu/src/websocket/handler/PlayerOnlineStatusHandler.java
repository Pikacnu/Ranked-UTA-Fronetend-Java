package com.pikacnu.src.websocket.handler;

import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.Status;
import com.pikacnu.src.json.data.Payload;
import net.minecraft.server.MinecraftServer;

public class PlayerOnlineStatusHandler extends BaseHandler {

  public PlayerOnlineStatusHandler(MinecraftServer server) {
    super(server);
  }

  @Override
  public void handle(Action action, Status status, String sessionId, Payload payload) {
    // 處理玩家線上狀態的邏輯
    // 這裡可以根據需要添加具體的實作
  }

  @Override
  public Action getActionType() {
    return Action.player_online_status;
  }
}
