package com.hytale.networkhub.tasks;

import com.hytale.networkhub.managers.HeartbeatManager;

public class HeartbeatTask implements Runnable {
    private final HeartbeatManager heartbeatManager;
    private final int currentPlayers;

    public HeartbeatTask(HeartbeatManager heartbeatManager, int currentPlayers) {
        this.heartbeatManager = heartbeatManager;
        this.currentPlayers = currentPlayers;
    }

    @Override
    public void run() {
        try {
            heartbeatManager.sendHeartbeat(currentPlayers);
        } catch (Exception e) {
            // Silently fail - don't spam logs
        }
    }
}
