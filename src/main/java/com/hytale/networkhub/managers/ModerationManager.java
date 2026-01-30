package com.hytale.networkhub.managers;

import com.google.gson.Gson;
import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.database.DatabaseManager;
import com.hytale.networkhub.redis.RedisManager;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Manages cross-server moderation actions
 * Handles bans, kicks, and mutes across the entire network
 */
public class ModerationManager {
    private final Logger logger;
    private final NetworkConfig config;
    private final DatabaseManager dbManager;
    private final RedisManager redisManager;
    private final Gson gson;

    public ModerationManager(Logger logger, NetworkConfig config, DatabaseManager dbManager,
                            RedisManager redisManager, Gson gson) {
        this.logger = logger;
        this.config = config;
        this.dbManager = dbManager;
        this.redisManager = redisManager;
        this.gson = gson;
    }

    /**
     * Ban a player across the entire network
     */
    public void banPlayer(UUID targetUuid, String targetName, UUID moderatorUuid,
                         String moderatorName, String reason, long durationSeconds) {
        // Store in database
        String sql = """
            INSERT INTO moderation_actions (target_uuid, target_name, action_type, moderator_uuid,
                                           moderator_name, reason, server_id, expires_at, created_at)
            VALUES (?, ?, 'BAN', ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;

        String expiresAt = durationSeconds > 0
            ? "CURRENT_TIMESTAMP + INTERVAL '" + durationSeconds + " seconds'"
            : "NULL";

        // PostgreSQL
        if (!isMySQL()) {
            sql = """
                INSERT INTO moderation_actions (target_uuid, target_name, action_type, moderator_uuid,
                                               moderator_name, reason, server_id, expires_at, created_at)
                VALUES (?, ?, 'BAN', ?, ?, ?, ?, CURRENT_TIMESTAMP + INTERVAL '? seconds', CURRENT_TIMESTAMP)
            """;
        } else {
            // MySQL/MariaDB
            sql = """
                INSERT INTO moderation_actions (target_uuid, target_name, action_type, moderator_uuid,
                                               moderator_name, reason, server_id, expires_at, created_at)
                VALUES (?, ?, 'BAN', ?, ?, ?, ?,
                        IF(? > 0, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL ? SECOND), NULL),
                        CURRENT_TIMESTAMP)
            """;
        }

        dbManager.executeUpdateAsync(sql,
            targetUuid.toString(),
            targetName,
            moderatorUuid.toString(),
            moderatorName,
            reason,
            config.getConfig().server.serverId,
            durationSeconds,
            durationSeconds
        );

        // Broadcast to all servers via Redis
        if (redisManager.isEnabled()) {
            Map<String, Object> moderationAction = new HashMap<>();
            moderationAction.put("action", "BAN");
            moderationAction.put("targetUuid", targetUuid.toString());
            moderationAction.put("targetName", targetName);
            moderationAction.put("moderatorName", moderatorName);
            moderationAction.put("reason", reason);
            moderationAction.put("duration", durationSeconds);
            moderationAction.put("timestamp", System.currentTimeMillis());

            redisManager.publish(redisManager.getChannel("moderation"), moderationAction);
        }

        String durationType = durationSeconds > 0
            ? "for " + formatDuration(durationSeconds)
            : "permanently";

        logger.info(String.format("Player %s banned %s by %s: %s",
            targetName, durationType, moderatorName, reason));
    }

    /**
     * Kick a player from the network
     */
    public void kickPlayer(UUID targetUuid, String targetName, UUID moderatorUuid,
                          String moderatorName, String reason) {
        // Log to database
        String sql = """
            INSERT INTO moderation_actions (target_uuid, target_name, action_type, moderator_uuid,
                                           moderator_name, reason, server_id, created_at)
            VALUES (?, ?, 'KICK', ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;

        dbManager.executeUpdateAsync(sql,
            targetUuid.toString(),
            targetName,
            moderatorUuid.toString(),
            moderatorName,
            reason,
            config.getConfig().server.serverId
        );

        // Broadcast to all servers via Redis
        if (redisManager.isEnabled()) {
            Map<String, Object> moderationAction = new HashMap<>();
            moderationAction.put("action", "KICK");
            moderationAction.put("targetUuid", targetUuid.toString());
            moderationAction.put("targetName", targetName);
            moderationAction.put("moderatorName", moderatorName);
            moderationAction.put("reason", reason);
            moderationAction.put("timestamp", System.currentTimeMillis());

            redisManager.publish(redisManager.getChannel("moderation"), moderationAction);
        }

        logger.info(String.format("Player %s kicked by %s: %s",
            targetName, moderatorName, reason));
    }

