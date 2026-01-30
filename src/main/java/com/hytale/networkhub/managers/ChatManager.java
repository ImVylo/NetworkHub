package com.hytale.networkhub.managers;

import com.google.gson.Gson;
import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.database.DatabaseManager;
import com.hytale.networkhub.redis.RedisManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.hypixel.hytale.logger.HytaleLogger;
import java.util.logging.Level;

public class ChatManager {
    private final HytaleLogger logger;
    private final DatabaseManager dbManager;
    private final RedisManager redisManager;
    private final NetworkConfig config;
    private final Gson gson;

    public ChatManager(HytaleLogger logger, DatabaseManager dbManager, RedisManager redisManager,
                      NetworkConfig config, Gson gson) {
        this.logger = logger;
        this.dbManager = dbManager;
        this.redisManager = redisManager;
        this.config = config;
        this.gson = gson;
    }

    public void sendGlobalMessage(UUID senderUuid, String senderName, String message) {
        if (!config.getConfig().messaging.globalChatEnabled) {
            return;
        }

        if (redisManager.isEnabled()) {
            Map<String, Object> chatMessage = new HashMap<>();
            chatMessage.put("senderUuid", senderUuid.toString());
            chatMessage.put("senderName", senderName);
            chatMessage.put("serverId", config.getConfig().server.serverId);
            chatMessage.put("messageType", "GLOBAL");
            chatMessage.put("content", message);
            chatMessage.put("timestamp", System.currentTimeMillis());

            redisManager.publish(redisManager.getChannel("chatMessage"), chatMessage);
        }

        // Persist if enabled
        if (config.getConfig().messaging.persistChatHistory) {
            persistChatMessage(senderUuid, senderName, "GLOBAL", message);
        }

        logger.at(Level.FINE).log("Global chat from " + senderName + ": " + message);
    }

    public void sendStaffMessage(UUID senderUuid, String senderName, String message) {
        if (!config.getConfig().messaging.staffChatEnabled) {
            return;
        }

        if (redisManager.isEnabled()) {
            Map<String, Object> chatMessage = new HashMap<>();
            chatMessage.put("senderUuid", senderUuid.toString());
            chatMessage.put("senderName", senderName);
            chatMessage.put("serverId", config.getConfig().server.serverId);
            chatMessage.put("messageType", "STAFF");
            chatMessage.put("content", message);
            chatMessage.put("timestamp", System.currentTimeMillis());

            redisManager.publish(redisManager.getChannel("staffChat"), chatMessage);
        }

        // Persist if enabled
        if (config.getConfig().messaging.persistChatHistory) {
            persistChatMessage(senderUuid, senderName, "STAFF", message);
        }

        logger.at(Level.FINE).log("Staff chat from " + senderName + ": " + message);
    }

    private void persistChatMessage(UUID senderUuid, String senderName, String messageType, String content) {
        String sql = """
            INSERT INTO chat_messages (player_uuid, player_name, server_id, message_type, content, sent_at)
            VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;

        dbManager.executeUpdateAsync(sql,
            senderUuid.toString(),
            senderName,
            config.getConfig().server.serverId,
            messageType,
            content
        );
    }
}
