package com.hytale.networkhub.listeners;

import com.google.gson.Gson;
import com.hytale.networkhub.managers.PlayerTrackingManager;
import com.hytale.networkhub.redis.RedisManager;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class PlayerJoinListener {
    private final Logger logger;
    private final PlayerTrackingManager trackingManager;
    private final RedisManager redisManager;
    private final Gson gson;
    private final String serverId;

    public PlayerJoinListener(Logger logger, PlayerTrackingManager trackingManager,
                             RedisManager redisManager, Gson gson, String serverId) {
        this.logger = logger;
        this.trackingManager = trackingManager;
        this.redisManager = redisManager;
        this.gson = gson;
        this.serverId = serverId;
    }

    public void onPlayerJoin(Player player) {
        try {
            // Track in database
            trackingManager.trackJoin(player.getUniqueId(), player.getUsername());

            // Publish to Redis for real-time updates
            if (redisManager.isEnabled()) {
                Map<String, Object> message = new HashMap<>();
                message.put("playerUuid", player.getUniqueId().toString());
                message.put("playerName", player.getUsername());
                message.put("serverId", serverId);
                message.put("timestamp", System.currentTimeMillis());

                redisManager.publish(redisManager.getChannel("playerJoin"), message);
            }

            logger.fine("Player joined: " + player.getUsername());

        } catch (Exception e) {
            logger.severe("Error handling player join: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
