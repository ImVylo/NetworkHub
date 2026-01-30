package com.hytale.networkhub.commands;

import com.hytale.networkhub.gui.menus.ServerSelectorGUI;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;

import com.hypixel.hytale.logger.HytaleLogger;

/**
 * Command to open the server selector GUI
 * Allows players to browse and join available servers
 */
public class ServersCommand {
    private final HytaleLogger logger;
    private final ServerSelectorGUI serverSelectorGUI;

    public ServersCommand(HytaleLogger logger, ServerSelectorGUI serverSelectorGUI) {
        this.logger = logger;
        this.serverSelectorGUI = serverSelectorGUI;
    }

    /**
     * Execute the servers command
     */
    public boolean execute(Player player, String[] args) {
        serverSelectorGUI.open(player);
        return true;
    }
}
