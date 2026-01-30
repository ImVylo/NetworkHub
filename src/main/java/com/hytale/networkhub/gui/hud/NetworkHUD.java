package com.hytale.networkhub.gui.hud;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.database.models.ServerRecord;
import com.hytale.networkhub.managers.MessagingManager;
import com.hytale.networkhub.managers.QueueManager;
import com.hytale.networkhub.managers.ServerRegistryManager;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Manages the persistent network HUD (sidebar) for players in hub servers
 * Displays real-time network statistics, player counts, and notifications
 */
public class NetworkHUD {
    private final Logger logger;
    private final NetworkConfig config;
    private final ServerRegistryManager registryManager;
    private final QueueManager queueManager;
    private final MessagingManager messagingManager;
    private final HUDRenderer renderer;

    // Track which players have HUD enabled
    private final Map<UUID, Boolean> hudEnabled = new ConcurrentHashMap<>();

    // Cache network stats to avoid excessive database queries
    private NetworkStats cachedStats;
    private long lastStatsUpdate = 0;

    public NetworkHUD(Logger logger, NetworkConfig config, ServerRegistryManager registryManager,
                     QueueManager queueManager, MessagingManager messagingManager, HUDRenderer renderer) {
        this.logger = logger;
        this.config = config;
        this.registryManager = registryManager;
        this.queueManager = queueManager;
        this.messagingManager = messagingManager;
        this.renderer = renderer;
    }

    /**
     * Enable HUD for a player
     */
    public void enable(Player player) {
        // Only show in hub servers
        if (config.getConfig().hud.hubOnly && !config.getConfig().server.isHub) {
            return;
        }

        if (!config.getConfig().hud.enabled) {
            return;
        }

        // Check permission
        // TODO: Implement when permission system is available
        // if (!player.hasPermission("networkhub.hud")) return;

        hudEnabled.put(player.getUniqueId(), true);

        // Create initial scoreboard
        NetworkStats stats = getNetworkStats();
        renderer.createScoreboard(player, stats);

        logger.fine("Enabled network HUD for " + player.getUsername());
    }

    /**
     * Disable HUD for a player
     */
    public void disable(Player player) {
        hudEnabled.remove(player.getUniqueId());
        renderer.removeScoreboard(player);
        logger.fine("Disabled network HUD for " + player.getUsername());
    }

    /**
     * Toggle HUD for a player
     */
    public boolean toggle(Player player) {
        if (isEnabled(player.getUniqueId())) {
            disable(player);
            return false;
        } else {
            enable(player);
            return true;
        }
    }

    /**
     * Check if HUD is enabled for a player
     */
    public boolean isEnabled(UUID playerUuid) {
        return hudEnabled.getOrDefault(playerUuid, false);
    }

