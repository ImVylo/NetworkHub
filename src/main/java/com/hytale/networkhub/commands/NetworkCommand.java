package com.hytale.networkhub.commands;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.database.models.ServerRecord;
import com.hytale.networkhub.managers.*;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;

import java.util.List;
import java.util.logging.Logger;

/**
 * Main network administration command
 * Routes to various subcommands for network management
 */
public class NetworkCommand {
    private final Logger logger;
    private final NetworkConfig config;
    private final ServerRegistryManager registryManager;
    private final HubManager hubManager;
    private final TransferManager transferManager;
    private final PlayerTrackingManager trackingManager;

    public NetworkCommand(Logger logger, NetworkConfig config, ServerRegistryManager registryManager,
                         HubManager hubManager, TransferManager transferManager,
                         PlayerTrackingManager trackingManager) {
        this.logger = logger;
        this.config = config;
        this.registryManager = registryManager;
        this.hubManager = hubManager;
        this.transferManager = transferManager;
        this.trackingManager = trackingManager;
    }

    /**
     * Execute the network command
     */
    public boolean execute(Player player, String[] args) {
        // Check permission
        // TODO: Implement when permission system is available
        // if (!player.hasPermission("networkhub.admin")) {
        //     player.sendMessage(Message.raw("§cYou don't have permission to use this command"));
        //     return true;
        // }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "listservers":
            case "servers":
                return listServers(player);

            case "sethub":
                return setHub(player, args);

            case "unsethub":
                return unsetHub(player, args);

            case "listhubs":
            case "hubs":
                return listHubs(player);

            case "transfer":
                return transferPlayer(player, args);

            case "transferall":
                return transferAllPlayers(player, args);

            case "register":
                return registerServer(player, args);

            case "unregister":
                return unregisterServer(player, args);

            case "reload":
                return reloadConfig(player);

            default:
                player.sendMessage(Message.raw("§cUnknown subcommand: " + subcommand);
                sendHelp(player);
                return true;
        }
    }

    /**
     * Send help message
     */
    private void sendHelp(Player player) {
        player.sendMessage(Message.raw("§8§m-------------------------"));
        player.sendMessage(Message.raw("§6§lNetworkHub Commands"));
        player.sendMessage(Message.raw("§8§m-------------------------"));
        player.sendMessage(Message.raw("§e/network listservers §7- List all servers"));
        player.sendMessage(Message.raw("§e/network sethub <server> [priority] §7- Set hub"));
        player.sendMessage(Message.raw("§e/network unsethub <server> §7- Remove hub"));
        player.sendMessage(Message.raw("§e/network listhubs §7- List all hubs"));
        player.sendMessage(Message.raw("§e/network transfer <player> <server> §7- Transfer player"));
        player.sendMessage(Message.raw("§e/network transferall <server> §7- Transfer all players"));
        player.sendMessage(Message.raw("§e/network register <id> <host> <port> <name> §7- Register server"));
        player.sendMessage(Message.raw("§e/network unregister <server> §7- Unregister server"));
        player.sendMessage(Message.raw("§e/network reload §7- Reload config"));
        player.sendMessage(Message.raw("§8§m-------------------------"));
    }

