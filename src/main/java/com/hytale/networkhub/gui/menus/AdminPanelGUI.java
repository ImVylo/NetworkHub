package com.hytale.networkhub.gui.menus;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.gui.GUIManager;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.logging.Logger;

/**
 * Main admin control panel GUI
 * Central hub for accessing all admin functions
 */
public class AdminPanelGUI {
    private static final String GUI_ID = "admin_panel";

    private final Logger logger;
    private final NetworkConfig config;
    private final GUIManager guiManager;

    // Sub-GUIs
    private final ServerManagementGUI serverManagementGUI;
    private final PlayerManagementGUI playerManagementGUI;
    private final TeleporterEditorGUI teleporterEditorGUI;
    private final QueueViewerGUI queueViewerGUI;
    private final AnnouncementCreatorGUI announcementCreatorGUI;

    public AdminPanelGUI(Logger logger, NetworkConfig config, GUIManager guiManager,
                        ServerManagementGUI serverManagementGUI, PlayerManagementGUI playerManagementGUI,
                        TeleporterEditorGUI teleporterEditorGUI, QueueViewerGUI queueViewerGUI,
                        AnnouncementCreatorGUI announcementCreatorGUI) {
        this.logger = logger;
        this.config = config;
        this.guiManager = guiManager;
        this.serverManagementGUI = serverManagementGUI;
        this.playerManagementGUI = playerManagementGUI;
        this.teleporterEditorGUI = teleporterEditorGUI;
        this.queueViewerGUI = queueViewerGUI;
        this.announcementCreatorGUI = announcementCreatorGUI;

        // Register with GUI manager
        guiManager.registerGUI(GUI_ID, this);
    }

    /**
     * Open the admin panel for a player
     */
    public void open(Player player) {
        if (!guiManager.isEnabled()) {
            player.sendMessage("§cGUI system is disabled");
            return;
        }

        // TODO: Check admin permission
        // if (!player.hasPermission("networkhub.admin")) {
        //     player.sendMessage("§cYou don't have permission to access the admin panel");
        //     return;
        // }

        // TODO: Create actual GUI with Hytale API
        // For now, display menu as chat
        displayAdminMenu(player);

        // Track GUI open
        guiManager.trackGUIOpen(player, GUI_ID);
    }

    /**
     * Close the admin panel
     */
    public void close(Player player) {
        // TODO: Close actual GUI with Hytale API
        guiManager.trackGUIClose(player);
    }

    /**
     * Display admin menu as chat (fallback)
     */
    private void displayAdminMenu(Player player) {
        player.sendMessage("§8§m-------------------------");
        player.sendMessage("§6§lNetworkHub Admin Panel");
        player.sendMessage("§8§m-------------------------");
        player.sendMessage("");
        player.sendMessage("§e[1] §aServer Management");
        player.sendMessage("   §7Configure servers, hubs, priorities");
        player.sendMessage("");
        player.sendMessage("§e[2] §aPlayer Management");
        player.sendMessage("   §7Transfer players, track locations");
        player.sendMessage("");
        player.sendMessage("§e[3] §aTeleporter Editor");
        player.sendMessage("   §7Create and edit teleporters");
        player.sendMessage("");
        player.sendMessage("§e[4] §aQueue Viewer");
        player.sendMessage("   §7View and manage server queues");
        player.sendMessage("");
        player.sendMessage("§e[5] §aAnnouncements");
        player.sendMessage("   §7Create network-wide announcements");
        player.sendMessage("");
        player.sendMessage("§e[6] §aNetwork Status");
        player.sendMessage("   §7View health and statistics");
        player.sendMessage("");
        player.sendMessage("§8§m-------------------------");
        player.sendMessage("§7Use commands until GUI is fully implemented:");
        player.sendMessage("§e/network listservers §7- View all servers");
        player.sendMessage("§e/network sethub <server> §7- Set hub");
        player.sendMessage("§e/network transfer <player> <server> §7- Transfer");
    }

    /**
     * Handle menu option selection
     */
    public void handleMenuClick(Player player, int option) {
        switch (option) {
            case 1:
                close(player);
                serverManagementGUI.open(player);
                break;

            case 2:
                close(player);
                playerManagementGUI.open(player);
                break;

            case 3:
                close(player);
                teleporterEditorGUI.open(player);
                break;

            case 4:
                close(player);
                queueViewerGUI.open(player);
                break;

            case 5:
                close(player);
                announcementCreatorGUI.open(player);
                break;

            case 6:
                // Display network status
                displayNetworkStatus(player);
                break;

            default:
                player.sendMessage("§cInvalid option");
        }
    }

    /**
     * Display network status information
     */
    private void displayNetworkStatus(Player player) {
        // TODO: Gather and display comprehensive network stats
        player.sendMessage("§8§m-------------------------");
        player.sendMessage("§6§lNetwork Status");
        player.sendMessage("§8§m-------------------------");
        player.sendMessage("§7Status information coming soon...");
        player.sendMessage("§7Use §e/network listservers §7for now");
        player.sendMessage("§8§m-------------------------");
    }
}
