package com.hytale.networkhub.listeners;

import com.hytale.networkhub.managers.ChatManager;
import com.hytale.networkhub.managers.MessagingManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;

import com.hypixel.hytale.logger.HytaleLogger;

public class PlayerChatListener {
    private final HytaleLogger logger;
    private final ChatManager chatManager;
    private final MessagingManager messagingManager;

    public PlayerChatListener(HytaleLogger logger, ChatManager chatManager, MessagingManager messagingManager) {
        this.logger = logger;
        this.chatManager = chatManager;
        this.messagingManager = messagingManager;
    }

    public boolean onPlayerChat(Player player, String message) {
        // Check for global chat prefix
        if (message.startsWith("/g ")) {
            String actualMessage = message.substring(3);
            chatManager.sendGlobalMessage(player.getPlayerRef().getUuid(), player.getPlayerRef().getUsername(), actualMessage);
            return true; // Cancel normal chat
        }

        // Check for staff chat prefix
        if (message.startsWith("/sc ")) {
            String actualMessage = message.substring(4);
            // TODO: Check if player has staff permission
            chatManager.sendStaffMessage(player.getPlayerRef().getUuid(), player.getPlayerRef().getUsername(), actualMessage);
            return true; // Cancel normal chat
        }

        return false; // Allow normal chat
    }
}
