package com.hytale.networkhub.managers;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.database.DatabaseManager;
import com.hytale.networkhub.database.models.ServerRecord;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class TransferManager {
    private final Logger logger;
    private final DatabaseManager dbManager;
    private final NetworkConfig config;

    public TransferManager(Logger logger, DatabaseManager dbManager, NetworkConfig config) {
        this.logger = logger;
        this.dbManager = dbManager;
        this.config = config;
    }

    public CompletableFuture<Boolean> transferPlayer(PlayerRef playerRef, ServerRecord destination,
                                                     TransferType type, String reason) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Validate destination
                if (destination.getStatus() != ServerRecord.ServerStatus.ONLINE) {
                    logger.warning("Cannot transfer player to offline server: " + destination.getServerName());
                    return false;
                }

                UUID playerUuid = playerRef.getUuid();
                String playerName = playerRef.getUsername();

                logger.info(String.format("Transferring player %s to %s (%s:%d)",
                    playerName, destination.getServerName(), destination.getHost(), destination.getPort()));

                // Update player location in database
                String updateSql = """
                    UPDATE player_locations
                    SET server_id = ?, joined_at = CURRENT_TIMESTAMP
                    WHERE player_uuid = ?
                """;
                dbManager.executeUpdate(updateSql, destination.getServerId(), playerUuid.toString());

                // Execute transfer using Hytale API
                playerRef.referToServer(destination.getHost(), destination.getPort(), new byte[0]);

                // Log transfer history (async)
                logTransferHistory(playerUuid, playerName, config.getConfig().server.serverId,
                    destination.getServerId(), type, null, reason, true);

                return true;

            } catch (Exception e) {
                logger.severe("Failed to transfer player: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }

    private void logTransferHistory(UUID playerUuid, String playerName, String fromServerId,
                                    String toServerId, TransferType type, UUID initiatedBy,
                                    String reason, boolean success) {
        String sql = """
            INSERT INTO transfer_history (player_uuid, player_name, from_server_id, to_server_id,
                                         transfer_type, initiated_by, reason, success, transferred_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;

        dbManager.executeUpdateAsync(sql,
            playerUuid.toString(),
            playerName,
            fromServerId,
            toServerId,
            type.name(),
            initiatedBy != null ? initiatedBy.toString() : null,
            reason,
            success
        );
    }

    public enum TransferType {
        TELEPORTER,
        COMMAND,
        FALLBACK,
        KICK,
        QUEUE,
        MODERATION
    }
}
