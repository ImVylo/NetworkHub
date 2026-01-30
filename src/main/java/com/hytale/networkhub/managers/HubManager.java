package com.hytale.networkhub.managers;

import com.hytale.networkhub.database.DatabaseManager;
import com.hytale.networkhub.database.models.ServerRecord;

import java.util.List;
import com.hypixel.hytale.logger.HytaleLogger;
import java.util.logging.Level;

public class HubManager {
    private final HytaleLogger logger;
    private final DatabaseManager dbManager;
    private final ServerRegistryManager registryManager;

    public HubManager(HytaleLogger logger, DatabaseManager dbManager, ServerRegistryManager registryManager) {
        this.logger = logger;
        this.dbManager = dbManager;
        this.registryManager = registryManager;
    }

    public void setHub(String serverId, int priority) {
        String sql = "UPDATE servers SET is_hub = true, hub_priority = ? WHERE server_id = ?";
        int rows = dbManager.executeUpdate(sql, priority, serverId);

        if (rows > 0) {
            logger.at(Level.INFO).log("Set server " + serverId + " as hub with priority " + priority);
        }
    }

    public void unsetHub(String serverId) {
        String sql = "UPDATE servers SET is_hub = false, hub_priority = 0 WHERE server_id = ?";
        int rows = dbManager.executeUpdate(sql, serverId);

        if (rows > 0) {
            logger.at(Level.INFO).log("Removed hub designation from server " + serverId);
        }
    }

    public List<ServerRecord> getAvailableHubs() {
        return registryManager.getHubServers().stream()
            .filter(s -> s.getStatus() == ServerRecord.ServerStatus.ONLINE)
            .sorted((a, b) -> {
                if (a.getHubPriority() != b.getHubPriority()) {
                    return Integer.compare(b.getHubPriority(), a.getHubPriority());
                }
                return Integer.compare(a.getCurrentPlayers(), b.getCurrentPlayers());
            })
            .toList();
    }

    public ServerRecord selectFallbackHub() {
        List<ServerRecord> hubs = getAvailableHubs();

        if (hubs.isEmpty()) {
            logger.at(Level.SEVERE).log("No available hub servers found for fallback!");

            // Try to find ANY online server as last resort
            List<ServerRecord> anyServer = registryManager.getOnlineServers();
            if (!anyServer.isEmpty()) {
                logger.at(Level.WARNING).log("Using non-hub server as fallback: " + anyServer.get(0).getServerName());
                return anyServer.get(0);
            }

            throw new RuntimeException("No online servers available for fallback!");
        }

        ServerRecord selectedHub = hubs.get(0);
        logger.at(Level.INFO).log("Selected fallback hub: " + selectedHub.getServerName() +
                   " (Priority: " + selectedHub.getHubPriority() +
                   ", Players: " + selectedHub.getCurrentPlayers() + "/" + selectedHub.getMaxPlayers() + ")");

        return selectedHub;
    }
}
