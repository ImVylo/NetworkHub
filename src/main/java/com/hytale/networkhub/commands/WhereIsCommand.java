package com.hytale.networkhub.commands;

import com.hytale.networkhub.database.models.PlayerLocation;
import com.hytale.networkhub.managers.PlayerTrackingManager;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;

import javax.annotation.Nonnull;

/**
 * Command to find which server a player is on
 */
public class WhereIsCommand extends CommandBase {
    private final PlayerTrackingManager trackingManager;

    public WhereIsCommand(PlayerTrackingManager trackingManager) {
        super("whereis", "Find which server a player is on");
        this.trackingManager = trackingManager;
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        CommandSender sender = context.sender();

        // TODO: Parse player name argument from context
        // For now, show usage message
        context.sendMessage(Message.raw("§cCommand not yet fully implemented"));
        context.sendMessage(Message.raw("§eUsage: /whereis <player>"));
    }
}
