package com.hytale.networkhub.commands;

import com.hytale.networkhub.gui.menus.ServerSelectorGUI;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;

import java.util.logging.Logger;

/**
 * Command to open the server selector GUI
 * Allows players to browse and join available servers
 */
public class ServersCommand {
    private final Logger logger;
    private final ServerSelectorGUI serverSelectorGUI;

    public ServersCommand(Logger logger, ServerSelectorGUI serverSelectorGUI) {
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
