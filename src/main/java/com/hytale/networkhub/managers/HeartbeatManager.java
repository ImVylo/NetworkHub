package com.hytale.networkhub.managers;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.database.DatabaseManager;
import com.hytale.networkhub.database.models.ServerRecord;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import com.hypixel.hytale.logger.HytaleLogger;
import java.util.logging.Level;

public class HeartbeatManager {
    private final HytaleLogger logger;
    private final DatabaseManager dbManager;
    private final NetworkConfig config;
    private final String serverId;
    private long serverStartTime;

    public HeartbeatManager(HytaleLogger logger, NetworkConfig config, DatabaseManager dbManager) {
        this.logger = logger;
        this.dbManager = dbManager;
        this.config = config;
        this.serverId = config.getConfig().server.serverId;
        this.serverStartTime = System.currentTimeMillis();
    }

    public void sendHeartbeat(int currentPlayers) {
        String sql = """
            INSERT INTO server_health (server_id, status, last_heartbeat, current_players, tps, memory_used_mb, memory_max_mb, cpu_percent, uptime_seconds, consecutive_failures, updated_at)
            VALUES (?, 'ONLINE', CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, 0, CURRENT_TIMESTAMP)
            ON CONFLICT (server_id) DO UPDATE SET
                status = 'ONLINE',
                last_heartbeat = CURRENT_TIMESTAMP,
                current_players = EXCLUDED.current_players,
                tps = EXCLUDED.tps,
                memory_used_mb = EXCLUDED.memory_used_mb,
                memory_max_mb = EXCLUDED.memory_max_mb,
                cpu_percent = EXCLUDED.cpu_percent,
                uptime_seconds = EXCLUDED.uptime_seconds,
                consecutive_failures = 0,
                updated_at = CURRENT_TIMESTAMP
        """;

        // For MySQL/MariaDB
        if (isMySQL()) {
            sql = """
                INSERT INTO server_health (server_id, status, last_heartbeat, current_players, tps, memory_used_mb, memory_max_mb, cpu_percent, uptime_seconds, consecutive_failures, updated_at)
                VALUES (?, 'ONLINE', CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, 0, CURRENT_TIMESTAMP)
                ON DUPLICATE KEY UPDATE
                    status = 'ONLINE',
                    last_heartbeat = CURRENT_TIMESTAMP,
                    current_players = VALUES(current_players),
                    tps = VALUES(tps),
                    memory_used_mb = VALUES(memory_used_mb),
                    memory_max_mb = VALUES(memory_max_mb),
                    cpu_percent = VALUES(cpu_percent),
                    uptime_seconds = VALUES(uptime_seconds),
                    consecutive_failures = 0,
                    updated_at = CURRENT_TIMESTAMP
            """;
        }

        double tps = getTPS();
        long memUsed = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024;
        long memMax = Runtime.getRuntime().maxMemory() / 1024 / 1024;
        double cpu = getCpuUsage();
        long uptime = (System.currentTimeMillis() - serverStartTime) / 1000;

        dbManager.executeUpdateAsync(sql, serverId, currentPlayers, tps, memUsed, memMax, cpu, uptime);
    }

    public void checkServerHealth() {
        NetworkConfig.HeartbeatConfig hbConfig = config.getConfig().heartbeat;

        String sql = """
            UPDATE server_health
            SET consecutive_failures = consecutive_failures + 1,
                status = CASE
                    WHEN consecutive_failures + 1 >= ? THEN 'OFFLINE'
                    WHEN consecutive_failures + 1 >= ? THEN 'DEGRADED'
                    ELSE status
                END,
                updated_at = CURRENT_TIMESTAMP
            WHERE server_id != ?
              AND (EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - last_heartbeat)) > ? OR last_heartbeat IS NULL)
              AND status != 'OFFLINE'
        """;

        // For MySQL/MariaDB
        if (isMySQL()) {
            sql = """
                UPDATE server_health
                SET consecutive_failures = consecutive_failures + 1,
                    status = CASE
                        WHEN consecutive_failures + 1 >= ? THEN 'OFFLINE'
                        WHEN consecutive_failures + 1 >= ? THEN 'DEGRADED'
                        ELSE status
                    END,
                    updated_at = CURRENT_TIMESTAMP
                WHERE server_id != ?
                  AND (TIMESTAMPDIFF(SECOND, last_heartbeat, CURRENT_TIMESTAMP) > ? OR last_heartbeat IS NULL)
                  AND status != 'OFFLINE'
            """;
        }

        int updated = dbManager.executeUpdate(sql,
            hbConfig.failureThreshold,
            hbConfig.failureThreshold - 1,
            serverId,
            hbConfig.timeoutSeconds
        );

        if (updated > 0) {
            logger.at(Level.FINE).log("Marked " + updated + " server(s) as degraded/offline due to missed heartbeats");
        }
    }

    public void markOffline() {
        String sql = "UPDATE server_health SET status = 'OFFLINE', updated_at = CURRENT_TIMESTAMP WHERE server_id = ?";
        dbManager.executeUpdate(sql, serverId);
        logger.at(Level.INFO).log("Marked server as offline: " + serverId);
    }

    private double getTPS() {
        // TODO: Get actual TPS from Hytale server
        // For now, return a placeholder
        return 20.0;
    }

    private double getCpuUsage() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            return osBean.getSystemLoadAverage();
        } catch (Exception e) {
            return 0.0;
        }
    }

    private boolean isMySQL() {
        // Simple check - could be improved
        return !config.getConfig().server.serverId.isEmpty();
    }
}
