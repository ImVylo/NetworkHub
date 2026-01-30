package com.hytale.networkhub.gui;

import com.hytale.networkhub.config.NetworkConfig;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.hypixel.hytale.logger.HytaleLogger;
import java.util.logging.Level;

/**
 * Central manager for all GUI systems in the network
 * Handles GUI registration, opening, and lifecycle management
 */
public class GUIManager {
    private final HytaleLogger logger;
    private final NetworkConfig config;

    // Track active GUIs per player
    private final Map<UUID, String> activeGUIs = new ConcurrentHashMap<>();

    // Track GUI instances for cleanup
    private final Map<String, Object> guiInstances = new ConcurrentHashMap<>();

    public GUIManager(HytaleLogger logger, NetworkConfig config) {
        this.logger = logger;
        this.config = config;
    }

    public void initialize() {
        if (!config.getConfig().gui.enabled) {
            logger.at(Level.INFO).log("GUI system is disabled in config");
            return;
        }

        logger.at(Level.INFO).log("Initializing GUI system...");

        // GUI instances will be registered by their respective managers
        // This allows lazy initialization and dependency injection

        logger.at(Level.INFO).log("GUI system initialized");
    }

    /**
     * Register a GUI instance for management
     */
    public void registerGUI(String guiId, Object guiInstance) {
        guiInstances.put(guiId, guiInstance);
        logger.at(Level.FINE).log("Registered GUI: " + guiId);
    }

    /**
     * Track that a player has opened a specific GUI
     */
    public void trackGUIOpen(Player player, String guiId) {
        activeGUIs.put(player.getPlayerRef().getUuid(), guiId);
        logger.at(Level.FINE).log(player.getPlayerRef().getUsername() + " opened GUI: " + guiId);
    }

    /**
     * Track that a player has closed their GUI
     */
    public void trackGUIClose(Player player) {
        String guiId = activeGUIs.remove(player.getPlayerRef().getUuid());
        if (guiId != null) {
            logger.at(Level.FINE).log(player.getPlayerRef().getUsername() + " closed GUI: " + guiId);
        }
    }

    /**
     * Get the currently active GUI for a player
     */
    public String getActiveGUI(UUID playerUuid) {
        return activeGUIs.get(playerUuid);
    }

    /**
     * Check if GUIs are enabled in config
     */
    public boolean isEnabled() {
        return config.getConfig().gui.enabled;
    }

    /**
     * Get GUI refresh interval from config
     */
    public int getRefreshIntervalSeconds() {
        return config.getConfig().gui.refreshIntervalSeconds;
    }

    /**
     * Check if player server selection is allowed
     */
    public boolean canPlayersSelectServers() {
        return config.getConfig().gui.allowPlayerServerSelection;
    }

    /**
     * Close all active GUIs (used on shutdown)
     */
    public void closeAllGUIs() {
        logger.at(Level.INFO).log("Closing all active GUIs...");
        activeGUIs.clear();
        guiInstances.clear();
    }
}
