package com.hytale.networkhub.commands;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.gui.menus.AnnouncementCreatorGUI;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.logging.Logger;

/**
 * Command to create network-wide announcements
 * Opens the announcement creator GUI or creates quick announcements
 */
public class AnnounceCommand {
    private final Logger logger;
    private final NetworkConfig config;
    private final AnnouncementCreatorGUI announcementCreatorGUI;

    public AnnounceCommand(Logger logger, NetworkConfig config,
                          AnnouncementCreatorGUI announcementCreatorGUI) {
        this.logger = logger;
        this.config = config;
        this.announcementCreatorGUI = announcementCreatorGUI;
    }

    /**
     * Execute the announce command
     */
    public boolean execute(Player player, String[] args) {
        if (!config.getConfig().announcements.enabled) {
            player.sendMessage("§cAnnouncement system is disabled");
            return true;
        }

        // TODO: Check permission
        // if (!player.hasPermission("networkhub.announce")) {
        //     player.sendMessage("§cYou don't have permission to create announcements");
        //     return true;
        // }

        if (args.length == 0) {
            // Open GUI
            announcementCreatorGUI.open(player);
        } else {
            // Create quick announcement from command
            String message = String.join(" ", args);
            announcementCreatorGUI.createQuickAnnouncement(player, message);
        }

        return true;
    }
}
