package com.hytale.networkhub.commands;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.managers.ChatManager;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.logging.Logger;

/**
 * Command to send messages to staff-only network chat
 */
public class StaffChatCommand {
    private final Logger logger;
    private final NetworkConfig config;
    private final ChatManager chatManager;

    public StaffChatCommand(Logger logger, NetworkConfig config, ChatManager chatManager) {
        this.logger = logger;
        this.config = config;
        this.chatManager = chatManager;
    }

    /**
     * Execute the staff chat command
     */
    public boolean execute(Player player, String[] args) {
        if (!config.getConfig().messaging.staffChatEnabled) {
            player.sendMessage("§cStaff chat is disabled");
            return true;
        }

        // Check permission
        // TODO: Implement when permission system is available
        // if (!player.hasPermission("networkhub.staffchat")) {
        //     player.sendMessage("§cYou don't have permission to use staff chat");
        //     return true;
        // }

        if (args.length < 1) {
            player.sendMessage("§cUsage: /sc <message>");
            return true;
        }

        String message = String.join(" ", args);

        // Send to staff chat
        chatManager.sendStaffMessage(player.getUniqueId(), player.getUsername(), message);

        return true;
    }
}
