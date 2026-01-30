package com.hytale.networkhub.gui.menus;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.database.models.Announcement;
import com.hytale.networkhub.gui.GUIManager;
import com.hytale.networkhub.managers.AnnouncementManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;

import java.util.ArrayList;
import com.hypixel.hytale.logger.HytaleLogger;
import java.util.logging.Level;

/**
 * GUI for creating network-wide announcements
 * Supports title, subtitle, action bar, and custom popup GUIs
 */
public class AnnouncementCreatorGUI {
    private static final String GUI_ID = "announcement_creator";

    private final HytaleLogger logger;
    private final NetworkConfig config;
    private final GUIManager guiManager;
    private final AnnouncementManager announcementManager;

    public AnnouncementCreatorGUI(HytaleLogger logger, NetworkConfig config, GUIManager guiManager,
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
            player.sendMessage(Message.raw("§cGUI system is disabled"));
            return;
        }

        if (!config.getConfig().announcements.enabled) {
            player.sendMessage(Message.raw("§cAnnouncement system is disabled"));
            return;
        }

        // TODO: Check permission
        // if (!player.hasPermission("networkhub.announce")) {
        //     player.sendMessage(Message.raw("§cYou don't have permission to create announcements"));
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
        player.sendMessage(Message.raw("§8§m-------------------------"));
        player.sendMessage(Message.raw("§6§lAnnouncement Creator"));
        player.sendMessage(Message.raw("§8§m-------------------------"));
        player.sendMessage(Message.raw("§7Create network-wide announcements"));
        player.sendMessage(Message.raw(""));
        player.sendMessage(Message.raw("§eDisplay Types:"));
        player.sendMessage(Message.raw("§7- TITLE: Large centered text"));
        player.sendMessage(Message.raw("§7- SUBTITLE: Smaller subtitle text"));
        player.sendMessage(Message.raw("§7- ACTIONBAR: Text above hotbar"));
        player.sendMessage(Message.raw("§7- POPUP: Custom popup GUI"));
        player.sendMessage(Message.raw("§7- ALL: Combination of all"));
        player.sendMessage(Message.raw(""));
        player.sendMessage(Message.raw("§8§m-------------------------"));
        player.sendMessage(Message.raw("§7Command (quick announcement):"));
        player.sendMessage(Message.raw("§e/announce <message>"));
        player.sendMessage(Message.raw(""));
        player.sendMessage(Message.raw("§7For advanced options, use the GUI"));
        player.sendMessage(Message.raw("§7when it's fully implemented"));
    }

    /**
     * Create a quick announcement from command
     */
    public void createQuickAnnouncement(Player player, String message) {
        Announcement announcement = new Announcement();
        announcement.setCreatorUuid(player.getPlayerRef().getUuid());
        announcement.setCreatorName(player.getPlayerRef().getUsername());
        announcement.setTitle(message);
        announcement.setDisplayType(Announcement.DisplayType.TITLE);
        announcement.setTargetServers(new ArrayList<>()); // All servers
        announcement.setDurationSeconds(config.getConfig().announcements.defaultDurationSeconds);
        announcement.setPriority(5);

        try {
            announcementManager.createAnnouncement(announcement);
            player.sendMessage(Message.raw("§aAnnouncement sent to all servers!"));
        } catch (Exception e) {
            player.sendMessage(Message.raw("§cFailed to create announcement: " + e.getMessage()));
            logger.at(Level.WARNING).log("Failed to create announcement: " + e.getMessage());
        }
    }

    /**
     * Preview an announcement (show to creator only)
     */
    public void previewAnnouncement(Player player, Announcement announcement) {
        player.sendMessage(Message.raw("§8§m-------------------------"));
        player.sendMessage(Message.raw("§6§lAnnouncement Preview"));
        player.sendMessage(Message.raw("§8§m-------------------------"));
        player.sendMessage(Message.raw("§eTitle: §f" + announcement.getTitle()));
        player.sendMessage(Message.raw("§eSubtitle: §f" + announcement.getSubtitle()));
        player.sendMessage(Message.raw("§eAction Bar: §f" + announcement.getActionBar()));
        player.sendMessage(Message.raw("§eDisplay Type: §f" + announcement.getDisplayType()));
        player.sendMessage(Message.raw("§eDuration: §f" + announcement.getDurationSeconds() + "s"));
        player.sendMessage(Message.raw("§ePriority: §f" + announcement.getPriority()));
        player.sendMessage(Message.raw("§8§m-------------------------"));
    }
}
