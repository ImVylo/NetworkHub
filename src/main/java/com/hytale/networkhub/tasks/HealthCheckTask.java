package com.hytale.networkhub.tasks;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.database.DatabaseManager;
import com.hypixel.hytale.logger.HytaleLogger;

import java.util.logging.Level;

/**
 * Periodic task that checks for dead servers and marks them offline
 * Runs every 15 seconds to detect servers that missed heartbeats
 */
public class HealthCheckTask implements Runnable {
    private final HytaleLogger logger;
    private final NetworkConfig config;
    private final DatabaseManager dbManager;

    public HealthCheckTask(HytaleLogger logger, NetworkConfig config, DatabaseManager dbManager) {
        this.logger = logger;
        this.config = config;
        this.dbManager = dbManager;
    }

    @Override
    public void run() {
        try {
            int timeoutSeconds = config.getConfig().heartbeat.timeoutSeconds;
            int failureThreshold = config.getConfig().heartbeat.failureThreshold;

            // Increment failure count for servers with old heartbeats
            String incrementSql = """
                UPDATE server_health
                SET consecutive_failures = consecutive_failures + 1,
                    updated_at = CURRENT_TIMESTAMP
                WHERE last_heartbeat < NOW() - INTERVAL '%d seconds'
                  AND status != 'OFFLINE'
            """.formatted(timeoutSeconds);

            // MySQL syntax
            if (isMySQL()) {
                incrementSql = """
                    UPDATE server_health
                    SET consecutive_failures = consecutive_failures + 1,
                        updated_at = CURRENT_TIMESTAMP
                    WHERE last_heartbeat < DATE_SUB(NOW(), INTERVAL %d SECOND)
                      AND status != 'OFFLINE'
                """.formatted(timeoutSeconds);
            }

            int updated = dbManager.executeUpdate(incrementSql);

            // Mark servers as offline if they exceeded failure threshold
            String markOfflineSql = """
                UPDATE server_health
                SET status = 'OFFLINE',
                    updated_at = CURRENT_TIMESTAMP
                WHERE consecutive_failures >= ?
                  AND status != 'OFFLINE'
            """;

            int markedOffline = dbManager.executeUpdate(markOfflineSql, failureThreshold);

            if (markedOffline > 0) {
                logger.at(Level.WARNING).log("Marked %d server(s) as offline due to missed heartbeats", markedOffline);
            }

        } catch (Exception e) {
            logger.at(Level.WARNING).log("Error during health check: %s", e.getMessage());
        }
    }

    private boolean isMySQL() {
        return dbManager.isMySQL();
    }
}
