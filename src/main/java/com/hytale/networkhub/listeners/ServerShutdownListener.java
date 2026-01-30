package com.hytale.networkhub.listeners;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.database.models.ServerRecord;
import com.hytale.networkhub.managers.HubManager;
import com.hytale.networkhub.managers.ServerRegistryManager;
import com.hytale.networkhub.managers.TransferManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Handles graceful server shutdown
 * Evacuates all players to hub servers before shutting down
 */
public class ServerShutdownListener {
    private final Logger logger;
    private final NetworkConfig config;
    private final HubManager hubManager;
    private final TransferManager transferManager;
    private final ServerRegistryManager registryManager;

    public ServerShutdownListener(Logger logger, NetworkConfig config, HubManager hubManager,
                                 TransferManager transferManager, ServerRegistryManager registryManager) {
        this.logger = logger;
        this.config = config;
        this.hubManager = hubManager;
        this.transferManager = transferManager;
        this.registryManager = registryManager;
    }

    /**
     * Called when the server is shutting down
     * Evacuates all players to hub
     */
    public void onShutdown(Collection<Player> onlinePlayers) {
        if (!config.getConfig().fallback.enabled || !config.getConfig().fallback.triggerOnShutdown) {
            logger.info("Fallback on shutdown is disabled, skipping player evacuation");
            return;
        }

        if (onlinePlayers.isEmpty()) {
            logger.info("No players online, skipping evacuation");
            return;
        }

        logger.info("Server shutting down, evacuating " + onlinePlayers.size() + " players to hub...");

        try {
            // Select fallback hub
            ServerRecord hub = hubManager.selectFallbackHub();
            if (hub == null) {
                logger.severe("No hub available for fallback! Players will be disconnected!");
                return;
            }

            logger.info("Transferring players to hub: " + hub.getServerName());

            // Small delay before transfer
            int delaySeconds = config.getConfig().fallback.transferDelaySeconds;
            if (delaySeconds > 0) {
                logger.info("Waiting " + delaySeconds + " seconds before transfer...");
                Thread.sleep(delaySeconds * 1000L);
            }

            // Transfer all players
            CompletableFuture<?>[] futures = new CompletableFuture[onlinePlayers.size()];
            int i = 0;

            for (Player player : onlinePlayers) {
                player.sendMessage(Message.raw("§cServer restarting, transferring to hub..."));

                futures[i++] = transferManager.transferPlayer(
                    player,
                    hub,
                    TransferManager.TransferType.FALLBACK,
                    "Server shutdown"
                );
            }

            // Wait for all transfers to complete (max 10 seconds)
            CompletableFuture.allOf(futures).get(10, TimeUnit.SECONDS);

            logger.info("Successfully evacuated all players to hub");

        } catch (Exception e) {
            logger.severe("Failed to evacuate players during shutdown: " + e.getMessage());
            e.printStackTrace();
        }

        // Mark server as offline in database
        try {
            registryManager.unregisterServer();
            logger.info("Server unregistered from network");
        } catch (Exception e) {
            logger.warning("Failed to unregister server: " + e.getMessage());
        }
    }

    /**
     * Evacuate all players on the current server to a hub
     * Can be called manually by admins
     */
    public void evacuateServer(Collection<Player> onlinePlayers) {
        if (onlinePlayers.isEmpty()) {
            logger.info("No players to evacuate");
            return;
        }

        logger.info("Evacuating " + onlinePlayers.size() + " players...");

        try {
            ServerRecord hub = hubManager.selectFallbackHub();
            if (hub == null) {
                logger.severe("No hub available for evacuation!");
                return;
            }

            for (Player player : onlinePlayers) {
                player.sendMessage(Message.raw("§eYou are being transferred to the hub..."));

                transferManager.transferPlayer(
                    player,
                    hub,
                    TransferManager.TransferType.FALLBACK,
                    "Manual evacuation"
                );
            }

            logger.info("Evacuation initiated");

        } catch (Exception e) {
            logger.severe("Failed to evacuate players: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
