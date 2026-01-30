package com.hytale.networkhub.database.models;

import java.sql.Timestamp;
import java.util.UUID;

public class QueueEntry implements Comparable<QueueEntry> {
    private int queueId;
    private String serverId;
    private UUID playerUuid;
    private String playerName;
    private int priority;
    private Timestamp joinedQueueAt;
    private int position;
    private boolean notified;

    public QueueEntry() {}

    public QueueEntry(UUID playerUuid, String playerName, String serverId, int priority) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.serverId = serverId;
        this.priority = priority;
        this.joinedQueueAt = new Timestamp(System.currentTimeMillis());
    }

    @Override
    public int compareTo(QueueEntry other) {
        // Higher priority first, then earlier join time
        if (this.priority != other.priority) {
            return Integer.compare(other.priority, this.priority);
        }
        return this.joinedQueueAt.compareTo(other.joinedQueueAt);
    }

    // Getters and Setters
    public int getQueueId() { return queueId; }
    public void setQueueId(int queueId) { this.queueId = queueId; }

    public String getServerId() { return serverId; }
    public void setServerId(String serverId) { this.serverId = serverId; }

    public UUID getPlayerUuid() { return playerUuid; }
    public void setPlayerUuid(UUID playerUuid) { this.playerUuid = playerUuid; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public Timestamp getJoinedQueueAt() { return joinedQueueAt; }
    public void setJoinedQueueAt(Timestamp joinedQueueAt) { this.joinedQueueAt = joinedQueueAt; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public boolean isNotified() { return notified; }
    public void setNotified(boolean notified) { this.notified = notified; }
}
