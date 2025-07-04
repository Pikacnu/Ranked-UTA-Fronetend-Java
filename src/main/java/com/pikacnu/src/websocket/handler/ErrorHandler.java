package com.pikacnu.src.websocket.handler;

import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.Status;
import com.pikacnu.src.json.data.Payload;
import com.pikacnu.UTA2;
import net.minecraft.server.MinecraftServer;

public class ErrorHandler extends BaseHandler {

  public ErrorHandler(MinecraftServer server) {
    super(server);
  }

  @Override
  public void handle(Action action, Status status, String sessionId, Payload payload) {
    String errorMessage = payload != null ? payload.message : "Unknown error";
    UTA2.LOGGER.error("Received error message: " + errorMessage);
  }

  @Override
  public Action getActionType() {
    return Action.error;
  }
}
