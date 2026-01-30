package com.hytale.networkhub.commands;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.database.models.ServerRecord;
import com.hytale.networkhub.database.models.TeleporterData;
import com.hytale.networkhub.managers.ServerRegistryManager;
import com.hytale.networkhub.managers.TeleporterManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;

import java.util.List;
import com.hypixel.hytale.logger.HytaleLogger;
import java.util.logging.Level;

/**
 * Command to manage teleporter blocks
 */
public class TeleporterCommand {
    private final HytaleLogger logger;
    private final NetworkConfig config;
    private final TeleporterManager teleporterManager;
    private final ServerRegistryManager registryManager;

    public TeleporterCommand(HytaleLogger logger, NetworkConfig config,
                            TeleporterManager teleporterManager,
                            ServerRegistryManager registryManager) {
        this.logger = logger;
        this.config = config;
        this.teleporterManager = teleporterManager;
        this.registryManager = registryManager;
    }

    /**
     * Execute the teleporter command
     */
    public boolean execute(Player player, String[] args) {
        // TODO: Check permission
        // if (!player.hasPermission("networkhub.teleporter")) {
        //     player.sendMessage(Message.raw("§cYou don't have permission to manage teleporters"));
        //     return true;
        // }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "create":
                return createTeleporter(player, args);

            case "remove":
                return removeTeleporter(player);

            case "list":
                return listTeleporters(player);

            default:
                player.sendMessage(Message.raw("§cUnknown subcommand: " + subcommand));
                sendHelp(player);
                return true;
        }
    }

    /**
     * Send help message
     */
    private void sendHelp(Player player) {
        player.sendMessage(Message.raw("§8§m-------------------------"));
        player.sendMessage(Message.raw("§6§lTeleporter Commands"));
        player.sendMessage(Message.raw("§8§m-------------------------"));
        player.sendMessage(Message.raw("§e/teleporter create <server> [name] §7- Create teleporter"));
        player.sendMessage(Message.raw("§e/teleporter remove §7- Remove teleporter at location"));
        player.sendMessage(Message.raw("§e/teleporter list §7- List all teleporters"));
        player.sendMessage(Message.raw("§8§m-------------------------"));
    }

    /**
     * Create a teleporter at the player's location
     */
    private boolean createTeleporter(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Message.raw("§cUsage: /teleporter create <server> [displayName]"));
            return true;
        }

        String destinationServerId = args[1];
        String displayName = args.length >= 3
            ? String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length))
            : "Teleporter to " + destinationServerId;

        // Validate destination server
        ServerRecord destination = registryManager.getServerById(destinationServerId);
        if (destination == null) {
            player.sendMessage(Message.raw("§cServer not found: " + destinationServerId));
            return true;
        }

        // TODO: Get player's current location when API is available
        // Location loc = player.getLocation();
        // String worldName = loc.getWorld().getName();
        // int x = loc.getBlockX();
        // int y = loc.getBlockY();
        // int z = loc.getBlockZ();

        // For now, use placeholder
        String worldName = "world";
        int x = 0, y = 0, z = 0;

        teleporterManager.createTeleporter(
            worldName,
            x, y, z,
            destinationServerId,
            displayName,
            null, // permission (null = everyone)
            config.getConfig().teleporter.cooldownSeconds
        );

        player.sendMessage(Message.raw(String.format("§aCreated teleporter §e%s §a→ §e%s",
            displayName, destination.getServerName())));

        return true;
    }

    /**
     * Remove a teleporter at the player's location
     */
    private boolean removeTeleporter(Player player) {
        // TODO: Get player's current location when API is available
        // Location loc = player.getLocation();
        // String worldName = loc.getWorld().getName();
        // int x = loc.getBlockX();
        // int y = loc.getBlockY();
        // int z = loc.getBlockZ();

        // For now, send message
        player.sendMessage(Message.raw("§cTeleporter removal coming soon..."));

        return true;
    }

    /**
     * List all teleporters on the current server
     */
    private boolean listTeleporters(Player player) {
        List<TeleporterData> teleporters = teleporterManager.getTeleportersByServer(
            config.getConfig().server.serverId
        );

        player.sendMessage(Message.raw("§8§m-------------------------"));
        player.sendMessage(Message.raw("§6§lTeleporters §7(" + teleporters.size() + ")"));
        player.sendMessage(Message.raw("§8§m-------------------------"));

        if (teleporters.isEmpty()) {
            player.sendMessage(Message.raw("§7No teleporters on this server"));
            return true;
        }

        for (TeleporterData tp : teleporters) {
            String status = tp.isEnabled() ? "§a✓" : "§c✗";
            player.sendMessage(Message.raw(String.format("%s §e%s §7→ §a%s §7(%d, %d, %d)",
                status,
                tp.getDisplayName(),
                tp.getDestinationServerId(),
                tp.getX(), tp.getY(), tp.getZ()
            )));
        }

        player.sendMessage(Message.raw("§8§m-------------------------"));

        return true;
    }
}
