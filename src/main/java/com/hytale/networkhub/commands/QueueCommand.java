package com.hytale.networkhub.commands;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.database.models.ServerRecord;
import com.hytale.networkhub.managers.QueueManager;
import com.hytale.networkhub.managers.ServerRegistryManager;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.logging.Logger;

/**
 * Command to manage server queues
 */
public class QueueCommand {
    private final Logger logger;
    private final NetworkConfig config;
    private final QueueManager queueManager;
    private final ServerRegistryManager registryManager;

    public QueueCommand(Logger logger, NetworkConfig config, QueueManager queueManager,
                       ServerRegistryManager registryManager) {
        this.logger = logger;
        this.config = config;
        this.queueManager = queueManager;
        this.registryManager = registryManager;
    }

    /**
     * Execute the queue command
     */
    public boolean execute(Player player, String[] args) {
        if (!config.getConfig().queue.enabled) {
            player.sendMessage("§cQueue system is disabled");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "join":
                return joinQueue(player, args);

            case "leave":
                return leaveQueue(player, args);

            case "info":
                return queueInfo(player, args);

            case "list":
                return listQueues(player);

            default:
                player.sendMessage("§cUnknown subcommand: " + subcommand);
                sendHelp(player);
                return true;
        }
    }

    /**
     * Send help message
     */
    private void sendHelp(Player player) {
        player.sendMessage("§8§m-------------------------");
        player.sendMessage("§6§lQueue Commands");
        player.sendMessage("§8§m-------------------------");
        player.sendMessage("§e/queue join <server> §7- Join queue");
        player.sendMessage("§e/queue leave <server> §7- Leave queue");
        player.sendMessage("§e/queue info <server> §7- Show queue info");
        player.sendMessage("§e/queue list §7- List your queues");
        player.sendMessage("§8§m-------------------------");
    }

    /**
     * Join a server queue
     */
    private boolean joinQueue(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /queue join <server>");
            return true;
        }

        String serverId = args[1];

        ServerRecord server = registryManager.getServerById(serverId);
        if (server == null) {
            player.sendMessage("§cServer not found: " + serverId);
            return true;
        }

        // Check for VIP permission
        int priority = 0; // Default priority
        // TODO: Implement when permission system is available
        // if (player.hasPermission("networkhub.queue.vip")) {
        //     priority = config.getConfig().queue.vipPriority;
        // }

        queueManager.joinQueue(player.getUniqueId(), serverId, priority);

        return true;
    }

    /**
     * Leave a server queue
     */
    private boolean leaveQueue(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /queue leave <server>");
            return true;
        }

        String serverId = args[1];

        ServerRecord server = registryManager.getServerById(serverId);
        if (server == null) {
            player.sendMessage("§cServer not found: " + serverId);
            return true;
        }

        queueManager.leaveQueue(player.getUniqueId(), serverId);

        return true;
    }

    /**
     * Show queue information for a server
     */
    private boolean queueInfo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /queue info <server>");
            return true;
        }

        String serverId = args[1];

        ServerRecord server = registryManager.getServerById(serverId);
        if (server == null) {
            player.sendMessage("§cServer not found: " + serverId);
            return true;
        }

        int queueSize = queueManager.getQueueSize(serverId);
        int position = queueManager.getQueuePosition(player.getUniqueId(), serverId);

        player.sendMessage("§8§m-------------------------");
        player.sendMessage("§6§lQueue Info: §e" + server.getServerName());
        player.sendMessage("§8§m-------------------------");
        player.sendMessage("§7Total in queue: §f" + queueSize);

        if (position > 0) {
            player.sendMessage("§7Your position: §e#" + position);
            int estimated = position * config.getConfig().queue.processIntervalSeconds;
            player.sendMessage("§7Estimated wait: §f~" + estimated + "s");
        } else {
            player.sendMessage("§7You are not in this queue");
        }

        player.sendMessage("§8§m-------------------------");

        return true;
    }

    /**
     * List all queues the player is in
     */
    private boolean listQueues(Player player) {
        // TODO: Implement when QueueManager has getAllPlayerQueues method
        player.sendMessage("§7Queue list coming soon...");
        return true;
    }
}
