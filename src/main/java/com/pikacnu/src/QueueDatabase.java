package com.pikacnu.src;

import java.util.ArrayList;

import com.pikacnu.UTA2;
import com.pikacnu.src.PartyDatabase.PartyData;
import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.data.Message;
import com.pikacnu.src.json.data.Payload;
import com.pikacnu.src.websocket.WebSocketClient;

public class QueueDatabase {
  public static enum QueueType {
    solo,
    duo,
    trio,
    siege,
    leave
  }

  public static class QueueDataEntry {
    public int partyId;
    public QueueType queueType;
  }

  private static final ArrayList<QueueDataEntry> QueueData = new ArrayList<>();

  private static void updateQueueDataToServer(int partyId, QueueType queueType, String executorUuid) {
    Payload payload = new Payload();
    payload.queue = new Payload.QueueData(
        queueType.name(), executorUuid,
        new ArrayList<>());
    Action action = queueType == QueueType.leave ? Action.queue_leave : Action.queue;
    Message message = new Message(action, payload);
    WebSocketClient.sendMessage(message);
  }

  public static void updateQueueData(int partyId, QueueType queueType, String executorUuid) {
    if (queueType == QueueType.leave) {
      PartyData party = PartyDatabase.getPartyData(partyId);
      if (party == null) {
        UTA2.LOGGER.error("Party with ID {} does not exist.", partyId);
      }
      removeQueueData(partyId);
    } else {
      addQueueData(partyId, queueType);
    }
    updateQueueDataToServer(partyId, queueType, executorUuid);
  }

  public static void addQueueData(int partyId, QueueType queueType) {
    QueueDataEntry entry = new QueueDataEntry();
    entry.partyId = partyId;
    if (!PartyDatabase.isPartyExists(partyId)) {
      UTA2.LOGGER.error("Party with ID {} does not exist.", partyId);
    }
    entry.queueType = queueType;
    QueueData.add(entry);
  }

  public static void removeQueueData(int partyId) {
    QueueData.removeIf(entry -> entry.partyId == partyId);
  }

  public static void updateAndRemoveQueueData(int partyId, QueueType queueType, String executorUuid) {
    if (!PartyDatabase.isPartyExists(partyId)) {
      throw new IllegalArgumentException("Party with ID " + partyId + " does not exist.");
    }
    updateQueueData(partyId, queueType, executorUuid);
    removeQueueData(partyId);
  }

  public static boolean isInQueue(int partyId) {
    for (QueueDataEntry entry : QueueData) {
      if (entry.partyId == partyId) {
        return true; // Party is in queue
      }
    }
    return false; // Party is not in queue
  }

  public static boolean isInQueue(String uuid) {
    for (QueueDataEntry entry : QueueData) {
      PartyData party = PartyDatabase.getPartyData(entry.partyId);
      if (party != null && party.isInParty(uuid)) {
        return true; // Player is in queue
      }
    }
    return false; // Player is not in queue
  }

  public static ArrayList<QueueDataEntry> getQueueData() {
    return QueueData;
  }
}
