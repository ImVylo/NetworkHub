package com.hytale.networkhub.commands.base;

import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;

/**
 * Simple wrapper to adapt existing command logic to Hytale's command system
 * Allows gradual migration without rewriting all commands at once
 */
public class SimpleCommandWrapper extends CommandBase {
    private final BiConsumer<CommandSender, String[]> executor;
    private final boolean playerOnly;

    public SimpleCommandWrapper(String name, String description, BiConsumer<CommandSender, String[]> executor, boolean playerOnly) {
        super(name, description);
        this.executor = executor;
        this.playerOnly = playerOnly;
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        CommandSender sender = context.sender();

        if (playerOnly && !(sender instanceof Player)) {
            context.sendMessage(Message.raw("§cThis command can only be used by players"));
            return;
        }

        // Parse arguments from context
        // For now, we'll use empty array - full arg parsing would require more work
        String[] args = new String[0];

        executor.accept(sender, args);
    }
}
