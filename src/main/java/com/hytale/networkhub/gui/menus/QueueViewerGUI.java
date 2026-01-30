package com.hytale.networkhub.gui.menus;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.gui.GUIManager;
import com.hytale.networkhub.managers.QueueManager;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.logging.Logger;

/**
 * GUI for viewing and managing server queues
 */
public class QueueViewerGUI {
    private static final String GUI_ID = "queue_viewer";

    private final Logger logger;
    private final NetworkConfig config;
    private final GUIManager guiManager;
    private final QueueManager queueManager;

    public QueueViewerGUI(Logger logger, NetworkConfig config, GUIManager guiManager,
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
            player.sendMessage("§cGUI system is disabled");
            return;
        }

        if (!config.getConfig().queue.enabled) {
            player.sendMessage("§cQueue system is disabled");
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
        player.sendMessage("§8§m-------------------------");
        player.sendMessage("§6§lQueue Viewer");
        player.sendMessage("§8§m-------------------------");

        int totalQueued = queueManager.getTotalQueuedPlayers();
        player.sendMessage("§eTotal Queued Players: §f" + totalQueued);

        player.sendMessage("§8§m-------------------------");
        player.sendMessage("§7Commands:");
        player.sendMessage("§e/queue join <server>");
        player.sendMessage("§e/queue leave <server>");
        player.sendMessage("§e/queue info <server>");
        player.sendMessage("§e/queue list");
    }
}
