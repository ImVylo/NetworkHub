package com.hytale.networkhub.database.models;

import java.sql.Timestamp;
import java.util.Objects;

public class ServerRecord {
    private String serverId;
    private String serverName;
    private String host;
    private int port;
    private String serverType;
    private boolean isHub;
    private int hubPriority;
    private int maxPlayers;
    private int currentPlayers;
    private String motd;
    private ServerStatus status;
    private Timestamp registeredAt;
    private Timestamp lastUpdated;

    public ServerRecord() {}

    public ServerRecord(String serverId, String serverName, String host, int port, String serverType) {
        this.serverId = serverId;
        this.serverName = serverName;
        this.host = host;
        this.port = port;
        this.serverType = serverType;
        this.status = ServerStatus.OFFLINE;
    }

    // Getters and Setters
    public String getServerId() { return serverId; }
    public void setServerId(String serverId) { this.serverId = serverId; }

    public String getServerName() { return serverName; }
    public void setServerName(String serverName) { this.serverName = serverName; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getServerType() { return serverType; }
    public void setServerType(String serverType) { this.serverType = serverType; }

    public boolean isHub() { return isHub; }
    public void setHub(boolean hub) { isHub = hub; }

    public int getHubPriority() { return hubPriority; }
    public void setHubPriority(int hubPriority) { this.hubPriority = hubPriority; }

    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }

    public int getCurrentPlayers() { return currentPlayers; }
    public void setCurrentPlayers(int currentPlayers) { this.currentPlayers = currentPlayers; }

    public String getMotd() { return motd; }
    public void setMotd(String motd) { this.motd = motd; }

    public ServerStatus getStatus() { return status; }
    public void setStatus(ServerStatus status) { this.status = status; }

    public Timestamp getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(Timestamp registeredAt) { this.registeredAt = registeredAt; }

    public Timestamp getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Timestamp lastUpdated) { this.lastUpdated = lastUpdated; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerRecord that = (ServerRecord) o;
        return Objects.equals(serverId, that.serverId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverId);
    }

    @Override
    public String toString() {
        return String.format("Server{id='%s', name='%s', status=%s, players=%d/%d}",
            serverId, serverName, status, currentPlayers, maxPlayers);
    }

    public enum ServerStatus {
        ONLINE, DEGRADED, OFFLINE
    }
}
