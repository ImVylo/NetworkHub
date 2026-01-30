package com.hytale.networkhub.gui.menus;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.database.models.ServerRecord;
import com.hytale.networkhub.gui.GUIManager;
import com.hytale.networkhub.managers.HubManager;
import com.hytale.networkhub.managers.ServerRegistryManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;

import java.util.List;
import java.util.logging.Logger;

/**
 * GUI for managing servers in the network
 * Configure hubs, priorities, and server settings
 */
public class ServerManagementGUI {
    private static final String GUI_ID = "server_management";

    private final Logger logger;
    private final NetworkConfig config;
    private final GUIManager guiManager;
    private final ServerRegistryManager registryManager;
    private final HubManager hubManager;

    public ServerManagementGUI(Logger logger, NetworkConfig config, GUIManager guiManager,
                              ServerRegistryManager registryManager, HubManager hubManager) {
        this.logger = logger;
        this.config = config;
        this.guiManager = guiManager;
        this.registryManager = registryManager;
        this.hubManager = hubManager;

        // Register with GUI manager
        guiManager.registerGUI(GUI_ID, this);
    }

    /**
     * Open the server management GUI
     */
    public void open(Player player) {
        if (!guiManager.isEnabled()) {
            player.sendMessage(Message.raw("§cGUI system is disabled"));
            return;
        }

        // Get all servers
        List<ServerRecord> servers = registryManager.getAllServers();

        // TODO: Create actual GUI with Hytale API
        displayServerManagement(player, servers);

        guiManager.trackGUIOpen(player, GUI_ID);
    }

    /**
     * Close the GUI
     */
    public void close(Player player) {
        guiManager.trackGUIClose(player);
    }

    /**
     * Display server management menu as chat (fallback)
     */
    private void displayServerManagement(Player player, List<ServerRecord> servers) {
        player.sendMessage(Message.raw("§8§m-------------------------"));
        player.sendMessage(Message.raw("§6§lServer Management"));
        player.sendMessage(Message.raw("§8§m-------------------------"));

        if (servers.isEmpty()) {
            player.sendMessage(Message.raw("§cNo servers registered"));
            return;
        }

        for (ServerRecord server : servers) {
            String statusColor = server.getStatus() == ServerRecord.ServerStatus.ONLINE ? "§a" : "§c";
            String hubBadge = server.isHub() ? " §e[HUB]" : "";
            String priority = server.isHub() ? " §7(Priority: " + server.getHubPriority() + ")" : "";

            player.sendMessage(Message.raw(String.format("%s%s%s §7- %s%d/%d%s",
                statusColor,
                server.getServerName(),
                hubBadge,
                statusColor,
                server.getCurrentPlayers(),
                server.getMaxPlayers(),
                priority
            ));
        }

        player.sendMessage(Message.raw("§8§m-------------------------"));
        player.sendMessage(Message.raw("§7Commands:"));
        player.sendMessage(Message.raw("§e/network sethub <server> [priority]"));
        player.sendMessage(Message.raw("§e/network unsethub <server>"));
        player.sendMessage(Message.raw("§e/network listservers"));
    }

    /**
     * Handle server click for editing
     */
    public void handleServerClick(Player player, ServerRecord server) {
        // TODO: Open server edit submenu
        player.sendMessage(Message.raw("§7Editing server: §e" + server.getServerName());
        player.sendMessage(Message.raw("§7Commands:"));
        player.sendMessage(Message.raw("§e/network sethub " + server.getServerId() + " [priority]");
        player.sendMessage(Message.raw("§e/network unsethub " + server.getServerId());
    }
}
