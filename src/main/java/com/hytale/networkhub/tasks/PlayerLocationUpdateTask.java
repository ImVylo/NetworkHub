package com.hytale.networkhub.tasks;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.managers.PlayerTrackingManager;
import com.hypixel.hytale.logger.HytaleLogger;

import java.util.logging.Level;

/**
 * Periodic task that updates player locations in the database
 * Runs every 30 seconds to keep location data fresh
 */
public class PlayerLocationUpdateTask implements Runnable {
    private final HytaleLogger logger;
    private final NetworkConfig config;
    private final PlayerTrackingManager trackingManager;

    public PlayerLocationUpdateTask(HytaleLogger logger, NetworkConfig config, PlayerTrackingManager trackingManager) {
        this.logger = logger;
        this.config = config;
        this.trackingManager = trackingManager;
    }

    @Override
    public void run() {
        try {
            // TODO: Get all online players and update their locations
            // This would iterate through all online players and call:
            // trackingManager.updateLocation(playerUuid, worldName, x, y, z);

            logger.at(Level.FINE).log("Player location update task completed");
        } catch (Exception e) {
            logger.at(Level.WARNING).log("Error updating player locations: %s", e.getMessage());
        }
    }
}
