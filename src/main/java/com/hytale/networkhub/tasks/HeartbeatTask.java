package com.hytale.networkhub.tasks;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.managers.HeartbeatManager;
import com.hypixel.hytale.logger.HytaleLogger;

import java.util.logging.Level;

public class HeartbeatTask implements Runnable {
    private final HytaleLogger logger;
    private final NetworkConfig config;
    private final HeartbeatManager heartbeatManager;

    public HeartbeatTask(HytaleLogger logger, NetworkConfig config, HeartbeatManager heartbeatManager) {
        this.logger = logger;
        this.config = config;
        this.heartbeatManager = heartbeatManager;
    }

    @Override
    public void run() {
        try {
            // TODO: Get actual player count from server
            int currentPlayers = 0;
            heartbeatManager.sendHeartbeat(currentPlayers);
        } catch (Exception e) {
            logger.at(Level.WARNING).log("Error sending heartbeat: %s", e.getMessage());
        }
    }
}
