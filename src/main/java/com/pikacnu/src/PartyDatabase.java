package com.pikacnu.src;

import java.util.ArrayList;
import java.util.UUID;

import com.pikacnu.UTA2;
import com.pikacnu.src.PlayerDatabase.PlayerData;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import com.pikacnu.src.json.*;

/**
 * PartyDatabase 負責管理隊伍資料與操作。
 */
public class PartyDatabase {
  /**
   * 隊伍成員資料結構。
   */
  public static class PartyPlayer {
    public String uuid;
    public String minecraftId;
    public Integer score;

    /**
     * 建構子。
     */
    public PartyPlayer(String uuid, String minecraftId, Integer score) {
      this.uuid = uuid;
      this.minecraftId = minecraftId;
      this.score = score;
    }
  }

  /**
   * 隊伍資料結構。
   */
  public static class PartyData {
    public int partyId;
    public String partyLeaderUUID;
    public ArrayList<PartyPlayer> partyMembers;
    public boolean isInQueue = false;

    /**
     * 判斷是否為隊長。
     */
    public boolean isPartyLeader(String uuid) {
      return partyLeaderUUID.equals(uuid);
    }

    /**
     * 判斷玩家是否在隊伍中。
     */
    public boolean isInParty(String uuid) {
      for (PartyPlayer member : partyMembers) {
        if (member.uuid.equals(uuid)) {
          return true;
        }
      }
      return false;
    }

    /**
     * 取得隊伍人數。
     */
    public int getPartySize() {
      return partyMembers.size();
    }

    /**
     * 取得隊伍成員中最低分數。
     */
    public int minimumScore() {
      if (partyMembers.isEmpty()) {
        return 0; // No members, return 0
      }
      int minScore = Integer.MAX_VALUE;
      for (PartyPlayer member : partyMembers) {
        if (member.score < minScore) {
          minScore = member.score;
        }
      }
      return minScore;
    }

    /**
     * 取得隊伍成員中最高分數。
     */
    public int maximumScore() {
      if (partyMembers.isEmpty()) {
        return 0; // No members, return 0
      }
      int maxScore = Integer.MIN_VALUE;
      for (PartyPlayer member : partyMembers) {
        if (member.score > maxScore) {
          maxScore = member.score;
        }
      }
      return maxScore;
    }

    /**
     * 解散隊伍。
     */
    public void disbandParty() {
      synchronized (PartyDatabase.class) {
        for (PartyPlayer member : partyMembers) {
          PlayerData player = PlayerDatabase.getPlayerData(member.uuid);
          if (player != null) {
            player.isInParty = false;
            player.partyId = null; // Clear party ID
            PlayerDatabase.updatePlayerData(player);
          }
        }
        partyMembers.clear();
        partyLeaderUUID = null; // Clear party leader UUID
        removeParty(partyId); // Remove the party from the database
      }
    }

    /**
     * 將隊伍資料更新至伺服器。
     */
    public void updateToServer(Action action) {
      Payload payload = new Payload();
      payload.data = this; // Assuming Payload can hold PartyData directly
      Message message = new Message(action, WebSocket.serverSessionId, payload);
      WebSocket.sendMessage(message); // Send the updated party data to the server
    }

    /**
     * 將隊伍資料更新至伺服器，動作為 PARTY。
     */
    public void updateToServer() {
      updateToServer(Action.PARTY);
    }

    /**
     * 加入玩家至隊伍。
     */
    public PartyResultMessage addPlayer(String uuid) {
      synchronized (PartyDatabase.class) {
        if (isInParty(uuid)) {
          return PartyResultMessage.PLAYER_ALREADY_IN_PARTY; // Player is already in the party
        }
        if (getPartySize() >= 4) {
          return PartyResultMessage.PARTY_FULL; // Assuming this means party is full
        }
        PlayerData player = PlayerDatabase.getPlayerData(uuid);
        if (player == null) {
          return PartyResultMessage.PLAYER_NOT_FOUND; // Player not found in player database
        }

        int min = minimumScore();
        int max = maximumScore();
        if (!(player.score >= min - 300 && player.score <= max + 300)) {
          UTA2.LOGGER.info(
              "Player " + player.minecraftId + " with score " + player.score + " is not within the acceptable range " +
                  "of the party's score range (" + min + " - " + max + ")");
          return PartyResultMessage.PLAYER_SCORE_NOT_WITHIN_RANGE; // Player's score is not within the acceptable range
        }

        partyMembers.add(new PartyPlayer(uuid, player.minecraftId, player.score));
        player.isInParty = true;
        player.partyId = partyId;
        PlayerDatabase.updatePlayerData(player);

        updateToServer(); // Update the party data to the server

        return PartyResultMessage.PARTY_JOINED; // Successfully added player to the party
      }
    }

