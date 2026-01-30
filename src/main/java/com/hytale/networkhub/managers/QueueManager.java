package com.hytale.networkhub.managers;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.database.DatabaseManager;
import com.hytale.networkhub.database.models.QueueEntry;
import com.hytale.networkhub.database.models.ServerRecord;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.hypixel.hytale.logger.HytaleLogger;
import java.util.logging.Level;

public class QueueManager {
    private final HytaleLogger logger;
    private final DatabaseManager dbManager;
    private final NetworkConfig config;
    private final ServerRegistryManager registryManager;
    private final TransferManager transferManager;
    private final PlayerTrackingManager trackingManager;

    // In-memory queue cache
    private final Map<String, PriorityQueue<QueueEntry>> serverQueues = new ConcurrentHashMap<>();

    public QueueManager(HytaleLogger logger, DatabaseManager dbManager, NetworkConfig config,
                       ServerRegistryManager registryManager, TransferManager transferManager,
                       PlayerTrackingManager trackingManager) {
        this.logger = logger;
        this.dbManager = dbManager;
        this.config = config;
        this.registryManager = registryManager;
        this.transferManager = transferManager;
        this.trackingManager = trackingManager;
    }

    public void joinQueue(UUID playerUuid, String targetServerId, int priority) {
        // Check if server actually has space
        ServerRecord server = registryManager.getServerById(targetServerId);
        if (server == null) {
            logger.at(Level.WARNING).log("Attempted to queue for unknown server: " + targetServerId);
            return;
        }

        if (server.getCurrentPlayers() < server.getMaxPlayers()) {
            logger.at(Level.INFO).log("Server has space, skipping queue for player " + playerUuid);
            // TODO: Transfer immediately
            return;
        }

        // Check queue size limit
        int queueSize = getQueueSize(targetServerId);
        if (queueSize >= config.getConfig().queue.maxQueueSize) {
            logger.at(Level.INFO).log("Queue full for server " + targetServerId);
            return;
        }

        // Get player name
        String playerName = getPlayerName(playerUuid);

        // Insert into database
        String sql = """
            INSERT INTO server_queues (server_id, player_uuid, player_name, priority, joined_queue_at, position, notified)
            VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, false)
            ON CONFLICT (server_id, player_uuid) DO UPDATE SET
                priority = EXCLUDED.priority,
                joined_queue_at = CURRENT_TIMESTAMP
        """;

        // For MySQL/MariaDB
        if (isMySQL()) {
            sql = """
                INSERT INTO server_queues (server_id, player_uuid, player_name, priority, joined_queue_at, position, notified)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, false)
                ON DUPLICATE KEY UPDATE
                    priority = VALUES(priority),
                    joined_queue_at = CURRENT_TIMESTAMP
            """;
        }

        dbManager.executeUpdate(sql, targetServerId, playerUuid.toString(), playerName, priority, queueSize + 1);

        // Add to in-memory queue
        QueueEntry entry = new QueueEntry(playerUuid, playerName, targetServerId, priority);
        serverQueues.computeIfAbsent(targetServerId, k -> new PriorityQueue<>()).add(entry);

        logger.at(Level.INFO).log("Added " + playerName + " to queue for " + targetServerId + " (priority: " + priority + ", position: " + (queueSize + 1) + ")");
    }

    public void leaveQueue(UUID playerUuid, String targetServerId) {
        String sql = "DELETE FROM server_queues WHERE player_uuid = ? AND server_id = ?";
        dbManager.executeUpdate(sql, playerUuid.toString(), targetServerId);

        PriorityQueue<QueueEntry> queue = serverQueues.get(targetServerId);
        if (queue != null) {
            queue.removeIf(e -> e.getPlayerUuid().equals(playerUuid));
        }

        logger.at(Level.INFO).log("Removed player " + playerUuid + " from queue for " + targetServerId);
    }

    public void processQueues() {
        for (Map.Entry<String, PriorityQueue<QueueEntry>> entry : serverQueues.entrySet()) {
            String serverId = entry.getKey();
            PriorityQueue<QueueEntry> queue = entry.getValue();

            if (queue.isEmpty()) continue;

            try {
                processServerQueue(serverId, queue);
            } catch (Exception e) {
                logger.at(Level.SEVERE).log("Error processing queue for " + serverId + ": " + e.getMessage());
            }
        }
    }

    private void processServerQueue(String serverId, PriorityQueue<QueueEntry> queue) {
        ServerRecord server = registryManager.getServerById(serverId);
        if (server == null) return;

        int availableSlots = server.getMaxPlayers() - server.getCurrentPlayers();
        if (availableSlots <= 0) return;

        logger.at(Level.FINE).log("Processing queue for " + serverId + ": " + availableSlots + " slots available");

        for (int i = 0; i < availableSlots && !queue.isEmpty(); i++) {
            QueueEntry queueEntry = queue.poll();
            if (queueEntry == null) break;

            UUID playerUuid = queueEntry.getPlayerUuid();

            // Check if player is still online
            var playerLoc = trackingManager.findPlayer(playerUuid);
            if (playerLoc == null) {
                // Player offline, remove from DB
                dbManager.executeUpdate("DELETE FROM server_queues WHERE player_uuid = ?",
                    playerUuid.toString());
                continue;
            }

            // TODO: Get actual Player object and transfer
            // For now just log and remove from DB
            logger.at(Level.INFO).log("Queue slot ready for " + queueEntry.getPlayerName() + " -> " + server.getServerName());

            dbManager.executeUpdate("DELETE FROM server_queues WHERE player_uuid = ? AND server_id = ?",
                playerUuid.toString(), serverId);
        }
    }

    public int getQueuePosition(UUID playerUuid, String serverId) {
        String sql = """
            SELECT COUNT(*) + 1 as position FROM server_queues
            WHERE server_id = ? AND (
                priority > (SELECT priority FROM server_queues WHERE player_uuid = ? AND server_id = ?)
                OR (priority = (SELECT priority FROM server_queues WHERE player_uuid = ? AND server_id = ?)
                    AND joined_queue_at < (SELECT joined_queue_at FROM server_queues WHERE player_uuid = ? AND server_id = ?))
            )
        """;

        Integer position = dbManager.executeQuery(sql, rs -> {
            if (rs.next()) {
                return rs.getInt("position");
            }
            return 0;
        }, serverId, playerUuid.toString(), serverId, playerUuid.toString(), serverId, playerUuid.toString(), serverId);

        return position != null ? position : 0;
    }

    public int getQueueSize(String serverId) {
        String sql = "SELECT COUNT(*) as count FROM server_queues WHERE server_id = ?";
        Integer count = dbManager.executeQuery(sql, rs -> {
            if (rs.next()) {
                return rs.getInt("count");
            }
            return 0;
        }, serverId);
        return count != null ? count : 0;
    }

    public int getTotalQueuedPlayers() {
        String sql = "SELECT COUNT(*) as count FROM server_queues";
        Integer count = dbManager.executeQuery(sql, rs -> {
            if (rs.next()) {
                return rs.getInt("count");
            }
            return 0;
        });
        return count != null ? count : 0;
    }

    private String getPlayerName(UUID playerUuid) {
        var playerLoc = trackingManager.findPlayer(playerUuid);
        return playerLoc != null ? playerLoc.getPlayerName() : "Unknown";
    }

    private boolean isMySQL() {
        return dbManager.isMySQL();
    }
}
