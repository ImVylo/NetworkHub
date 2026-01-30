package com.hytale.networkhub.gui.menus;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.gui.GUIManager;
import com.hytale.networkhub.managers.QueueManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;

import com.hypixel.hytale.logger.HytaleLogger;

/**
 * GUI for viewing and managing server queues
 */
public class QueueViewerGUI {
    private static final String GUI_ID = "queue_viewer";

    private final HytaleLogger logger;
    private final NetworkConfig config;
    private final GUIManager guiManager;
    private final QueueManager queueManager;

    public QueueViewerGUI(HytaleLogger logger, NetworkConfig config, GUIManager guiManager,
                         QueueManager queueManager) {
        this.logger = logger;
        this.config = config;
        this.guiManager = guiManager;
        this.queueManager = queueManager;

        guiManager.registerGUI(GUI_ID, this);
    }

    /**
     * Open the queue viewer GUI
     */
    public void open(Player player) {
        if (!guiManager.isEnabled()) {
            player.sendMessage(Message.raw("§cGUI system is disabled"));
            return;
        }

        if (!config.getConfig().queue.enabled) {
            player.sendMessage(Message.raw("§cQueue system is disabled"));
            return;
        }

        // TODO: Create actual GUI with Hytale API
        displayQueueViewer(player);

        guiManager.trackGUIOpen(player, GUI_ID);
    }

    /**
     * Close the GUI
     */
    public void close(Player player) {
        guiManager.trackGUIClose(player);
    }

    /**
     * Display queue viewer as chat (fallback)
     */
    private void displayQueueViewer(Player player) {
        player.sendMessage(Message.raw("§8§m-------------------------"));
        player.sendMessage(Message.raw("§6§lQueue Viewer"));
        player.sendMessage(Message.raw("§8§m-------------------------"));

        int totalQueued = queueManager.getTotalQueuedPlayers();
        player.sendMessage(Message.raw("§eTotal Queued Players: §f" + totalQueued));

        player.sendMessage(Message.raw("§8§m-------------------------"));
        player.sendMessage(Message.raw("§7Commands:"));
        player.sendMessage(Message.raw("§e/queue join <server>"));
        player.sendMessage(Message.raw("§e/queue leave <server>"));
        player.sendMessage(Message.raw("§e/queue info <server>"));
        player.sendMessage(Message.raw("§e/queue list"));
    }
}
