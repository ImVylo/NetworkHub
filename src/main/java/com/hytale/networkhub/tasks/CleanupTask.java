package com.hytale.networkhub.tasks;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.database.DatabaseManager;

import java.util.logging.Logger;

/**
 * Periodic cleanup task that removes stale data from the database
 * Runs every 5 minutes to keep the database clean
 */
public class CleanupTask implements Runnable {
    private final Logger logger;
    private final NetworkConfig config;
    private final DatabaseManager dbManager;

    public CleanupTask(Logger logger, NetworkConfig config, DatabaseManager dbManager) {
        this.logger = logger;
        this.config = config;
        this.dbManager = dbManager;
    }

    @Override
    public void run() {
        try {
            logger.fine("Running database cleanup task...");

            int totalCleaned = 0;

            // Clean up old player locations (players offline for more than 7 days)
            totalCleaned += cleanupOldPlayerLocations();

            // Clean up expired queue entries
            totalCleaned += cleanupExpiredQueueEntries();

            // Clean up old chat messages (if persistence is enabled)
            if (config.getConfig().messaging.persistChatHistory) {
                totalCleaned += cleanupOldChatMessages();
            }

            // Clean up old announcements (expired for more than 24 hours)
            if (config.getConfig().announcements.logToDatabase) {
                totalCleaned += cleanupOldAnnouncements();
            }

            // Clean up old transfer history (older than 30 days)
            totalCleaned += cleanupOldTransferHistory();

            if (totalCleaned > 0) {
                logger.info("Cleanup task completed: removed " + totalCleaned + " stale records");
            } else {
                logger.fine("Cleanup task completed: no stale records found");
            }

        } catch (Exception e) {
            logger.warning("Error during cleanup task: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Remove player locations for players who have been offline for more than 7 days
     */
    private int cleanupOldPlayerLocations() {
        String sql = """
            DELETE FROM player_locations
            WHERE last_seen < NOW() - INTERVAL '7 days'
        """;

        // MySQL/MariaDB syntax
        if (isMySQL()) {
            sql = """
                DELETE FROM player_locations
                WHERE last_seen < DATE_SUB(NOW(), INTERVAL 7 DAY)
            """;
        }

        int deleted = dbManager.executeUpdate(sql);
        if (deleted > 0) {
            logger.fine("Cleaned up " + deleted + " old player location records");
        }
        return deleted;
    }

    /**
     * Remove expired queue entries (queued for more than 1 hour)
     */
    private int cleanupExpiredQueueEntries() {
        String sql = """
            DELETE FROM server_queues
            WHERE joined_queue_at < NOW() - INTERVAL '1 hour'
        """;

        // MySQL/MariaDB syntax
        if (isMySQL()) {
            sql = """
                DELETE FROM server_queues
                WHERE joined_queue_at < DATE_SUB(NOW(), INTERVAL 1 HOUR)
            """;
        }

        int deleted = dbManager.executeUpdate(sql);
        if (deleted > 0) {
            logger.fine("Cleaned up " + deleted + " expired queue entries");
        }
        return deleted;
    }

    /**
     * Remove old chat messages based on retention period
     */
    private int cleanupOldChatMessages() {
        int retentionDays = config.getConfig().messaging.chatHistoryDays;

        String sql = """
            DELETE FROM chat_messages
            WHERE sent_at < NOW() - INTERVAL '? days'
        """;

        // MySQL/MariaDB syntax
        if (isMySQL()) {
            sql = """
                DELETE FROM chat_messages
                WHERE sent_at < DATE_SUB(NOW(), INTERVAL ? DAY)
            """;
        }

        int deleted = dbManager.executeUpdate(sql, retentionDays);
        if (deleted > 0) {
            logger.fine("Cleaned up " + deleted + " old chat messages");
        }
        return deleted;
    }

    /**
     * Remove old announcements that expired more than 24 hours ago
     */
    private int cleanupOldAnnouncements() {
        String sql = """
            DELETE FROM announcements
            WHERE expires_at < NOW() - INTERVAL '24 hours'
        """;

        // MySQL/MariaDB syntax
        if (isMySQL()) {
            sql = """
                DELETE FROM announcements
                WHERE expires_at < DATE_SUB(NOW(), INTERVAL 24 HOUR)
            """;
        }

        int deleted = dbManager.executeUpdate(sql);
        if (deleted > 0) {
            logger.fine("Cleaned up " + deleted + " old announcements");
        }
        return deleted;
    }

    /**
     * Remove old transfer history records (older than 30 days)
     */
    private int cleanupOldTransferHistory() {
        String sql = """
            DELETE FROM transfer_history
            WHERE transferred_at < NOW() - INTERVAL '30 days'
        """;

        // MySQL/MariaDB syntax
        if (isMySQL()) {
            sql = """
                DELETE FROM transfer_history
                WHERE transferred_at < DATE_SUB(NOW(), INTERVAL 30 DAY)
            """;
        }

        int deleted = dbManager.executeUpdate(sql);
        if (deleted > 0) {
            logger.fine("Cleaned up " + deleted + " old transfer history records");
        }
        return deleted;
    }

    /**
     * Check if database is MySQL/MariaDB
     */
    private boolean isMySQL() {
        String dbType = config.getConfig().database.type.toLowerCase();
        return dbType.equals("mysql") || dbType.equals("mariadb");
    }
}
