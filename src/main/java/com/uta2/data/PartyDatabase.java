package com.uta2.data;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PartyDatabase {
    private static final ConcurrentHashMap<String, PartyData> parties = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, PartyInvitation> invitations = new ConcurrentHashMap<>(); // Key: Invited UUID

    // Party Management
    public static void addParty(PartyData party) {
        parties.put(party.getPartyId(), party);
    }

    public static PartyData getParty(String partyId) {
        return parties.get(partyId);
    }
    
    public static PartyData getPartyByMember(UUID memberUuid) {
        return parties.values().stream()
                .filter(p -> p.getMembers().contains(memberUuid))
                .findFirst()
                .orElse(null);
    }

    public static void removeParty(String partyId) {
        parties.remove(partyId);
    }

    // Invitation Management
    public static void addInvitation(PartyInvitation invitation) {
        invitations.put(invitation.getInvited(), invitation);
    }

    public static PartyInvitation getInvitation(UUID invitedUuid) {
        return invitations.get(invitedUuid);
    }

    public static void removeInvitation(UUID invitedUuid) {
        invitations.remove(invitedUuid);
    }
    
    public static void cleanupExpiredInvitations() {
        List<UUID> expired = invitations.values().stream()
                .filter(PartyInvitation::isExpired)
                .map(PartyInvitation::getInvited)
                .collect(Collectors.toList());
        expired.forEach(invitations::remove);
    }
}