    /**
     * 移除隊伍中的玩家。
     */
    public PartyResultMessage removePlayer(String uuid) {
      synchronized (PartyDatabase.class) {
        if (!isInParty(uuid)) {
          return PartyResultMessage.PLAYER_NOT_IN_PARTY; // Player is not in the party
        }

        if (partyMembers.size() <= 1) {
          this.disbandParty(); // If only one member left, disband the party
          updateToServer(Action.PARTY_DISBANDED); // Notify server about disbanding
          return PartyResultMessage.PARTY_DISBANDED; // Successfully disbanded the party
        }

        PartyPlayer playerToRemove = null;
        for (PartyPlayer member : partyMembers) {
          if (member.uuid.equals(uuid)) {
            playerToRemove = member;
            break;
          }
        }
        if (playerToRemove == null) {
          return PartyResultMessage.PLAYER_NOT_FOUND; // Player not found in party members
        }

        partyMembers.remove(playerToRemove);

        if (partyLeaderUUID.equals(uuid) && !partyMembers.isEmpty()) {
          this.partyLeaderUUID = partyMembers.get(0).uuid; // Set the first member as the new leader
        }

        PlayerData player = PlayerDatabase.getPlayerData(uuid);
        if (player != null) {
          player.isInParty = false;
          player.partyId = null; // Clear party ID
          PlayerDatabase.updatePlayerData(player);
        }

        updateToServer(); // Update the party data to the server after removing the player

        return PartyResultMessage.PARTY_LEFT; // Successfully removed player from the party
      }
    }
  }

  /**
   * 隊伍邀請資料結構。
   */
  public static class PartyInvition {
    public int partyId;
    public String targetUuid;
    public String inviterUuid;
    public Long timestamp;

    /**
     * 建構子。
     */
    public PartyInvition() {
      // Default constructor
    }

    /**
     * 建構子。
     */
    public PartyInvition(int partyId, String targetUuid, String inviterUuid) {
      this.partyId = partyId;
      this.targetUuid = targetUuid;
      this.inviterUuid = inviterUuid;
      this.timestamp = System.currentTimeMillis(); // Set the current timestamp
    }
  }

  public static ArrayList<PartyData> partyList = new ArrayList<>();
  public static ArrayList<PartyInvition> partyInvitions = new ArrayList<>();
  public static MinecraftServer server;

  public PartyDatabase(
      MinecraftServer server) {
    PartyDatabase.server = server;
  }

  /**
   * 取得指定 ID 的隊伍資料。
   */
  public static PartyData getPartyData(int partyId) {
    for (PartyData party : partyList) {
      if (party.partyId == partyId)
        return party;
    }
    return null;
  }

  /**
   * 建立新隊伍。
   */
  public static PartyData createParty(String leaderUuid) {
    synchronized (PartyDatabase.class) {
      PartyData newParty = new PartyData();
      newParty.partyId = (int) System.currentTimeMillis();
      newParty.partyMembers = new ArrayList<>();
      newParty.partyLeaderUUID = leaderUuid;
      PlayerData leader = PlayerDatabase.getPlayerData(leaderUuid);
      if (leader == null) {
        return null;
      }
      leader.isInParty = true;
      leader.partyId = newParty.partyId; // Set party ID for the leader
      PlayerDatabase.updatePlayerData(leader);
      newParty.partyMembers.add(new PartyPlayer(leaderUuid, leader.minecraftId, leader.score));
      newParty.updateToServer();
      partyList.add(newParty);
      return newParty;
    }
  }

