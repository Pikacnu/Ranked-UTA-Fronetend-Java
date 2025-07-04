package com.pikacnu.src.websocket.handler;

import java.util.ArrayList;
import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.Status;
import com.pikacnu.src.json.data.Payload;
import com.pikacnu.src.json.data.Payload.QueueData;
import com.pikacnu.src.PartyDatabase;
import com.pikacnu.src.PartyDatabase.PartyData;
import com.pikacnu.UTA2;
import net.minecraft.server.MinecraftServer;

public class QueueMatchHandler extends BaseHandler {

  public QueueMatchHandler(MinecraftServer server) {
    super(server);
  }

  @Override
  public void handle(Action action, Status status, String sessionId, Payload payload) {
    if (payload != null && payload.queue != null) {
      QueueData queueData = payload.queue;
      ArrayList<ArrayList<PartyData>> parties = queueData.parties;
      if (parties == null || parties.isEmpty()) {
        UTA2.LOGGER.error("Received QUEUE_MATCH message with empty or null parties");
        return;
      }

      parties.stream().forEach(partyList -> {
        if (partyList == null || partyList.isEmpty()) {
          UTA2.LOGGER.warn("Received QUEUE_MATCH message with empty party list");
          return;
        }
        partyList.forEach(party -> {
          if (party == null) {
            UTA2.LOGGER.warn("Received QUEUE_MATCH message with null party");
            return;
          }
          PartyDatabase.removeParty(party.partyId);
        });
      });

      UTA2.LOGGER.info("Received QUEUE_MATCH action: " + action);
      // Handle queue logic here, e.g., add to queue, remove from queue, etc.
    } else {
      UTA2.LOGGER.error("Received QUEUE_MATCH message with invalid payload");
    }
  }

  @Override
  public Action getActionType() {
    return Action.QUEUE_MATCH;
  }
}
