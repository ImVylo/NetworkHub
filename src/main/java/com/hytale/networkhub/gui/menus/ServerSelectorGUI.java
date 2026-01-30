package com.hytale.networkhub.gui.menus;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.database.models.ServerRecord;
import com.hytale.networkhub.gui.GUIManager;
import com.hytale.networkhub.managers.QueueManager;
import com.hytale.networkhub.managers.ServerRegistryManager;
import com.hytale.networkhub.managers.TransferManager;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.List;
import java.util.logging.Logger;

/**
 * Interactive GUI for players to select and join servers
 * Shows all available servers with status, player counts, and click-to-join
 */
public class ServerSelectorGUI {
    private static final String GUI_ID = "server_selector";

    private final Logger logger;
    private final NetworkConfig config;
    private final GUIManager guiManager;
    private final ServerRegistryManager registryManager;
    private final TransferManager transferManager;
    private final QueueManager queueManager;

    public ServerSelectorGUI(Logger logger, NetworkConfig config, GUIManager guiManager,
                            ServerRegistryManager registryManager, TransferManager transferManager,
                            QueueManager queueManager) {
        this.logger = logger;
        this.config = config;
        this.guiManager = guiManager;
        this.registryManager = registryManager;
        this.transferManager = transferManager;
        this.queueManager = queueManager;

        // Register with GUI manager
        guiManager.registerGUI(GUI_ID, this);
    }

    /**
     * Open the server selector GUI for a player
     */
    public void open(Player player) {
        if (!guiManager.isEnabled()) {
            player.sendMessage("§cGUI system is disabled");
            return;
        }

        if (!guiManager.canPlayersSelectServers()) {
            player.sendMessage("§cServer selection is disabled");
            return;
        }

        // Get all servers
        List<ServerRecord> servers = registryManager.getAllServers();

        if (servers.isEmpty()) {
            player.sendMessage("§cNo servers available");
            return;
        }

        // TODO: Create actual GUI with Hytale API
        // For now, send formatted list as chat fallback
        displayServerList(player, servers);

        // Track GUI open
        guiManager.trackGUIOpen(player, GUI_ID);
    }

    /**
     * Close the GUI for a player
     */
    public void close(Player player) {
        // TODO: Close actual GUI with Hytale API
        guiManager.trackGUIClose(player);
    }

    /**
     * Handle server selection click
     */
    private void handleServerClick(Player player, ServerRecord server) {
        // Check if server is online
        if (server.getStatus() != ServerRecord.ServerStatus.ONLINE) {
            player.sendMessage("§cServer " + server.getServerName() + " is offline!");
            return;
        }

        // Check if player is already on this server
        if (server.getServerId().equals(config.getConfig().server.serverId)) {
            player.sendMessage("§eYou are already on this server!");
            return;
        }

        // Check if server is full
        if (server.getCurrentPlayers() >= server.getMaxPlayers()) {
            handleFullServer(player, server);
            return;
        }

        // Transfer player
        close(player);
        player.sendMessage("§aTransferring to " + server.getServerName() + "...");

        transferManager.transferPlayer(player, server, TransferManager.TransferType.COMMAND, "Server selector")
            .thenAccept(success -> {
                if (!success) {
                    player.sendMessage("§cFailed to transfer to " + server.getServerName());
                }
            });
    }

    /**
     * Handle when player tries to join a full server
     */
    private void handleFullServer(Player player, ServerRecord server) {
        if (!config.getConfig().queue.enabled) {
            player.sendMessage("§cServer " + server.getServerName() + " is full!");
            return;
        }

        if (config.getConfig().queue.autoJoinOnFull) {
            // Auto-join queue
            int priority = 0; // TODO: Check for VIP permission
            // if (player.hasPermission("networkhub.queue.vip")) {
            //     priority = config.getConfig().queue.vipPriority;
            // }

            queueManager.joinQueue(player.getUniqueId(), server.getServerId(), priority);
            player.sendMessage(String.format("§eServer is full! You've been added to the queue for §a%s",
                server.getServerName()));
            close(player);
        } else {
            // Prompt player to manually join queue
            player.sendMessage(String.format("§cServer %s is full! Use §e/queue join %s §cto join the queue",
                server.getServerName(), server.getServerId()));
        }
    }

    /**
     * Display server list as chat (fallback until GUI API is available)
     */
    private void displayServerList(Player player, List<ServerRecord> servers) {
        player.sendMessage("§8§m--------------------");
        player.sendMessage("§6§lNetwork Servers");
        player.sendMessage("§8§m--------------------");

        for (ServerRecord server : servers) {
            String statusColor = getStatusColor(server);
            String statusIcon = getStatusIcon(server);

            String playerInfo = server.getStatus() == ServerRecord.ServerStatus.ONLINE
                ? String.format("[%d/%d]", server.getCurrentPlayers(), server.getMaxPlayers())
                : "[OFFLINE]";

            String serverType = server.isHub() ? "HUB" : server.getServerType();

            player.sendMessage(String.format("%s%s [%s] %s %s %s",
                statusColor,
                statusIcon,
                serverType,
                server.getServerName(),
                playerInfo,
                getServerNote(server)));
        }

        player.sendMessage("§8§m--------------------");
        player.sendMessage("§7Click a server to join (GUI coming soon)");
        player.sendMessage("§7Or use: §e/transfer <server>");
    }

    /**
     * Get color based on server status
     */
    private String getStatusColor(ServerRecord server) {
        switch (server.getStatus()) {
            case ONLINE:
                if (server.getCurrentPlayers() >= server.getMaxPlayers()) {
                    return "§c"; // Red for full
                }
                return "§a"; // Green for online
            case DEGRADED:
                return "§e"; // Yellow for degraded
            case OFFLINE:
            default:
                return "§7"; // Gray for offline
        }
    }

    /**
     * Get icon based on server status
     */
    private String getStatusIcon(ServerRecord server) {
        switch (server.getStatus()) {
            case ONLINE:
                return server.getCurrentPlayers() >= server.getMaxPlayers() ? "⏸" : "✓";
            case DEGRADED:
                return "⚠";
            case OFFLINE:
            default:
                return "✗";
        }
    }

    /**
     * Get additional note about server
     */
    private String getServerNote(ServerRecord server) {
        if (server.getServerId().equals(config.getConfig().server.serverId)) {
            return "§a§l← YOU ARE HERE";
        }
        if (server.getCurrentPlayers() >= server.getMaxPlayers()) {
            return "§c(FULL)";
        }
        return "";
    }

    /**
     * Refresh the GUI with updated server data
     */
    public void refresh(Player player) {
        // TODO: Update GUI items with fresh data
        // For now, re-open
        open(player);
    }
}
