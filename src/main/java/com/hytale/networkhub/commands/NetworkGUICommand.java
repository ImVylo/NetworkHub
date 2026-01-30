package com.hytale.networkhub.commands;

import com.hytale.networkhub.gui.menus.AdminPanelGUI;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.logging.Logger;

/**
 * Command to open the network admin panel GUI
 */
public class NetworkGUICommand {
    private final Logger logger;
    private final AdminPanelGUI adminPanelGUI;

    public NetworkGUICommand(Logger logger, AdminPanelGUI adminPanelGUI) {
        this.logger = logger;
        this.adminPanelGUI = adminPanelGUI;
    }

    /**
     * Execute the network GUI command
     */
    public boolean execute(Player player, String[] args) {
        adminPanelGUI.open(player);
        return true;
    }
}
