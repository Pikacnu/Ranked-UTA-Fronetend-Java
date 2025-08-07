package com.pikacnu.src.websocket.handler;

import com.mojang.brigadier.ParseResults;
import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.Status;
import com.pikacnu.src.json.data.Payload;
import com.pikacnu.src.websocket.WebSocketClient;
import com.pikacnu.UTA2;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

public class RequestDataHandler extends BaseHandler {

  public RequestDataHandler(MinecraftServer server) {
    super(server);
  }

  @Override
  public void handle(Action action, Status status, String sessionId, Payload payload) {
    String requestTarget = payload != null ? payload.request_target : "";
    if (requestTarget.isEmpty()) {
      UTA2.LOGGER.error("Received request_data message with empty request_target");
      return;
    }

    WebSocketClient.Task target = WebSocketClient.taskQueue.stream()
        .filter(t -> t.action.equals(requestTarget))
        .findFirst()
        .orElse(null);

    if (target != null) {
      UTA2.LOGGER.info("Found task for action: {} with function {}", target.action, target.function);
      try {
        String functionString = target.function.toString() + " " + payload.data.toString();

        ParseResults<ServerCommandSource> parsedResult = server.getCommandManager().getDispatcher()
            .parse(functionString, server.getCommandSource().withLevel(2));
        server.getCommandManager().execute(parsedResult, functionString);
      } catch (Exception e) {
        UTA2.LOGGER.error("Error executing task function: {}", e.getMessage());
      }
      WebSocketClient.taskQueue.remove(target); // Remove task after execution
    } else {
      UTA2.LOGGER.warn("No task found for action: {}", requestTarget);
    }
  }

  @Override
  public Action getActionType() {
    return Action.request_data;
  }
}
