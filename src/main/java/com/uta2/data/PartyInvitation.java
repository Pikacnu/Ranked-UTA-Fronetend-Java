package com.uta2.data;

import java.util.UUID;

public class PartyInvitation {
    private final UUID inviter;
    private final UUID invited;
    private final long timestamp;

    public PartyInvitation(UUID inviter, UUID invited) {
        this.inviter = inviter;
        this.invited = invited;
        this.timestamp = System.currentTimeMillis();
    }

    public UUID getInviter() { return inviter; }
    public UUID getInvited() { return invited; }
    public boolean isExpired() {
        // 30 seconds expiration time
        return (System.currentTimeMillis() - timestamp) > 30000;
    }
}
