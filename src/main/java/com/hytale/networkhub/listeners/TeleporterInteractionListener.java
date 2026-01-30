package com.hytale.networkhub.listeners;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.database.models.ServerRecord;
import com.hytale.networkhub.database.models.TeleporterData;
import com.hytale.networkhub.managers.*;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class TeleporterInteractionListener {
    private final Logger logger;
    private final TeleporterManager teleporterManager;
    private final ServerRegistryManager registryManager;
    private final TransferManager transferManager;
    private final QueueManager queueManager;
    private final NetworkConfig config;

    private final Map<UUID, PendingTeleport> pendingTeleports = new ConcurrentHashMap<>();
    private final Map<UUID, String> lastBlockPosition = new ConcurrentHashMap<>();

    public TeleporterInteractionListener(Logger logger, TeleporterManager teleporterManager,
                                        ServerRegistryManager registryManager, TransferManager transferManager,
                                        QueueManager queueManager, NetworkConfig config) {
        this.logger = logger;
        this.teleporterManager = teleporterManager;
        this.registryManager = registryManager;
        this.transferManager = transferManager;
        this.queueManager = queueManager;
        this.config = config;
    }

    public void onPlayerMove(Player player, String worldName, int x, int y, int z) {
        UUID playerUuid = player.getUniqueId();
        String currentPos = makePos(worldName, x, y, z);
        String lastPos = lastBlockPosition.get(playerUuid);

        // Check if player moved to new block
        if (currentPos.equals(lastPos)) {
            // Still on same block - check countdown
            PendingTeleport pending = pendingTeleports.get(playerUuid);
            if (pending != null) {
                pending.timeRemaining -= 0.05f; // Assume called every tick

                if (pending.timeRemaining <= 0) {
                    // Execute teleport
                    executeTeleport(player, pending);
                    pendingTeleports.remove(playerUuid);
                } else {
                    // Show countdown
                    int seconds = (int) Math.ceil(pending.timeRemaining);
                    if (seconds != pending.lastShownSeconds) {
                        showCountdown(player, pending.teleporter.getDisplayName(), seconds);
                        pending.lastShownSeconds = seconds;
                    }
                }
            }
            return;
        }

        // Player moved to different block
        lastBlockPosition.put(playerUuid, currentPos);

        // Cancel any pending teleport
        if (pendingTeleports.remove(playerUuid) != null) {
            showCanceled(player);
        }

        // Check if new block is a teleporter
        TeleporterData teleporter = teleporterManager.getTeleporter(worldName, x, y, z);
        if (teleporter != null) {
            startTeleport(player, teleporter);
        }
    }

    private void startTeleport(Player player, TeleporterData teleporter) {
        UUID playerUuid = player.getUniqueId();

        // Check permission
        if (teleporter.getPermission() != null && !teleporter.getPermission().isEmpty()) {
            // TODO: Check if player has permission
            // if (!player.hasPermission(teleporter.getPermission())) {
            //     player.sendMessage("You don't have permission to use this teleporter");
            //     return;
            // }
        }

        // Check cooldown
        if (teleporterManager.hasCooldown(playerUuid, teleporter.getTeleporterId())) {
            long remaining = teleporterManager.getRemainingCooldown(playerUuid, teleporter.getTeleporterId());
            player.sendMessage("§cTeleporter on cooldown: " + remaining + " seconds remaining");
            return;
        }

        // Get destination server
        ServerRecord destination = registryManager.getServerById(teleporter.getDestinationServerId());
        if (destination == null) {
            player.sendMessage("§cDestination server not found");
            logger.warning("Teleporter points to unknown server: " + teleporter.getDestinationServerId());
            return;
        }

        if (destination.getStatus() != ServerRecord.ServerStatus.ONLINE) {
            player.sendMessage("§cDestination server is offline");
            return;
        }

        // Check if server is full
        if (destination.getCurrentPlayers() >= destination.getMaxPlayers()) {
            if (config.getConfig().queue.autoJoinOnFull) {
                int priority = 0; // TODO: Check if player has VIP
                queueManager.joinQueue(playerUuid, destination.getServerId(), priority);
                player.sendMessage("§eServer is full. You have been added to the queue.");
                return;
            } else {
                player.sendMessage("§cDestination server is full");
                return;
            }
        }

        // Start countdown
        int delay = config.getConfig().teleporter.confirmationTimeoutSeconds;
        PendingTeleport pending = new PendingTeleport(teleporter, destination, delay);
        pendingTeleports.put(playerUuid, pending);

        showInitial(player, destination.getServerName(), delay);
        logger.info("Player " + player.getUsername() + " stepped on teleporter to " + destination.getServerName());
    }

    private void executeTeleport(Player player, PendingTeleport pending) {
        try {
            showSuccess(player, pending.destination.getServerName());

            // Apply cooldown
            if (pending.teleporter.getCooldownSeconds() > 0) {
                teleporterManager.applyCooldown(player.getUniqueId(),
                    pending.teleporter.getTeleporterId(),
                    pending.teleporter.getCooldownSeconds());
            }

            // Transfer player
            transferManager.transferPlayer(player.getPlayerRef(), pending.destination,
                TransferManager.TransferType.TELEPORTER,
                "Teleporter at " + pending.teleporter.getX() + "," +
                pending.teleporter.getY() + "," + pending.teleporter.getZ());

        } catch (Exception e) {
            logger.severe("Failed to execute teleport: " + e.getMessage());
            e.printStackTrace();
            showError(player);
        }
    }

    private void showInitial(Player player, String destination, int seconds) {
        player.sendMessage("§a§lTeleporting to " + destination);
        player.sendMessage("§7Stand still for " + seconds + " seconds...");
    }

    private void showCountdown(Player player, String destination, int seconds) {
        player.sendMessage("§e§lTeleporting to " + destination + " in " + seconds + " second" +
            (seconds == 1 ? "" : "s") + "...");
    }

    private void showSuccess(Player player, String destination) {
        player.sendMessage("§a§lTeleporting to " + destination + "!");
    }

    private void showCanceled(Player player) {
        player.sendMessage("§c§lTeleport canceled - you moved!");
    }

    private void showError(Player player) {
        player.sendMessage("§c§lTeleport failed - please try again");
    }

    private String makePos(String world, int x, int y, int z) {
        return world + ":" + x + ":" + y + ":" + z;
    }

    private static class PendingTeleport {
        final TeleporterData teleporter;
        final ServerRecord destination;
        float timeRemaining;
        int lastShownSeconds;

        PendingTeleport(TeleporterData teleporter, ServerRecord destination, float delay) {
            this.teleporter = teleporter;
            this.destination = destination;
            this.timeRemaining = delay;
            this.lastShownSeconds = (int) delay;
        }
    }
}
