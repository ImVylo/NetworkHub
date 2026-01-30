package com.hytale.networkhub.database.models;

public class TeleporterData {
    private int teleporterId;
    private String serverId;
    private String worldName;
    private int x, y, z;
    private String destinationServerId;
    private String destinationWorld;
    private Double destinationX, destinationY, destinationZ;
    private String displayName;
    private String permission;
    private int cooldownSeconds;
    private boolean enabled;

    public TeleporterData() {}

    // Getters and Setters
    public int getTeleporterId() { return teleporterId; }
    public void setTeleporterId(int teleporterId) { this.teleporterId = teleporterId; }

    public String getServerId() { return serverId; }
    public void setServerId(String serverId) { this.serverId = serverId; }

    public String getWorldName() { return worldName; }
    public void setWorldName(String worldName) { this.worldName = worldName; }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getZ() { return z; }
    public void setZ(int z) { this.z = z; }

    public String getDestinationServerId() { return destinationServerId; }
    public void setDestinationServerId(String destinationServerId) { this.destinationServerId = destinationServerId; }

    public String getDestinationWorld() { return destinationWorld; }
    public void setDestinationWorld(String destinationWorld) { this.destinationWorld = destinationWorld; }

    public Double getDestinationX() { return destinationX; }
    public void setDestinationX(Double destinationX) { this.destinationX = destinationX; }

    public Double getDestinationY() { return destinationY; }
    public void setDestinationY(Double destinationY) { this.destinationY = destinationY; }

    public Double getDestinationZ() { return destinationZ; }
    public void setDestinationZ(Double destinationZ) { this.destinationZ = destinationZ; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }

    public int getCooldownSeconds() { return cooldownSeconds; }
    public void setCooldownSeconds(int cooldownSeconds) { this.cooldownSeconds = cooldownSeconds; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