    /**
     * Update HUD for all active players
     * Called by HUDUpdateTask every 2 seconds
     */
    public void updateAll(Collection<Player> onlinePlayers) {
        if (!config.getConfig().hud.enabled) {
            return;
        }

        // Gather fresh network stats
        NetworkStats stats = getNetworkStats();

        // Update each player's HUD
        for (Player player : onlinePlayers) {
            if (isEnabled(player.getUniqueId())) {
                try {
                    renderer.updateScoreboard(player, stats);
                } catch (Exception e) {
                    logger.warning("Failed to update HUD for " + player.getUsername() + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Gather current network statistics
     * Cached for performance (only refreshes every update interval)
     */
    private NetworkStats getNetworkStats() {
        long now = System.currentTimeMillis();
        int updateInterval = config.getConfig().hud.updateIntervalSeconds * 1000;

        // Return cached stats if still fresh
        if (cachedStats != null && (now - lastStatsUpdate) < updateInterval) {
            return cachedStats;
        }

        // Gather fresh stats
        NetworkStats stats = new NetworkStats();

        try {
            // Get all servers
            List<ServerRecord> servers = registryManager.getAllServers();
            stats.setTotalServers(servers.size());

            int totalPlayers = 0;
            int onlineServers = 0;
            int hubPlayers = 0;
            int gamePlayers = 0;
            Map<String, ServerInfo> serverInfoMap = new HashMap<>();

            for (ServerRecord server : servers) {
                if (server.getStatus() == ServerRecord.ServerStatus.ONLINE) {
                    onlineServers++;
                    int playerCount = server.getCurrentPlayers();
                    totalPlayers += playerCount;

                    if (server.isHub()) {
                        hubPlayers += playerCount;
                    } else {
                        gamePlayers += playerCount;
                    }

                    // Store server info for detailed display
                    serverInfoMap.put(server.getServerId(), new ServerInfo(
                        server.getServerName(),
                        server.getServerId(),
                        playerCount,
                        server.getMaxPlayers(),
                        server.isHub()
                    ));
                }
            }

            stats.setTotalPlayers(totalPlayers);
            stats.setOnlineServers(onlineServers);
            stats.setHubPlayers(hubPlayers);
            stats.setGamePlayers(gamePlayers);
            stats.setServerInfoMap(serverInfoMap);

            // Get queue counts
            int totalQueued = queueManager.getTotalQueuedPlayers();
            stats.setTotalQueuedPlayers(totalQueued);

            // Store current server ID
            stats.setCurrentServerId(config.getConfig().server.serverId);

            // Cache the stats
            cachedStats = stats;
            lastStatsUpdate = now;

        } catch (Exception e) {
            logger.warning("Failed to gather network stats: " + e.getMessage());
            // Return cached stats if available
            if (cachedStats != null) {
                return cachedStats;
            }
            // Return empty stats as fallback
            stats = new NetworkStats();
        }

        return stats;
    }

    /**
     * Force refresh of cached stats
     */
    public void refreshStats() {
        lastStatsUpdate = 0;
        cachedStats = null;
    }

    /**
     * Get unread message count for a player
     */
    public int getUnreadCount(UUID playerUuid) {
        return messagingManager.getUnreadCount(playerUuid);
    }

    /**
     * Container for network statistics
     */
    public static class NetworkStats {
        private int totalPlayers;
        private int totalServers;
        private int onlineServers;
        private int hubPlayers;
        private int gamePlayers;
        private int totalQueuedPlayers;
        private String currentServerId;
        private Map<String, ServerInfo> serverInfoMap = new HashMap<>();

        // Getters and setters
        public int getTotalPlayers() { return totalPlayers; }
        public void setTotalPlayers(int totalPlayers) { this.totalPlayers = totalPlayers; }

        public int getTotalServers() { return totalServers; }
        public void setTotalServers(int totalServers) { this.totalServers = totalServers; }

        public int getOnlineServers() { return onlineServers; }
        public void setOnlineServers(int onlineServers) { this.onlineServers = onlineServers; }

        public int getHubPlayers() { return hubPlayers; }
        public void setHubPlayers(int hubPlayers) { this.hubPlayers = hubPlayers; }

        public int getGamePlayers() { return gamePlayers; }
        public void setGamePlayers(int gamePlayers) { this.gamePlayers = gamePlayers; }

        public int getTotalQueuedPlayers() { return totalQueuedPlayers; }
        public void setTotalQueuedPlayers(int totalQueuedPlayers) { this.totalQueuedPlayers = totalQueuedPlayers; }

        public String getCurrentServerId() { return currentServerId; }
        public void setCurrentServerId(String currentServerId) { this.currentServerId = currentServerId; }

        public Map<String, ServerInfo> getServerInfoMap() { return serverInfoMap; }
        public void setServerInfoMap(Map<String, ServerInfo> serverInfoMap) { this.serverInfoMap = serverInfoMap; }
    }

    /**
     * Container for individual server information
     */
    public static class ServerInfo {
        private final String name;
        private final String id;
        private final int players;
        private final int maxPlayers;
        private final boolean isHub;

        public ServerInfo(String name, String id, int players, int maxPlayers, boolean isHub) {
            this.name = name;
            this.id = id;
            this.players = players;
            this.maxPlayers = maxPlayers;
            this.isHub = isHub;
        }

        public String getName() { return name; }
        public String getId() { return id; }
        public int getPlayers() { return players; }
        public int getMaxPlayers() { return maxPlayers; }
        public boolean isHub() { return isHub; }
    }
}
