package com.hytale.networkhub.commands;

import com.hytale.networkhub.database.models.PlayerLocation;
import com.hytale.networkhub.managers.PlayerTrackingManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;

import com.hypixel.hytale.logger.HytaleLogger;

/**
 * Command to find which server a player is on
 */
public class WhereIsCommand {
    private final HytaleLogger logger;
    private final PlayerTrackingManager trackingManager;

    public WhereIsCommand(HytaleLogger logger, PlayerTrackingManager trackingManager) {
        this.logger = logger;
        this.trackingManager = trackingManager;
    }

    /**
     * Execute the whereis command
     */
    public boolean execute(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(Message.raw("§cUsage: /whereis <player>"));
            return true;
        }

        String targetName = args[0];

        // Find player
        PlayerLocation location = trackingManager.findPlayerByName(targetName);

        if (location == null) {
            player.sendMessage(Message.raw("§cPlayer not found: " + targetName));
            return true;
        }

        player.sendMessage(Message.raw("§8§m-------------------------"));
        player.sendMessage(Message.raw("§6§lPlayer Location"));
        player.sendMessage(Message.raw("§8§m-------------------------"));
        player.sendMessage(Message.raw("§ePlayer: §f" + location.getPlayerName()));
        player.sendMessage(Message.raw("§eServer: §a" + location.getServerId()));

        if (location.getWorldName() != null) {
            player.sendMessage(Message.raw("§eWorld: §f" + location.getWorldName()));
            player.sendMessage(Message.raw(String.format("§ePosition: §f%d, %d, %d",
                location.getX(), location.getY(), location.getZ())));
        }

        player.sendMessage(Message.raw("§8§m-------------------------"));

        return true;
    }
}
