package com.hytale.networkhub.gui.menus;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.database.models.Announcement;
import com.hytale.networkhub.gui.GUIManager;
import com.hytale.networkhub.managers.AnnouncementManager;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * GUI for creating network-wide announcements
 * Supports title, subtitle, action bar, and custom popup GUIs
 */
public class AnnouncementCreatorGUI {
    private static final String GUI_ID = "announcement_creator";

    private final Logger logger;
    private final NetworkConfig config;
    private final GUIManager guiManager;
    private final AnnouncementManager announcementManager;

    public AnnouncementCreatorGUI(Logger logger, NetworkConfig config, GUIManager guiManager,
                                 AnnouncementManager announcementManager) {
        this.logger = logger;
        this.config = config;
        this.guiManager = guiManager;
        this.announcementManager = announcementManager;

        guiManager.registerGUI(GUI_ID, this);
    }

    /**
     * Open the announcement creator GUI
     */
    public void open(Player player) {
        if (!guiManager.isEnabled()) {
            player.sendMessage("§cGUI system is disabled");
            return;
        }

        if (!config.getConfig().announcements.enabled) {
            player.sendMessage("§cAnnouncement system is disabled");
            return;
        }

        // TODO: Check permission
        // if (!player.hasPermission("networkhub.announce")) {
        //     player.sendMessage("§cYou don't have permission to create announcements");
        //     return;
        // }

        // TODO: Create actual GUI with Hytale API
        displayAnnouncementCreator(player);

        guiManager.trackGUIOpen(player, GUI_ID);
    }

    /**
     * Close the GUI
     */
    public void close(Player player) {
        guiManager.trackGUIClose(player);
    }

    /**
     * Display announcement creator as chat (fallback)
     */
    private void displayAnnouncementCreator(Player player) {
        player.sendMessage("§8§m-------------------------");
        player.sendMessage("§6§lAnnouncement Creator");
        player.sendMessage("§8§m-------------------------");
        player.sendMessage("§7Create network-wide announcements");
        player.sendMessage("");
        player.sendMessage("§eDisplay Types:");
        player.sendMessage("§7- TITLE: Large centered text");
        player.sendMessage("§7- SUBTITLE: Smaller subtitle text");
        player.sendMessage("§7- ACTIONBAR: Text above hotbar");
        player.sendMessage("§7- POPUP: Custom popup GUI");
        player.sendMessage("§7- ALL: Combination of all");
        player.sendMessage("");
        player.sendMessage("§8§m-------------------------");
        player.sendMessage("§7Command (quick announcement):");
        player.sendMessage("§e/announce <message>");
        player.sendMessage("");
        player.sendMessage("§7For advanced options, use the GUI");
        player.sendMessage("§7when it's fully implemented");
    }

    /**
     * Create a quick announcement from command
     */
    public void createQuickAnnouncement(Player player, String message) {
        Announcement announcement = new Announcement();
        announcement.setCreatorUuid(player.getUniqueId());
        announcement.setCreatorName(player.getUsername());
        announcement.setTitle(message);
        announcement.setDisplayType(Announcement.DisplayType.TITLE);
        announcement.setTargetServers(new ArrayList<>()); // All servers
        announcement.setDurationSeconds(config.getConfig().announcements.defaultDurationSeconds);
        announcement.setPriority(5);

        try {
            announcementManager.createAnnouncement(announcement);
            player.sendMessage("§aAnnouncement sent to all servers!");
        } catch (Exception e) {
            player.sendMessage("§cFailed to create announcement: " + e.getMessage());
            logger.warning("Failed to create announcement: " + e.getMessage());
        }
    }

    /**
     * Preview an announcement (show to creator only)
     */
    public void previewAnnouncement(Player player, Announcement announcement) {
        player.sendMessage("§8§m-------------------------");
        player.sendMessage("§6§lAnnouncement Preview");
        player.sendMessage("§8§m-------------------------");
        player.sendMessage("§eTitle: §f" + announcement.getTitle());
        player.sendMessage("§eSubtitle: §f" + announcement.getSubtitle());
        player.sendMessage("§eAction Bar: §f" + announcement.getActionBar());
        player.sendMessage("§eDisplay Type: §f" + announcement.getDisplayType());
        player.sendMessage("§eDuration: §f" + announcement.getDurationSeconds() + "s");
        player.sendMessage("§ePriority: §f" + announcement.getPriority());
        player.sendMessage("§8§m-------------------------");
    }
}
