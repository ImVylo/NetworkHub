package com.hytale.networkhub.commands;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.managers.ChatManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;

import com.hypixel.hytale.logger.HytaleLogger;

/**
 * Command to send messages to staff-only network chat
 */
public class StaffChatCommand {
    private final HytaleLogger logger;
    private final NetworkConfig config;
    private final ChatManager chatManager;

    public StaffChatCommand(HytaleLogger logger, NetworkConfig config, ChatManager chatManager) {
        this.logger = logger;
        this.config = config;
        this.chatManager = chatManager;
    }

    /**
     * Execute the staff chat command
     */
    public boolean execute(Player player, String[] args) {
        if (!config.getConfig().messaging.staffChatEnabled) {
            player.sendMessage(Message.raw("§cStaff chat is disabled"));
            return true;
        }

        // Check permission
        // TODO: Implement when permission system is available
        // if (!player.hasPermission("networkhub.staffchat")) {
        //     player.sendMessage(Message.raw("§cYou don't have permission to use staff chat"));
        //     return true;
        // }

        if (args.length < 1) {
            player.sendMessage(Message.raw("§cUsage: /sc <message>"));
            return true;
        }

        String message = String.join(" ", args);

        // Send to staff chat
        chatManager.sendStaffMessage(player.getPlayerRef().getUuid(), player.getPlayerRef().getUsername(), message);

        return true;
    }
}
