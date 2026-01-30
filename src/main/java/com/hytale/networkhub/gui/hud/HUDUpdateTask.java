package com.hytale.networkhub.gui.hud;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;

import java.util.Collection;
import com.hypixel.hytale.logger.HytaleLogger;
import java.util.logging.Level;

/**
 * Scheduled task that updates the network HUD for all players
 * Runs every 2 seconds (configurable) to refresh stats
 */
public class HUDUpdateTask implements Runnable {
    private final HytaleLogger logger;
    private final NetworkHUD networkHUD;
    private final Collection<Player> onlinePlayers;

    public HUDUpdateTask(HytaleLogger logger, NetworkHUD networkHUD, Collection<Player> onlinePlayers) {
        this.logger = logger;
        this.networkHUD = networkHUD;
        this.onlinePlayers = onlinePlayers;
    }

    @Override
    public void run() {
        try {
            // Update HUD for all online players
            networkHUD.updateAll(onlinePlayers);
        } catch (Exception e) {
            logger.at(Level.WARNING).log("Failed to update network HUD: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
