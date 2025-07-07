package com.pikacnu.src.websocket.handler;

import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.Status;
import com.pikacnu.src.json.data.Payload;
import com.pikacnu.src.json.data.Payload.WhitelistEntry;
import com.pikacnu.src.WhiteListManager;
import com.pikacnu.UTA2;
import net.minecraft.server.MinecraftServer;

public class WhitelistHandler extends BaseHandler {

  public WhitelistHandler(MinecraftServer server) {
    super(server);
  }

  @Override
  public void handle(Action action, Status status, String sessionId, Payload payload) {
    if (payload == null || payload.whitelist == null) {
      UTA2.LOGGER.error("Received WHILELIST_CHANGE message with invalid payload");
      return;
    }

    if (payload.whitelist.isEmpty() || payload.whitelist.size() == 0) {
      UTA2.LOGGER.info("Received WHILELIST_CHANGE message with empty whitelist, clearing whitelist");
      WhiteListManager.clearWhitelist();
      return;
    }

    for (WhitelistEntry player : payload.whitelist) {
      if (player.uuid() == null || player.uuid().isEmpty() || player.minecraftId() == null
          || player.minecraftId().isEmpty()) {
        UTA2.LOGGER.warn("Received WHILELIST_CHANGE message with empty UUID");
        continue;
      }
      try {
        WhiteListManager.addPlayerToWhitelist(player.uuid(), player.minecraftId());
      } catch (Exception e) {
        UTA2.LOGGER.error("Failed to add player to whitelist: " + e.getMessage());
      }
    }

    // Safely kick players not in whitelist with error handling
    try {
      WhiteListManager.kickPlayerNotInWhitelist();
    } catch (Exception e) {
      UTA2.LOGGER.warn("Error while kicking non-whitelisted players (this is usually harmless): " + e.getMessage());
    }
  }

  @Override
  public Action getActionType() {
    return Action.whitelist_change;
  }
}