    /**
     * Mute a player across the network
     */
    public void mutePlayer(UUID targetUuid, String targetName, UUID moderatorUuid,
                          String moderatorName, String reason, long durationSeconds) {
        // Store in database
        String sql = """
            INSERT INTO moderation_actions (target_uuid, target_name, action_type, moderator_uuid,
                                           moderator_name, reason, server_id, expires_at, created_at)
            VALUES (?, ?, 'MUTE', ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;

        if (!isMySQL()) {
            sql = """
                INSERT INTO moderation_actions (target_uuid, target_name, action_type, moderator_uuid,
                                               moderator_name, reason, server_id, expires_at, created_at)
                VALUES (?, ?, 'MUTE', ?, ?, ?, ?, CURRENT_TIMESTAMP + INTERVAL '? seconds', CURRENT_TIMESTAMP)
            """;
        } else {
            sql = """
                INSERT INTO moderation_actions (target_uuid, target_name, action_type, moderator_uuid,
                                               moderator_name, reason, server_id, expires_at, created_at)
                VALUES (?, ?, 'MUTE', ?, ?, ?, ?,
                        DATE_ADD(CURRENT_TIMESTAMP, INTERVAL ? SECOND),
                        CURRENT_TIMESTAMP)
            """;
        }

        dbManager.executeUpdateAsync(sql,
            targetUuid.toString(),
            targetName,
            moderatorUuid.toString(),
            moderatorName,
            reason,
            config.getConfig().server.serverId,
            durationSeconds
        );

        // Broadcast to all servers via Redis
        if (redisManager.isEnabled()) {
            Map<String, Object> moderationAction = new HashMap<>();
            moderationAction.put("action", "MUTE");
            moderationAction.put("targetUuid", targetUuid.toString());
            moderationAction.put("targetName", targetName);
            moderationAction.put("moderatorName", moderatorName);
            moderationAction.put("reason", reason);
            moderationAction.put("duration", durationSeconds);
            moderationAction.put("timestamp", System.currentTimeMillis());

            redisManager.publish(redisManager.getChannel("moderation"), moderationAction);
        }

        logger.info(String.format("Player %s muted for %s by %s: %s",
            targetName, formatDuration(durationSeconds), moderatorName, reason));
    }

    /**
     * Unban a player
     */
    public void unbanPlayer(UUID targetUuid, String targetName, UUID moderatorUuid,
                           String moderatorName) {
        // Remove active bans from database
        String sql = """
            UPDATE moderation_actions
            SET active = false, unbanned_by = ?, unbanned_at = CURRENT_TIMESTAMP
            WHERE target_uuid = ? AND action_type = 'BAN' AND active = true
        """;

        dbManager.executeUpdateAsync(sql,
            moderatorUuid.toString(),
            targetUuid.toString()
        );

        // Broadcast to all servers via Redis
        if (redisManager.isEnabled()) {
            Map<String, Object> moderationAction = new HashMap<>();
            moderationAction.put("action", "UNBAN");
            moderationAction.put("targetUuid", targetUuid.toString());
            moderationAction.put("targetName", targetName);
            moderationAction.put("moderatorName", moderatorName);
            moderationAction.put("timestamp", System.currentTimeMillis());

            redisManager.publish(redisManager.getChannel("moderation"), moderationAction);
        }

        logger.info(String.format("Player %s unbanned by %s", targetName, moderatorName));
    }

    /**
     * Unmute a player
     */
    public void unmutePlayer(UUID targetUuid, String targetName, UUID moderatorUuid,
                            String moderatorName) {
        // Remove active mutes from database
        String sql = """
            UPDATE moderation_actions
            SET active = false, unbanned_by = ?, unbanned_at = CURRENT_TIMESTAMP
            WHERE target_uuid = ? AND action_type = 'MUTE' AND active = true
        """;

        dbManager.executeUpdateAsync(sql,
            moderatorUuid.toString(),
            targetUuid.toString()
        );

        // Broadcast to all servers via Redis
        if (redisManager.isEnabled()) {
            Map<String, Object> moderationAction = new HashMap<>();
            moderationAction.put("action", "UNMUTE");
            moderationAction.put("targetUuid", targetUuid.toString());
            moderationAction.put("targetName", targetName);
            moderationAction.put("moderatorName", moderatorName);
            moderationAction.put("timestamp", System.currentTimeMillis());

            redisManager.publish(redisManager.getChannel("moderation"), moderationAction);
        }

        logger.info(String.format("Player %s unmuted by %s", targetName, moderatorName));
    }

    /**
     * Check if a player is banned
     */
    public boolean isBanned(UUID playerUuid) {
        String sql = """
            SELECT COUNT(*) FROM moderation_actions
            WHERE target_uuid = ? AND action_type = 'BAN' AND active = true
            AND (expires_at IS NULL OR expires_at > CURRENT_TIMESTAMP)
        """;

        return dbManager.queryInt(sql, playerUuid.toString()) > 0;
    }

    /**
     * Check if a player is muted
     */
    public boolean isMuted(UUID playerUuid) {
        String sql = """
            SELECT COUNT(*) FROM moderation_actions
            WHERE target_uuid = ? AND action_type = 'MUTE' AND active = true
            AND (expires_at IS NULL OR expires_at > CURRENT_TIMESTAMP)
        """;

        return dbManager.queryInt(sql, playerUuid.toString()) > 0;
    }

    /**
     * Get ban reason for a player
     */
    public String getBanReason(UUID playerUuid) {
        String sql = """
            SELECT reason FROM moderation_actions
            WHERE target_uuid = ? AND action_type = 'BAN' AND active = true
            AND (expires_at IS NULL OR expires_at > CURRENT_TIMESTAMP)
            ORDER BY created_at DESC LIMIT 1
        """;

        return dbManager.queryString(sql, playerUuid.toString());
    }

    /**
     * Handle incoming moderation actions from Redis
     */
    public void handleModerationAction(String json, Collection<Player> onlinePlayers) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> action = gson.fromJson(json, Map.class);

            String actionType = (String) action.get("action");
            String targetUuidStr = (String) action.get("targetUuid");
            String targetName = (String) action.get("targetName");
            String moderatorName = (String) action.get("moderatorName");
            String reason = (String) action.get("reason");

            UUID targetUuid = UUID.fromString(targetUuidStr);

            // Find target player on this server
            Player target = null;
            for (Player player : onlinePlayers) {
                if (player.getPlayerRef().getUuid().equals(targetUuid)) {
                    target = player;
                    break;
                }
            }

            if (target == null) {
                return; // Player not on this server
            }

            // Execute the moderation action
            switch (actionType) {
                case "BAN":
                    target.sendMessage("§c§lYou have been banned from the network!");
                    target.sendMessage("§cReason: §f" + reason);
                    target.sendMessage("§cBanned by: §f" + moderatorName);
                    // TODO: Kick player when API is available
                    // target.kick("Banned: " + reason);
                    break;

                case "KICK":
                    target.sendMessage("§c§lYou have been kicked from the network!");
                    target.sendMessage("§cReason: §f" + reason);
                    target.sendMessage("§cKicked by: §f" + moderatorName);
                    // TODO: Kick player when API is available
                    // target.kick("Kicked: " + reason);
                    break;

                case "MUTE":
                    target.sendMessage("§c§lYou have been muted!");
                    target.sendMessage("§cReason: §f" + reason);
                    target.sendMessage("§cMuted by: §f" + moderatorName);
                    break;

                case "UNMUTE":
                    target.sendMessage("§a§lYou have been unmuted!");
                    target.sendMessage("§aUnmuted by: §f" + moderatorName);
                    break;

                case "UNBAN":
                    // No action needed for unbans (player will be able to join)
                    break;
            }

        } catch (Exception e) {
            logger.warning("Failed to handle moderation action: " + e.getMessage());
        }
    }

    /**
     * Format duration in human-readable format
     */
    private String formatDuration(long seconds) {
        if (seconds < 60) {
            return seconds + " seconds";
        } else if (seconds < 3600) {
            return (seconds / 60) + " minutes";
        } else if (seconds < 86400) {
            return (seconds / 3600) + " hours";
        } else {
            return (seconds / 86400) + " days";
        }
    }

    /**
     * Check if database is MySQL/MariaDB
     */
    private boolean isMySQL() {
        String dbType = config.getConfig().database.type.toLowerCase();
        return dbType.equals("mysql") || dbType.equals("mariadb");
    }
}
