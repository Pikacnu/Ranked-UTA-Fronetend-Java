package com.uta2.data;

import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private final String minecraftId;
    private int score;
    private String partyId;
    private boolean isQueuing;

    public PlayerData(UUID uuid, String minecraftId) {
        this.uuid = uuid;
        this.minecraftId = minecraftId;
        this.score = 0;
        this.partyId = null;
        this.isQueuing = false;
    }

    // Getters and Setters
    public UUID getUuid() { return uuid; }
    public String getMinecraftId() { return minecraftId; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getPartyId() { return partyId; }
    public void setPartyId(String partyId) { this.partyId = partyId; }
    public boolean isQueuing() { return isQueuing; }
    public void setQueuing(boolean queuing) { isQueuing = queuing; }
}
