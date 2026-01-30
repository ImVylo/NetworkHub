package com.hytale.networkhub.commands;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.gui.hud.NetworkHUD;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;

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
            player.sendMessage(Message.raw("§cHUD system is disabled"));
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
        player.sendMessage(Message.raw("§6§lHUD Commands"));
        player.sendMessage(Message.raw("§8§m-------------------------"));
        player.sendMessage(Message.raw("§e/hud toggle §7- Toggle HUD on/off"));
        player.sendMessage(Message.raw("§e/hud on §7- Enable HUD"));
        player.sendMessage(Message.raw("§e/hud off §7- Disable HUD"));
        player.sendMessage(Message.raw("§e/hud reload §7- Reload HUD settings"));
        player.sendMessage(Message.raw("§8§m-------------------------"));
    }

    /**
     * Toggle HUD display
     */
    private boolean toggleHUD(Player player) {
        boolean enabled = networkHUD.toggle(player);

        if (enabled) {
            player.sendMessage(Message.raw("§aNetwork HUD enabled"));
        } else {
            player.sendMessage(Message.raw("§7Network HUD disabled"));
        }

        return true;
    }

    /**
     * Enable HUD display
     */
    private boolean enableHUD(Player player) {
        if (networkHUD.isEnabled(player.getPlayerRef().getUuid())) {
            player.sendMessage(Message.raw("§eHUD is already enabled"));
            return true;
        }

        networkHUD.enable(player);
        player.sendMessage(Message.raw("§aNetwork HUD enabled"));

        return true;
    }

    /**
     * Disable HUD display
     */
    private boolean disableHUD(Player player) {
        if (!networkHUD.isEnabled(player.getPlayerRef().getUuid())) {
            player.sendMessage(Message.raw("§eHUD is already disabled"));
            return true;
        }

        networkHUD.disable(player);
        player.sendMessage(Message.raw("§7Network HUD disabled"));

        return true;
    }

    /**
     * Reload HUD settings
     */
    private boolean reloadHUD(Player player) {
        // TODO: Check admin permission
        // if (!player.hasPermission("networkhub.admin")) {
        //     player.sendMessage(Message.raw("§cYou don't have permission to reload HUD settings"));
        //     return true;
        // }

        networkHUD.refreshStats();
        player.sendMessage(Message.raw("§aHUD settings reloaded"));

        return true;
    }
}
