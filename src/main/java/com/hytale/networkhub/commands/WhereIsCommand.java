package com.hytale.networkhub.commands;

import com.hytale.networkhub.database.models.PlayerLocation;
import com.hytale.networkhub.managers.PlayerTrackingManager;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.logging.Logger;

/**
 * Command to find which server a player is on
 */
public class WhereIsCommand {
    private final Logger logger;
    private final PlayerTrackingManager trackingManager;

    public WhereIsCommand(Logger logger, PlayerTrackingManager trackingManager) {
        this.logger = logger;
        this.trackingManager = trackingManager;
    }

    /**
     * Execute the whereis command
     */
    public boolean execute(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage("§cUsage: /whereis <player>");
            return true;
        }

        String targetName = args[0];

        // Find player
        PlayerLocation location = trackingManager.findPlayerByName(targetName);

        if (location == null) {
            player.sendMessage("§cPlayer not found: " + targetName);
            return true;
        }

        player.sendMessage("§8§m-------------------------");
        player.sendMessage("§6§lPlayer Location");
        player.sendMessage("§8§m-------------------------");
        player.sendMessage("§ePlayer: §f" + location.getPlayerName());
        player.sendMessage("§eServer: §a" + location.getServerId());

        if (location.getWorldName() != null) {
            player.sendMessage("§eWorld: §f" + location.getWorldName());
            player.sendMessage(String.format("§ePosition: §f%d, %d, %d",
                location.getX(), location.getY(), location.getZ()));
        }

        player.sendMessage("§8§m-------------------------");

        return true;
    }
}
