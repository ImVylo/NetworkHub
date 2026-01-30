package com.hytale.networkhub.managers;

import com.google.gson.Gson;
import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.database.DatabaseManager;
import com.hytale.networkhub.database.models.Announcement;
import com.hytale.networkhub.redis.RedisManager;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.Collection;
import java.util.logging.Logger;

public class AnnouncementManager {
    private final Logger logger;
    private final DatabaseManager dbManager;
    private final RedisManager redisManager;
    private final NetworkConfig config;
    private final Gson gson;

    public AnnouncementManager(Logger logger, DatabaseManager dbManager, RedisManager redisManager,
                              NetworkConfig config, Gson gson) {
        this.logger = logger;
        this.dbManager = dbManager;
        this.redisManager = redisManager;
        this.config = config;
        this.gson = gson;
    }

    public void createAnnouncement(Announcement announcement) {
        // Validate
        if (announcement.getDurationSeconds() > config.getConfig().announcements.maxDurationSeconds) {
            throw new IllegalArgumentException("Duration exceeds maximum");
        }

        // Store in database if enabled
        if (config.getConfig().announcements.logToDatabase) {
            String targetServersStr = String.join(",", announcement.getTargetServers());

            String sql = """
                INSERT INTO announcements (created_by_uuid, created_by_name, title, subtitle, action_bar,
                                          display_type, target_servers, target_permissions, duration_seconds,
                                          priority, sound, created_at, expires_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '? seconds')
            """;

            // For MySQL/MariaDB
            if (isMySQL()) {
                sql = """
                    INSERT INTO announcements (created_by_uuid, created_by_name, title, subtitle, action_bar,
                                              display_type, target_servers, target_permissions, duration_seconds,
                                              priority, sound, created_at, expires_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL ? SECOND))
                """;
            }

            dbManager.executeUpdateAsync(sql,
                announcement.getCreatorUuid().toString(),
                announcement.getCreatorName(),
                announcement.getTitle(),
                announcement.getSubtitle(),
                announcement.getActionBar(),
                announcement.getDisplayType().name(),
                targetServersStr,
                announcement.getTargetPermissions(),
                announcement.getDurationSeconds(),
                announcement.getPriority(),
                announcement.getSound(),
                announcement.getDurationSeconds()
            );
        }

        // Broadcast via Redis
        if (redisManager.isEnabled()) {
            redisManager.publish(redisManager.getChannel("announcement"), announcement);
        }

        logger.info("Announcement created by " + announcement.getCreatorName() + ": " + announcement.getTitle());
    }

    public void handleIncomingAnnouncement(String json, Collection<Player> onlinePlayers) {
        try {
            Announcement announcement = gson.fromJson(json, Announcement.class);

            // Filter by target servers
            if (!announcement.getTargetServers().isEmpty() &&
                !announcement.getTargetServers().contains(config.getConfig().server.serverId)) {
                return; // Not for this server
            }

            // Display to all eligible players
            for (Player player : onlinePlayers) {
                // Check permission
                if (announcement.getTargetPermissions() != null &&
                    !announcement.getTargetPermissions().isEmpty()) {
                    // TODO: Check player permission
                    // if (!player.hasPermission(announcement.getTargetPermissions())) continue;
                }

                displayAnnouncement(player, announcement);
            }

        } catch (Exception e) {
            logger.severe("Error handling announcement: " + e.getMessage());
        }
    }

    private void displayAnnouncement(Player player, Announcement announcement) {
        // TODO: Implement title/subtitle/action bar display using Hytale API
        // For now, send as chat message
        String message = "";

        switch (announcement.getDisplayType()) {
            case TITLE:
                message = "§6§l" + announcement.getTitle();
                break;
            case SUBTITLE:
                message = "§7" + announcement.getSubtitle();
                break;
            case ACTIONBAR:
                message = "§e" + announcement.getActionBar();
                break;
            case ALL:
                message = "§6§l" + announcement.getTitle() + "\n§7" + announcement.getSubtitle();
                break;
            case POPUP:
                // TODO: Open custom GUI
                message = "§6§l" + announcement.getTitle();
                break;
        }

        player.sendMessage(message);

        // TODO: Play sound if specified
        // if (announcement.getSound() != null) {
        //     player.playSound(announcement.getSound());
        // }
    }

    private boolean isMySQL() {
        return true; // Simplified
    }
}
