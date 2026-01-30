package com.hytale.networkhub.listeners;

import com.hytale.networkhub.managers.PlayerTrackingManager;
import com.hytale.networkhub.redis.RedisManager;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class PlayerQuitListener {
    private final Logger logger;
    private final PlayerTrackingManager trackingManager;
    private final RedisManager redisManager;
    private final String serverId;

    public PlayerQuitListener(Logger logger, PlayerTrackingManager trackingManager,
                             RedisManager redisManager, String serverId) {
        this.logger = logger;
        this.trackingManager = trackingManager;
        this.redisManager = redisManager;
        this.serverId = serverId;
    }

    public void onPlayerQuit(Player player) {
        try {
            // Update last seen in database
            trackingManager.trackQuit(player.getUniqueId());

            // Publish to Redis
            if (redisManager.isEnabled()) {
                Map<String, Object> message = new HashMap<>();
                message.put("playerUuid", player.getUniqueId().toString());
                message.put("playerName", player.getUsername());
                message.put("serverId", serverId);
                message.put("timestamp", System.currentTimeMillis());

                redisManager.publish(redisManager.getChannel("playerQuit"), message);
            }

            logger.fine("Player quit: " + player.getUsername());

        } catch (Exception e) {
            logger.severe("Error handling player quit: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
