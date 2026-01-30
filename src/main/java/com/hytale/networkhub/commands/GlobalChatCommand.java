package com.hytale.networkhub.commands;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.managers.ChatManager;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.logging.Logger;

/**
 * Command to send messages to global network chat
 */
public class GlobalChatCommand {
    private final Logger logger;
    private final NetworkConfig config;
    private final ChatManager chatManager;

    public GlobalChatCommand(Logger logger, NetworkConfig config, ChatManager chatManager) {
        this.logger = logger;
        this.config = config;
        this.chatManager = chatManager;
    }

    /**
     * Execute the global chat command
     */
    public boolean execute(Player player, String[] args) {
        if (!config.getConfig().messaging.globalChatEnabled) {
            player.sendMessage("§cGlobal chat is disabled");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("§cUsage: /g <message>");
            return true;
        }

        String message = String.join(" ", args);

        // Send to global chat
        chatManager.sendGlobalMessage(player.getUniqueId(), player.getUsername(), message);

        return true;
    }
}