    /**
     * List all servers in the network
     */
    private boolean listServers(Player player) {
        List<ServerRecord> servers = registryManager.getAllServers();

        player.sendMessage(Message.raw("§8§m-------------------------"));
        player.sendMessage(Message.raw("§6§lNetwork Servers §7(" + servers.size() + ")");
        player.sendMessage(Message.raw("§8§m-------------------------"));

        if (servers.isEmpty()) {
            player.sendMessage(Message.raw("§7No servers registered"));
            return true;
        }

        for (ServerRecord server : servers) {
            String statusColor = getStatusColor(server.getStatus());
            String hubBadge = server.isHub() ? " §e[HUB:" + server.getHubPriority() + "]" : "";

            player.sendMessage(Message.raw(String.format(
                "%s● §e%s §7(%s)%s §7- §f%d/%d §7players",
                statusColor,
                server.getServerName(),
                server.getServerId(),
                hubBadge,
                server.getCurrentPlayers(),
                server.getMaxPlayers()
            ));
        }

        player.sendMessage(Message.raw("§8§m-------------------------"));
        return true;
    }

    /**
     * Set a server as hub with optional priority
     */
    private boolean setHub(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Message.raw("§cUsage: /network sethub <server> [priority]"));
            return true;
        }

        String serverId = args[1];
        int priority = args.length >= 3 ? parseInt(args[2], 100) : 100;

        ServerRecord server = registryManager.getServerById(serverId);
        if (server == null) {
            player.sendMessage(Message.raw("§cServer not found: " + serverId);
            return true;
        }

        hubManager.setHub(serverId, priority);
        player.sendMessage(Message.raw(String.format("§aSet §e%s §aas hub with priority §e%d",
            server.getServerName(), priority));

        return true;
    }

    /**
     * Remove hub designation from a server
     */
    private boolean unsetHub(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Message.raw("§cUsage: /network unsethub <server>"));
            return true;
        }

        String serverId = args[1];

        ServerRecord server = registryManager.getServerById(serverId);
        if (server == null) {
            player.sendMessage(Message.raw("§cServer not found: " + serverId);
            return true;
        }

        hubManager.unsetHub(serverId);
        player.sendMessage(Message.raw("§aRemoved hub designation from §e" + server.getServerName());

        return true;
    }

    /**
     * List all hub servers
     */
    private boolean listHubs(Player player) {
        List<ServerRecord> hubs = registryManager.getHubServers();

        player.sendMessage(Message.raw("§8§m-------------------------"));
        player.sendMessage(Message.raw("§6§lHub Servers §7(" + hubs.size() + ")");
        player.sendMessage(Message.raw("§8§m-------------------------"));

        if (hubs.isEmpty()) {
            player.sendMessage(Message.raw("§7No hubs configured"));
            return true;
        }

        for (ServerRecord hub : hubs) {
            String statusColor = getStatusColor(hub.getStatus());
            player.sendMessage(Message.raw(String.format(
                "%s● §e%s §7(Priority: §f%d§7) - §f%d/%d §7players",
                statusColor,
                hub.getServerName(),
                hub.getHubPriority(),
                hub.getCurrentPlayers(),
                hub.getMaxPlayers()
            ));
        }

        player.sendMessage(Message.raw("§8§m-------------------------"));
        return true;
    }

    /**
     * Transfer a specific player to a server
     */
    private boolean transferPlayer(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Message.raw("§cUsage: /network transfer <player> <server>"));
            return true;
        }

        String playerName = args[1];
        String serverId = args[2];

        // Find target player
        // TODO: Get player from server when API is available
        // Player target = server.getPlayer(playerName);
        // if (target == null) {
        //     player.sendMessage(Message.raw("§cPlayer not found: " + playerName);
        //     return true;
        // }

        // Get destination server
        ServerRecord destination = registryManager.getServerById(serverId);
        if (destination == null) {
            player.sendMessage(Message.raw("§cServer not found: " + serverId);
            return true;
        }

        if (destination.getStatus() != ServerRecord.ServerStatus.ONLINE) {
            player.sendMessage(Message.raw("§cServer " + destination.getServerName() + " is offline");
            return true;
        }

        // TODO: Transfer player when API is available
        player.sendMessage(Message.raw(String.format("§aTransferring §e%s §ato §e%s§a...",
            playerName, destination.getServerName()));

        return true;
    }

    /**
     * Transfer all players on current server to another server
     */
    private boolean transferAllPlayers(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Message.raw("§cUsage: /network transferall <server>"));
            return true;
        }

        String serverId = args[1];

        ServerRecord destination = registryManager.getServerById(serverId);
        if (destination == null) {
            player.sendMessage(Message.raw("§cServer not found: " + serverId);
            return true;
        }

        if (destination.getStatus() != ServerRecord.ServerStatus.ONLINE) {
            player.sendMessage(Message.raw("§cServer " + destination.getServerName() + " is offline");
            return true;
        }

        // TODO: Get online players and transfer them
        player.sendMessage(Message.raw(String.format("§aTransferring all players to §e%s§a...",
            destination.getServerName()));

        return true;
    }

    /**
     * Manually register a server
     */
    private boolean registerServer(Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage(Message.raw("§cUsage: /network register <id> <host> <port> <name>"));
            return true;
        }

        String serverId = args[1];
        String host = args[2];
        int port = parseInt(args[3], 25565);
        String name = String.join(" ", java.util.Arrays.copyOfRange(args, 4, args.length));

        // TODO: Implement manual server registration
        player.sendMessage(Message.raw("§aServer registration coming soon..."));

        return true;
    }

    /**
     * Unregister a server
     */
    private boolean unregisterServer(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Message.raw("§cUsage: /network unregister <server>"));
            return true;
        }

        String serverId = args[1];

        // TODO: Implement server unregistration
        player.sendMessage(Message.raw("§aServer unregistration coming soon..."));

        return true;
    }

    /**
     * Reload configuration
     */
    private boolean reloadConfig(Player player) {
        // TODO: Implement config reload
        player.sendMessage(Message.raw("§aConfiguration reload coming soon..."));
        return true;
    }

    /**
     * Get color based on server status
     */
    private String getStatusColor(ServerRecord.ServerStatus status) {
        switch (status) {
            case ONLINE: return "§a";
            case DEGRADED: return "§e";
            case OFFLINE: return "§c";
            default: return "§7";
        }
    }

    /**
     * Parse integer with default value
     */
    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
