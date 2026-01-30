package com.hytale.networkhub.tasks;

import com.hytale.networkhub.managers.PlayerTrackingManager;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.Collection;

public class PlayerLocationUpdateTask implements Runnable {
    private final PlayerTrackingManager trackingManager;
    private final Collection<Player> onlinePlayers;

    public PlayerLocationUpdateTask(PlayerTrackingManager trackingManager, Collection<Player> onlinePlayers) {
        this.trackingManager = trackingManager;
        this.onlinePlayers = onlinePlayers;
    }

    @Override
    public void run() {
        try {
            for (Player player : onlinePlayers) {
                if (player != null && player.isOnline()) {
                    // TODO: Get actual world and position from player
                    // trackingManager.updateLocation(player.getUniqueId(), worldName, x, y, z);
                }
            }
        } catch (Exception e) {
            // Silently fail - don't spam logs
        }
    }
}
