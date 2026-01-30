package com.hytale.networkhub.listeners;

import com.hytale.networkhub.managers.PlayerTrackingManager;
import com.hytale.networkhub.redis.RedisManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.Message;

import java.util.HashMap;
import java.util.Map;
import com.hypixel.hytale.logger.HytaleLogger;
import java.util.logging.Level;

public class PlayerQuitListener {
    private final HytaleLogger logger;
    private final PlayerTrackingManager trackingManager;
    private final RedisManager redisManager;
    private final String serverId;

    public PlayerQuitListener(HytaleLogger logger, PlayerTrackingManager trackingManager,
                             RedisManager redisManager, String serverId) {
        this.logger = logger;
        this.trackingManager = trackingManager;
        this.redisManager = redisManager;
        this.serverId = serverId;
    }

    public void onPlayerQuit(PlayerRef playerRef) {
        try {
            // Update last seen in database
            trackingManager.trackQuit(playerRef.getUuid());

            // Publish to Redis
            if (redisManager.isEnabled()) {
                Map<String, Object> message = new HashMap<>();
                message.put("playerUuid", playerRef.getUuid().toString());
                message.put("playerName", playerRef.getUsername());
                message.put("serverId", serverId);
                message.put("timestamp", System.currentTimeMillis());

                redisManager.publish(redisManager.getChannel("playerQuit"), message);
            }

            logger.at(Level.FINE).log("Player quit: " + playerRef.getUsername());

        } catch (Exception e) {
            logger.at(Level.SEVERE).log("Error handling player quit: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
