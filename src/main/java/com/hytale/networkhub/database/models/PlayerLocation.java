package com.hytale.networkhub.database.models;

import java.sql.Timestamp;
import java.util.UUID;

public class PlayerLocation {
    private UUID playerUuid;
    private String playerName;
    private String serverId;
    private Timestamp joinedAt;
    private Timestamp lastSeen;
    private String worldName;
    private double x, y, z;

    public PlayerLocation() {}

    public PlayerLocation(UUID playerUuid, String playerName, String serverId) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.serverId = serverId;
        this.joinedAt = new Timestamp(System.currentTimeMillis());
        this.lastSeen = new Timestamp(System.currentTimeMillis());
    }

    // Getters and Setters
    public UUID getPlayerUuid() { return playerUuid; }
    public void setPlayerUuid(UUID playerUuid) { this.playerUuid = playerUuid; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public String getServerId() { return serverId; }
    public void setServerId(String serverId) { this.serverId = serverId; }

    public Timestamp getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Timestamp joinedAt) { this.joinedAt = joinedAt; }

    public Timestamp getLastSeen() { return lastSeen; }
    public void setLastSeen(Timestamp lastSeen) { this.lastSeen = lastSeen; }

    public String getWorldName() { return worldName; }
    public void setWorldName(String worldName) { this.worldName = worldName; }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getZ() { return z; }
    public void setZ(double z) { this.z = z; }
}
