package com.hytale.networkhub.commands;

import com.hytale.networkhub.gui.menus.ServerSelectorGUI;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;

import javax.annotation.Nonnull;

/**
 * Command to open the server selector GUI
 * Allows players to browse and join available servers
 */
public class ServersCommand extends CommandBase {
    private final ServerSelectorGUI serverSelectorGUI;

    public ServersCommand(ServerSelectorGUI serverSelectorGUI) {
        super("servers", "Open the network server selector");
        this.serverSelectorGUI = serverSelectorGUI;
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        CommandSender sender = context.sender();

        if (!(sender instanceof Player)) {
            context.sendMessage(Message.raw("§cThis command can only be used by players"));
            return;
        }

        Player player = (Player) sender;
        serverSelectorGUI.open(player);
    }
}
