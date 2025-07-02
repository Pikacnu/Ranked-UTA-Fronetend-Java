package com.uta2.data;

import java.util.List;
import java.util.UUID;

public class PartyData {
    private final String partyId;
    private UUID leader;
    private final List<UUID> members;

    public PartyData(String partyId, UUID leader, List<UUID> members) {
        this.partyId = partyId;
        this.leader = leader;
        this.members = members;
    }

    // Getters and Setters
    public String getPartyId() { return partyId; }
    public UUID getLeader() { return leader; }
    public void setLeader(UUID leader) { this.leader = leader; }
    public List<UUID> getMembers() { return members; }
}
