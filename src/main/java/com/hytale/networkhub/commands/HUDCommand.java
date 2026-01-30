package com.hytale.networkhub.commands;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.gui.hud.NetworkHUD;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.logging.Logger;

/**
 * Command to control the network HUD display
 */
public class HUDCommand {
    private final Logger logger;
    private final NetworkConfig config;
    private final NetworkHUD networkHUD;

    public HUDCommand(Logger logger, NetworkConfig config, NetworkHUD networkHUD) {
        this.logger = logger;
        this.config = config;
        this.networkHUD = networkHUD;
    }

    /**
     * Execute the HUD command
     */
    public boolean execute(Player player, String[] args) {
        if (!config.getConfig().hud.enabled) {
            player.sendMessage("§cHUD system is disabled");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "toggle":
                return toggleHUD(player);

            case "on":
                return enableHUD(player);

            case "off":
                return disableHUD(player);

            case "reload":
                return reloadHUD(player);

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
        player.sendMessage("§6§lHUD Commands");
        player.sendMessage("§8§m-------------------------");
        player.sendMessage("§e/hud toggle §7- Toggle HUD on/off");
        player.sendMessage("§e/hud on §7- Enable HUD");
        player.sendMessage("§e/hud off §7- Disable HUD");
        player.sendMessage("§e/hud reload §7- Reload HUD settings");
        player.sendMessage("§8§m-------------------------");
    }

    /**
     * Toggle HUD display
     */
    private boolean toggleHUD(Player player) {
        boolean enabled = networkHUD.toggle(player);

        if (enabled) {
            player.sendMessage("§aNetwork HUD enabled");
        } else {
            player.sendMessage("§7Network HUD disabled");
        }

        return true;
    }

    /**
     * Enable HUD display
     */
    private boolean enableHUD(Player player) {
        if (networkHUD.isEnabled(player.getUniqueId())) {
            player.sendMessage("§eHUD is already enabled");
            return true;
        }

        networkHUD.enable(player);
        player.sendMessage("§aNetwork HUD enabled");

        return true;
    }

    /**
     * Disable HUD display
     */
    private boolean disableHUD(Player player) {
        if (!networkHUD.isEnabled(player.getUniqueId())) {
            player.sendMessage("§eHUD is already disabled");
            return true;
        }

        networkHUD.disable(player);
        player.sendMessage("§7Network HUD disabled");

        return true;
    }

    /**
     * Reload HUD settings
     */
    private boolean reloadHUD(Player player) {
        // TODO: Check admin permission
        // if (!player.hasPermission("networkhub.admin")) {
        //     player.sendMessage("§cYou don't have permission to reload HUD settings");
        //     return true;
        // }

        networkHUD.refreshStats();
        player.sendMessage("§aHUD settings reloaded");

        return true;
    }
}
