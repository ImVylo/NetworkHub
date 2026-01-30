package com.hytale.networkhub.managers;

import com.google.gson.Gson;
import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.database.DatabaseManager;
import com.hytale.networkhub.database.models.PlayerLocation;
import com.hytale.networkhub.redis.RedisManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.hypixel.hytale.logger.HytaleLogger;
import java.util.logging.Level;

public class MessagingManager {
    private final HytaleLogger logger;
    private final DatabaseManager dbManager;
    private final RedisManager redisManager;
    private final PlayerTrackingManager trackingManager;
    private final NetworkConfig config;
    private final Gson gson;

    private final Map<UUID, UUID> lastConversations = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> unreadCounts = new ConcurrentHashMap<>();

    public MessagingManager(HytaleLogger logger, DatabaseManager dbManager, RedisManager redisManager,
                           PlayerTrackingManager trackingManager, NetworkConfig config, Gson gson) {
        this.logger = logger;
        this.dbManager = dbManager;
        this.redisManager = redisManager;
        this.trackingManager = trackingManager;
        this.config = config;
        this.gson = gson;
    }

    public boolean sendDirectMessage(UUID senderUuid, String senderName, UUID recipientUuid, String message) {
        if (!config.getConfig().messaging.directMessagingEnabled) {
            return false;
        }

        // Find recipient server
        PlayerLocation recipientLoc = trackingManager.findPlayer(recipientUuid);
        if (recipientLoc == null) {
            logger.at(Level.INFO).log("Player not found: " + recipientUuid);
            return false;
        }

        // Store last conversation for /reply
        lastConversations.put(senderUuid, recipientUuid);
        lastConversations.put(recipientUuid, senderUuid);

        // Publish to Redis
        if (redisManager.isEnabled()) {
            Map<String, Object> dmMessage = new HashMap<>();
            dmMessage.put("senderUuid", senderUuid.toString());
            dmMessage.put("senderName", senderName);
            dmMessage.put("recipientUuid", recipientUuid.toString());
            dmMessage.put("content", message);
            dmMessage.put("serverId", config.getConfig().server.serverId);
            dmMessage.put("timestamp", System.currentTimeMillis());

            redisManager.publish(redisManager.getChannel("directMessage"), dmMessage);
        }

        // Persist to database if enabled
        if (config.getConfig().messaging.persistChatHistory) {
            persistChatMessage(senderUuid, senderName, recipientUuid, "DIRECT", message);
        }

        logger.at(Level.FINE).log("Direct message from " + senderName + " to " + recipientUuid);
        return true;
    }

    public UUID getLastConversationPartner(UUID playerUuid) {
        return lastConversations.get(playerUuid);
    }

    public int getUnreadCount(UUID playerUuid) {
        return unreadCounts.getOrDefault(playerUuid, 0);
    }

    public void incrementUnreadCount(UUID playerUuid) {
        unreadCounts.merge(playerUuid, 1, Integer::sum);
    }

    public void clearUnreadCount(UUID playerUuid) {
        unreadCounts.remove(playerUuid);
    }

    private void persistChatMessage(UUID senderUuid, String senderName, UUID recipientUuid,
                                   String messageType, String content) {
        String sql = """
            INSERT INTO chat_messages (player_uuid, player_name, server_id, message_type, recipient_uuid, content, sent_at)
            VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;

        dbManager.executeUpdateAsync(sql,
            senderUuid.toString(),
            senderName,
            config.getConfig().server.serverId,
            messageType,
            recipientUuid != null ? recipientUuid.toString() : null,
            content
        );
    }
}
