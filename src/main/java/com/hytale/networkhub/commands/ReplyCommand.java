package com.hytale.networkhub.commands;

import com.hytale.networkhub.database.models.PlayerLocation;
import com.hytale.networkhub.managers.MessagingManager;
import com.hytale.networkhub.managers.PlayerTrackingManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * Command to reply to the last received direct message
 */
public class ReplyCommand {
    private final Logger logger;
    private final MessagingManager messagingManager;
    private final PlayerTrackingManager trackingManager;

    public ReplyCommand(Logger logger, MessagingManager messagingManager,
                       PlayerTrackingManager trackingManager) {
        this.logger = logger;
        this.messagingManager = messagingManager;
        this.trackingManager = trackingManager;
    }

    /**
     * Execute the reply command
     */
    public boolean execute(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(Message.raw("§cUsage: /reply <message>"));
            return true;
        }

        String message = String.join(" ", args);

        // Get last conversation partner
        UUID recipientUuid = messagingManager.getLastConversationPartner(player.getPlayerRef().getUuid());
        if (recipientUuid == null) {
            player.sendMessage(Message.raw("§cNo one to reply to"));
            return true;
        }

        // Find recipient
        PlayerLocation recipientLoc = trackingManager.findPlayer(recipientUuid);
        if (recipientLoc == null) {
            player.sendMessage(Message.raw("§cPlayer is no longer online"));
            return true;
        }

        // Send message
        boolean success = messagingManager.sendDirectMessage(
            player.getPlayerRef().getUuid(),
            player.getPlayerRef().getUsername(),
            recipientUuid,
            message
        );

        if (success) {
            player.sendMessage(Message.raw(String.format("§d[To %s] §f%s", recipientLoc.getPlayerName(), message));
        } else {
            player.sendMessage(Message.raw("§cFailed to send message"));
        }

        return true;
    }
}
