package com.hytale.networkhub.gui.hud;

import com.hytale.networkhub.config.NetworkConfig;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Handles the actual rendering of the network HUD scoreboard
 * Converts network stats into formatted scoreboard lines
 */
public class HUDRenderer {
    private final Logger logger;
    private final NetworkConfig config;

    // Track active scoreboards per player
    private final Map<UUID, Object> activeScoreboards = new ConcurrentHashMap<>();

    public HUDRenderer(Logger logger, NetworkConfig config) {
        this.logger = logger;
        this.config = config;
    }

    /**
     * Create a new scoreboard for a player
     */
    public void createScoreboard(Player player, NetworkHUD.NetworkStats stats) {
        try {
            // TODO: Use Hytale's scoreboard API when available
            // For now, send formatted chat messages as fallback
            List<String> lines = buildLines(player, stats);

            // Send lines as chat messages for now
            player.sendMessage("§8§m------------------");
            player.sendMessage("§6§lNETWORK HUD");
            player.sendMessage("§8§m------------------");
            for (String line : lines) {
                player.sendMessage(line);
            }
            player.sendMessage("§8§m------------------");

            // TODO: Replace with actual scoreboard creation:
            // Scoreboard scoreboard = new Scoreboard("network_hud");
            // Objective objective = scoreboard.createObjective("network", "dummy");
            // objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            // objective.setDisplayName("§6§lHYTALE NETWORK");
            // player.setScoreboard(scoreboard);
            // activeScoreboards.put(player.getUniqueId(), scoreboard);

        } catch (Exception e) {
            logger.warning("Failed to create scoreboard for " + player.getUsername() + ": " + e.getMessage());
        }
    }

    /**
     * Update an existing scoreboard with new stats
     */
    public void updateScoreboard(Player player, NetworkHUD.NetworkStats stats) {
        Object scoreboard = activeScoreboards.get(player.getUniqueId());
        if (scoreboard == null) {
            // Scoreboard doesn't exist, create it
            createScoreboard(player, stats);
            return;
        }

        try {
            // TODO: Use Hytale's scoreboard API to update scores
            // For now, this is a no-op since we can't update chat messages
            // Once scoreboard API is available:
            // Objective objective = scoreboard.getObjective("network");
            // objective.clearScores();
            // List<String> lines = buildLines(player, stats);
            // int lineNumber = 15;
            // for (String line : lines) {
            //     objective.setScore(line, lineNumber--);
            // }

        } catch (Exception e) {
            logger.warning("Failed to update scoreboard for " + player.getUsername() + ": " + e.getMessage());
        }
    }

    /**
     * Remove a player's scoreboard
     */
    public void removeScoreboard(Player player) {
        Object scoreboard = activeScoreboards.remove(player.getUniqueId());
        if (scoreboard != null) {
            // TODO: Clear player's scoreboard with Hytale API
            // player.setScoreboard(null);
            player.sendMessage("§7HUD disabled");
        }
    }

    /**
     * Build the list of formatted lines to display
     */
    private List<String> buildLines(Player player, NetworkHUD.NetworkStats stats) {
        List<String> lines = new ArrayList<>();
        List<String> displayLines = config.getConfig().hud.displayLines;

        for (String lineType : displayLines) {
            String line = buildLine(lineType, player, stats);
            if (line != null && !line.isEmpty()) {
                lines.add(line);
            }
        }

        return lines;
    }

    /**
     * Build a single line based on type
     */
    private String buildLine(String lineType, Player player, NetworkHUD.NetworkStats stats) {
        switch (lineType.toLowerCase()) {
            case "network_name":
                return "§6§lHYTALE NETWORK";

            case "separator":
                return "§7§m---------------";

            case "blank":
                return " ";

            case "total_players":
                return String.format("§aPlayers: §f%d", stats.getTotalPlayers());

            case "server_list":
                return buildServerList(stats);

            case "server_health":
                return String.format("§aServers: §f%d/%d Online",
                    stats.getOnlineServers(), stats.getTotalServers());

            case "queue_info":
                int queueCount = stats.getTotalQueuedPlayers();
                if (queueCount > 0) {
                    return String.format("§eQueue: §f%d waiting", queueCount);
                }
                return null; // Hide if no queue

            case "your_server":
                return String.format("§7You: §a%s", stats.getCurrentServerId());

            case "notifications":
                // TODO: Get unread message count
                // int unreadMessages = messagingManager.getUnreadCount(player.getUniqueId());
                // if (unreadMessages > 0) {
                //     return String.format("§8[§6!§8] §f%d new msg%s",
                //         unreadMessages, unreadMessages == 1 ? "" : "s");
                // }
                return null;

            default:
                logger.warning("Unknown HUD line type: " + lineType);
                return null;
        }
    }

    /**
     * Build server list display
     * In compact mode: shows totals per type
     * In normal mode: shows each server individually
     */
    private String buildServerList(NetworkHUD.NetworkStats stats) {
        if (config.getConfig().hud.compactMode) {
            // Compact: "§7Hub: §f45 §8| §7Game: §f78"
            return String.format("§7Hub: §f%d §8| §7Game: §f%d",
                stats.getHubPlayers(), stats.getGamePlayers());
        } else {
            // Normal mode: show top servers (limited to avoid clutter)
            // This will be multiple lines, but we'll return the first line here
            // TODO: Improve to support multiple lines properly
            Map<String, NetworkHUD.ServerInfo> servers = stats.getServerInfoMap();
            if (servers.isEmpty()) {
                return "§7No servers online";
            }

            // Get first server as example
            NetworkHUD.ServerInfo firstServer = servers.values().iterator().next();
            return String.format("§7%s: §f%d/%d",
                firstServer.getName(), firstServer.getPlayers(), firstServer.getMaxPlayers());
        }
    }

    /**
     * Build detailed server list (for non-compact mode)
     * Returns multiple lines
     */
    private List<String> buildDetailedServerList(NetworkHUD.NetworkStats stats) {
        List<String> lines = new ArrayList<>();
        Map<String, NetworkHUD.ServerInfo> servers = stats.getServerInfoMap();

        // Sort servers: hubs first, then by name
        List<NetworkHUD.ServerInfo> sortedServers = new ArrayList<>(servers.values());
        sortedServers.sort((a, b) -> {
            if (a.isHub() != b.isHub()) {
                return a.isHub() ? -1 : 1;
            }
            return a.getName().compareTo(b.getName());
        });

        // Limit to top 5 servers to avoid clutter
        int count = 0;
        for (NetworkHUD.ServerInfo server : sortedServers) {
            if (count >= 5) break;

            // Highlight current server
            boolean isCurrent = server.getId().equals(stats.getCurrentServerId());
            String prefix = isCurrent ? "§a§l" : "§7";

            lines.add(String.format("%s%s: §f%d/%d",
                prefix, server.getName(), server.getPlayers(), server.getMaxPlayers()));

            count++;
        }

        return lines;
    }

    /**
     * Format color codes in text
     */
    private String formatColors(String text) {
        return text.replace("&", "§");
    }
}
