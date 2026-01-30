package com.hytale.networkhub.commands;

import com.hytale.networkhub.database.models.PlayerLocation;
import com.hytale.networkhub.managers.MessagingManager;
import com.hytale.networkhub.managers.PlayerTrackingManager;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.logging.Logger;

/**
 * Command to send direct messages to players across the network
 */
public class MessageCommand {
    private final Logger logger;
    private final MessagingManager messagingManager;
    private final PlayerTrackingManager trackingManager;

    public MessageCommand(Logger logger, MessagingManager messagingManager,
                         PlayerTrackingManager trackingManager) {
        this.logger = logger;
        this.messagingManager = messagingManager;
        this.trackingManager = trackingManager;
    }

    /**
     * Execute the message command
     */
    public boolean execute(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /msg <player> <message>");
            return true;
        }

        String recipientName = args[0];
        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        // Find recipient
        PlayerLocation recipientLoc = trackingManager.findPlayerByName(recipientName);
        if (recipientLoc == null) {
            player.sendMessage("§cPlayer not found: " + recipientName);
            return true;
        }

        // Send message
        boolean success = messagingManager.sendDirectMessage(
            player.getUniqueId(),
            player.getUsername(),
            recipientLoc.getPlayerUuid(),
            message
        );

        if (success) {
            player.sendMessage(String.format("§d[To %s] §f%s", recipientName, message));
        } else {
            player.sendMessage("§cFailed to send message");
        }

        return true;
    }
}
