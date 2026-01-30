package com.hytale.networkhub.commands;

import com.hytale.networkhub.gui.menus.AdminPanelGUI;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;

import javax.annotation.Nonnull;

/**
 * Command to open the network admin panel GUI
 */
public class NetworkGUICommand extends CommandBase {
    private final AdminPanelGUI adminPanelGUI;

    public NetworkGUICommand(AdminPanelGUI adminPanelGUI) {
        super("networkgui", "Open the network administration panel");
        this.adminPanelGUI = adminPanelGUI;
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        CommandSender sender = context.sender();

        if (!(sender instanceof Player)) {
            context.sendMessage(Message.raw("§cThis command can only be used by players"));
            return;
        }

        Player player = (Player) sender;
        adminPanelGUI.open(player);
    }
}