  /**
   * 移除指定 ID 的隊伍。
   */
  public static void removeParty(int partyId) {
    synchronized (PartyDatabase.class) {
      partyList.removeIf(party -> party.partyId == partyId);
    }
  }

  /**
   * 取得玩家所屬的隊伍資料。
   */
  public static PartyData getPartyData(String uuid) {
    for (PartyData party : partyList) {
      if (party.isInParty(uuid)) {
        return party;
      }
    }
    return null;
  }

  /**
   * 建立隊伍邀請。
   */
  public static PartyResultMessage createInvitation(int partyId, String inviterUuid, String targetUuid) {
    PartyData party = getPartyData(partyId);
    if (party == null) {
      return PartyResultMessage.PARTY_NOT_FOUND; // Party does not exist
    }
    if (party.getPartySize() >= 4) {
      return PartyResultMessage.PARTY_FULL; // Party is full
    }
    PartyData existingParty = getPartyData(targetUuid);
    if (existingParty != null) {
      return PartyResultMessage.PLAYER_ALREADY_IN_PARTY; // Player is already in a party
    }

    for (PartyInvition invition : partyInvitions) {
      if (invition.partyId == partyId && invition.targetUuid.equals(targetUuid)) {
        return PartyResultMessage.PLAYER_INVITATION_ALREADY_EXISTS; // Player already has an invitation
      }
    }
    PlayerData inviter = PlayerDatabase.getPlayerData(inviterUuid);
    PlayerData targetPlayer = PlayerDatabase.getPlayerData(targetUuid);
    if (Math.abs(inviter.score - targetPlayer.score) > 300) {
      return PartyResultMessage.PLAYER_SCORE_NOT_WITHIN_RANGE; // Player's score is not within the acceptable range
    }
    if (inviter == null || targetPlayer == null) {
      return PartyResultMessage.PLAYER_NOT_FOUND; // Inviter or target player not found
    }
    if (!party.isPartyLeader(inviterUuid)) {
      return PartyResultMessage.PLAYER_NOT_LEADER; // Inviter is not the party leader
    }
    if (PlayerDatabase.getPlayerData(targetUuid) == null) {
      return PartyResultMessage.PLAYER_NOT_FOUND; // Target player not found
    }
    server.getPlayerManager().getPlayer(UUID.fromString(inviterUuid))
        .sendMessage(Text.literal("Invitation sent to " + targetPlayer.minecraftId)
            .withColor(0x00FF00), false);
    server.getPlayerManager().getPlayer(UUID.fromString(targetUuid))
        .sendMessage(Text
            .literal("You have received an invitation from " + inviter.minecraftId)
            .withColor(0x00FF00), false);
    partyInvitions.add(new PartyInvition(partyId, targetUuid, inviterUuid));
    return null;
  }

  /**
   * 接受隊伍邀請。
   */
  public static PartyResultMessage AcceptInvitation(String targetUuid, String inviterUuid) {
    PartyInvition targetInvitation = null;
    for (PartyInvition invitation : partyInvitions) {
      if (invitation.targetUuid.equals(targetUuid) && invitation.inviterUuid.equals(inviterUuid)) {
        targetInvitation = invitation;
        break;
      }
    }

    if (targetInvitation != null) {
      PartyData party = getPartyData(targetInvitation.partyId);
      if (party != null) {
        PartyResultMessage result = party.addPlayer(targetUuid);
        if (result == PartyResultMessage.PARTY_JOINED) {
          ArrayList<PartyInvition> othersInvitations = new ArrayList<>(partyInvitions.stream()
              .filter(inv -> inv.targetUuid.equals(targetUuid) && !inv.inviterUuid.equals(inviterUuid))
              .toList());
          othersInvitations.forEach(inv -> {
            server.getPlayerManager().getPlayer(UUID.fromString(inv.inviterUuid)).sendMessage(
                Text.literal(PartyResultMessage.PLAYER_INVITATION_REJECTED.getMessage()).withColor(0xFF0000), false);
          });
          server.getPlayerManager().getPlayer(UUID.fromString(inviterUuid))
              .sendMessage(
                  Text.literal("Invitation accepted by " + PlayerDatabase.getPlayerData(targetUuid).minecraftId)
                      .withColor(0x00FF00),
                  false);
          partyInvitions.removeAll(othersInvitations); // Remove all other invitations for the target player
          partyInvitions.remove(targetInvitation); // Remove the accepted invitation
          return PartyResultMessage.PARTY_JOINED;
        } else {
          partyInvitions.remove(targetInvitation); // Remove the invitation if not successful
          return result; // Return the result of adding the player to the party
        }
      } else {
        return PartyResultMessage.PARTY_NOT_FOUND; // Party does not exist
      }
    }
    return PartyResultMessage.INVITATION_NOT_FOUND; // Invitation not found
  }

