package com.hytale.networkhub.listeners;

import com.google.gson.Gson;
import com.hytale.networkhub.managers.PlayerTrackingManager;
import com.hytale.networkhub.redis.RedisManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;

import java.util.HashMap;
import java.util.Map;
import com.hypixel.hytale.logger.HytaleLogger;
import java.util.logging.Level;

public class PlayerJoinListener {
    private final HytaleLogger logger;
    private final PlayerTrackingManager trackingManager;
    private final RedisManager redisManager;
    private final Gson gson;
    private final String serverId;

    public PlayerJoinListener(HytaleLogger logger, PlayerTrackingManager trackingManager,
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
            trackingManager.trackJoin(player.getPlayerRef().getUuid(), player.getPlayerRef().getUsername());

            // Publish to Redis for real-time updates
            if (redisManager.isEnabled()) {
                Map<String, Object> message = new HashMap<>();
                message.put("playerUuid", player.getPlayerRef().getUuid().toString());
                message.put("playerName", player.getPlayerRef().getUsername());
                message.put("serverId", serverId);
                message.put("timestamp", System.currentTimeMillis());

                redisManager.publish(redisManager.getChannel("playerJoin"), message);
            }

            logger.at(Level.FINE).log("Player joined: " + player.getPlayerRef().getUsername());

        } catch (Exception e) {
            logger.at(Level.SEVERE).log("Error handling player join: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
