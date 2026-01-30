package com.hytale.networkhub.tasks;

import com.hytale.networkhub.managers.HeartbeatManager;

public class HealthCheckTask implements Runnable {
    private final HeartbeatManager heartbeatManager;

    public HealthCheckTask(HeartbeatManager heartbeatManager) {
        this.heartbeatManager = heartbeatManager;
    }

    @Override
    public void run() {
        try {
            heartbeatManager.checkServerHealth();
        } catch (Exception e) {
            // Silently fail
        }
    }
}
