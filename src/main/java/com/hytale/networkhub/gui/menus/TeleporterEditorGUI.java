package com.hytale.networkhub.gui.menus;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.database.models.TeleporterData;
import com.hytale.networkhub.gui.GUIManager;
import com.hytale.networkhub.managers.TeleporterManager;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.List;
import java.util.logging.Logger;

/**
 * GUI for creating and editing teleporter blocks
 */
public class TeleporterEditorGUI {
    private static final String GUI_ID = "teleporter_editor";

    private final Logger logger;
    private final NetworkConfig config;
    private final GUIManager guiManager;
    private final TeleporterManager teleporterManager;

    public TeleporterEditorGUI(Logger logger, NetworkConfig config, GUIManager guiManager,
                              TeleporterManager teleporterManager) {
        this.logger = logger;
        this.config = config;
        this.guiManager = guiManager;
        this.teleporterManager = teleporterManager;

        guiManager.registerGUI(GUI_ID, this);
    }

    /**
     * Open the teleporter editor GUI
     */
    public void open(Player player) {
        if (!guiManager.isEnabled()) {
            player.sendMessage("§cGUI system is disabled");
            return;
        }

        // Get teleporters on this server
        List<TeleporterData> teleporters = teleporterManager.getTeleportersByServer(
            config.getConfig().server.serverId
        );

        // TODO: Create actual GUI with Hytale API
        displayTeleporterEditor(player, teleporters);

        guiManager.trackGUIOpen(player, GUI_ID);
    }

    /**
     * Close the GUI
     */
    public void close(Player player) {
        guiManager.trackGUIClose(player);
    }

    /**
     * Display teleporter editor as chat (fallback)
     */
    private void displayTeleporterEditor(Player player, List<TeleporterData> teleporters) {
        player.sendMessage("§8§m-------------------------");
        player.sendMessage("§6§lTeleporter Editor");
        player.sendMessage("§8§m-------------------------");

        if (teleporters.isEmpty()) {
            player.sendMessage("§7No teleporters on this server");
        } else {
            player.sendMessage("§aTeleporters: §f" + teleporters.size());
            for (TeleporterData tp : teleporters) {
                player.sendMessage(String.format("§7- §e%s §7→ §a%s §7(%d, %d, %d)",
                    tp.getDisplayName(),
                    tp.getDestinationServerId(),
                    tp.getX(), tp.getY(), tp.getZ()
                ));
            }
        }

        player.sendMessage("§8§m-------------------------");
        player.sendMessage("§7Commands:");
        player.sendMessage("§e/teleporter create <server> [name]");
        player.sendMessage("§e/teleporter remove §7(at location)");
        player.sendMessage("§e/teleporter list");
    }
}