  /**
   * 拒絕隊伍邀請。
   */
  public static PartyResultMessage RejectInvitation(String targetUuid, String inviterUuid) {
    for (PartyInvition invitation : partyInvitions) {
      if (invitation.targetUuid.equals(targetUuid) && invitation.inviterUuid.equals(inviterUuid)) {
        partyInvitions.remove(invitation);
        server.getPlayerManager().getPlayer(UUID.fromString(inviterUuid))
            .sendMessage(Text.literal(PlayerDatabase.getPlayerData(targetUuid).minecraftId).withColor(0xFF0000).append(
                Text.literal(" has Rejected Your Invitation")), false);
        return PartyResultMessage.PLAYER_INVITATION_REJECTED;
      }
    }
    return PartyResultMessage.INVITATION_NOT_FOUND; // Invitation not found
  }

  /**
   * 取得指定玩家的邀請資料。
   */
  public static PartyInvition getInvitation(String targetUuid) {
    for (PartyInvition invitation : partyInvitions) {
      if (invitation.targetUuid.equals(targetUuid)) {
        return invitation; // Return the first invitation found for the target player
      }
    }
    return null; // No invitation found for the target player
  }

  /**
   * 清除過期的隊伍邀請。
   */
  public static void schedulePartyInvitationCleanup() {
    long currentTime = System.currentTimeMillis();
    ArrayList<PartyInvition> expiredInvitations = new ArrayList<>(
        partyInvitions.stream()
            .filter(invitation -> currentTime - invitation.timestamp > 30 * 1000)
            .toList());
    for (PartyInvition invitation : expiredInvitations) {
      partyInvitions.remove(invitation);
      server.getPlayerManager().getPlayer(UUID.fromString(invitation.inviterUuid))
          .sendMessage(Text.literal("Invitation to " + PlayerDatabase.getPlayerData(invitation.targetUuid).minecraftId +
              " has expired.").withColor(0xFF0000), false);
    }
  }

  /**
   * 清除所有隊伍與邀請資料。
   */
  public static void clear() {
    partyList.clear();
    partyInvitions.clear();
  }

  /**
   * 隊伍操作結果訊息。
   */
  public enum PartyResultMessage {
    PARTY_CREATED("Party created successfully"),
    PARTY_JOINED("Joined party successfully"),
    PARTY_LEFT("Left party successfully"),
    PARTY_DISBANDED("Party disbanded successfully"),
    PARTY_NOT_FOUND("Party not found"),
    PARTY_FULL("Party is full"),
    PLAYER_NOT_IN_PARTY("Player is not in a party"),
    PLAYER_NOT_FOUND("Player not found"),
    PLAYER_NOT_LEADER("You are not the party leader"),
    PLAYER_SCORE_NOT_WITHIN_RANGE("Player's score is not within the acceptable range (±300)"),
    PLAYER_INVITATION_SENT("Invitation sent successfully"),
    PLAYER_INVITATION_ACCEPTED("Invitation accepted successfully"),
    PLAYER_INVITATION_REJECTED("Invitation rejected successfully"),
    PLAYER_INVITATION_ALREADY_EXISTS("Player already has an invitation"),
    INVITATION_NOT_FOUND("Invitation not found"),
    INVITATION_EXPIRED("Invitation has expired"),
    PARTY_IN_QUEUE("Party is in queue"),
    PLAYER_ALREADY_IN_PARTY("Player is already in a party");

    private final String message;
    PartyResultMessage(String message) {
      this.message = message;
    }
    public String getMessage() {
      return message;
    }
  }
}
