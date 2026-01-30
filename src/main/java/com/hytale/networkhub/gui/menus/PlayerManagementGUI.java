package com.hytale.networkhub.gui.menus;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.database.models.PlayerLocation;
import com.hytale.networkhub.database.models.ServerRecord;
import com.hytale.networkhub.gui.GUIManager;
import com.hytale.networkhub.managers.PlayerTrackingManager;
import com.hytale.networkhub.managers.ServerRegistryManager;
import com.hytale.networkhub.managers.TransferManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;

import java.util.List;
import java.util.logging.Logger;

/**
 * GUI for managing players across the network
 * Transfer, track, and locate players
 */
public class PlayerManagementGUI {
    private static final String GUI_ID = "player_management";

    private final Logger logger;
    private final NetworkConfig config;
    private final GUIManager guiManager;
    private final PlayerTrackingManager trackingManager;
    private final ServerRegistryManager registryManager;
    private final TransferManager transferManager;

    public PlayerManagementGUI(Logger logger, NetworkConfig config, GUIManager guiManager,
                              PlayerTrackingManager trackingManager, ServerRegistryManager registryManager,
                              TransferManager transferManager) {
        this.logger = logger;
        this.config = config;
        this.guiManager = guiManager;
        this.trackingManager = trackingManager;
        this.registryManager = registryManager;
        this.transferManager = transferManager;

        guiManager.registerGUI(GUI_ID, this);
    }

    /**
     * Open the player management GUI
     */
    public void open(Player player) {
        if (!guiManager.isEnabled()) {
            player.sendMessage(Message.raw("§cGUI system is disabled"));
            return;
        }

        // TODO: Create actual GUI with Hytale API
        displayPlayerManagement(player);

        guiManager.trackGUIOpen(player, GUI_ID);
    }

    /**
     * Close the GUI
     */
    public void close(Player player) {
        guiManager.trackGUIClose(player);
    }

    /**
     * Display player management as chat (fallback)
     */
    private void displayPlayerManagement(Player player) {
        player.sendMessage(Message.raw("§8§m-------------------------"));
        player.sendMessage(Message.raw("§6§lPlayer Management"));
        player.sendMessage(Message.raw("§8§m-------------------------"));
        player.sendMessage(Message.raw("§7Commands:"));
        player.sendMessage(Message.raw("§e/network transfer <player> <server>"));
        player.sendMessage(Message.raw("§e/network transferall <server>"));
        player.sendMessage(Message.raw("§e/whereis <player>"));
        player.sendMessage(Message.raw("§8§m-------------------------"));
    }

    /**
     * Display player locations
     */
    public void displayPlayerLocations(Player admin) {
        // Get all online players
        List<PlayerLocation> locations = trackingManager.getAllOnlinePlayers();

        admin.sendMessage("§8§m-------------------------");
        admin.sendMessage("§6§lOnline Players: §f" + locations.size());
        admin.sendMessage("§8§m-------------------------");

        if (locations.isEmpty()) {
            admin.sendMessage("§7No players online");
            return;
        }

        // Group by server
        locations.stream()
            .collect(java.util.stream.Collectors.groupingBy(PlayerLocation::getServerId))
            .forEach((serverId, players) -> {
                admin.sendMessage(String.format("§e%s §7(%d players):", serverId, players.size()));
                players.forEach(p -> admin.sendMessage("  §7- §f" + p.getPlayerName()));
            });

        admin.sendMessage("§8§m-------------------------");
    }
}
