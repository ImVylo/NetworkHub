package com.hytale.networkhub.plugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hytale.networkhub.config.DatabaseConfig;
import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.config.RedisConfig;
import com.hytale.networkhub.database.DatabaseManager;
import com.hytale.networkhub.database.SchemaInitializer;
import com.hytale.networkhub.redis.RedisManager;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.sql.SQLException;

public class NetworkHub extends JavaPlugin {
    public static final PluginManifest MANIFEST = PluginManifest.corePlugin(NetworkHub.class).build();

    private static NetworkHub instance;

    // Configuration
    private NetworkConfig networkConfig;
    private DatabaseConfig databaseConfig;
    private RedisConfig redisConfig;

    // Core managers
    private DatabaseManager databaseManager;
    private RedisManager redisManager;
    private Gson gson;

    // TODO: Add remaining managers in Phase 2
    // private ServerRegistryManager serverRegistryManager;
    // private HeartbeatManager heartbeatManager;
    // private HubManager hubManager;
    // private TransferManager transferManager;
    // etc...

    public NetworkHub(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        getLogger().atInfo().log("=== NetworkHub Plugin Setup ===");

        // Initialize Gson
        gson = new GsonBuilder().setPrettyPrinting().create();

        // Get config directory
        Path configDir = getDataDirectory();

        try {
            // Step 1: Load configurations
            getLogger().atInfo().log("Loading configurations...");
            networkConfig = new NetworkConfig(getLogger(), configDir);
            networkConfig.load();

            databaseConfig = new DatabaseConfig(getLogger(), configDir);
            databaseConfig.load();

            redisConfig = new RedisConfig(getLogger(), configDir);
            redisConfig.load();

            // Step 2: Initialize Database
            getLogger().atInfo().log("Initializing database connection...");
            databaseManager = new DatabaseManager(getLogger(), databaseConfig);
            databaseManager.initialize();

            // Step 3: Initialize database schema
            getLogger().atInfo().log("Initializing database schema...");
            SchemaInitializer schemaInitializer = new SchemaInitializer(getLogger(), databaseManager, databaseConfig);
            schemaInitializer.initialize();

            // Step 4: Initialize Redis
            if (networkConfig.getConfig().redis.enabled) {
                getLogger().atInfo().log("Initializing Redis connection...");
                redisManager = new RedisManager(getLogger(), redisConfig, gson);
                redisManager.initialize();
            } else {
                getLogger().atWarning().log("Redis is disabled - some features will not be available");
            }

            // TODO: Phase 2 - Initialize managers
            // TODO: Phase 3-11 - Initialize remaining components
            // TODO: Phase 9 - Register commands
            // TODO: Phase 3 - Register listeners

            getLogger().atInfo().log("=== NetworkHub Plugin Setup Complete ===");

        } catch (SQLException e) {
            getLogger().atSevere().withCause(e).log("Failed to initialize database");
            throw new RuntimeException("NetworkHub initialization failed", e);
        } catch (Exception e) {
            getLogger().atSevere().withCause(e).log("Failed to setup NetworkHub plugin");
            throw new RuntimeException("NetworkHub initialization failed", e);
        }
    }

    @Override
    protected void start() {
        getLogger().atInfo().log("=== NetworkHub Plugin Starting ===");

        try {
            // TODO: Phase 2 - Register this server with database
            // TODO: Phase 2 - Start scheduled tasks (heartbeat, health check, etc.)
            // TODO: Phase 4 - Load teleporters from database
            // TODO: Phase 8 - Initialize GUI system

            String serverId = networkConfig.getConfig().server.serverId;
            String serverName = networkConfig.getConfig().server.serverName;

            getLogger().atInfo().log("NetworkHub started on server: " + serverName + " (" + serverId + ")");

        } catch (Exception e) {
            getLogger().atSevere().withCause(e).log("Failed to start NetworkHub plugin");
            throw new RuntimeException("NetworkHub start failed", e);
        }
    }

    @Override
    protected void onShutdown() {
        getLogger().atInfo().log("=== NetworkHub Plugin Shutting Down ===");

        try {
            // TODO: Phase 10 - Implement graceful shutdown
            // If fallback enabled, transfer all players to hub
            // Unregister server from database
            // Clean up resources

            // Close Redis connections
            if (redisManager != null) {
                redisManager.close();
            }

            // Close database connections
            if (databaseManager != null) {
                databaseManager.close();
            }

            getLogger().atInfo().log("NetworkHub plugin shut down successfully");

        } catch (Exception e) {
            getLogger().atSevere().withCause(e).log("Error during NetworkHub shutdown");
        }
    }

    // Getters for other classes to access managers
    public static NetworkHub getInstance() {
        return instance;
    }

    public NetworkConfig getNetworkConfig() {
        return networkConfig;
    }

    public DatabaseConfig getDatabaseConfig() {
        return databaseConfig;
    }

    public RedisConfig getRedisConfig() {
        return redisConfig;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public RedisManager getRedisManager() {
        return redisManager;
    }

    public Gson getGson() {
        return gson;
    }

    // TODO: Add getters for remaining managers as they are implemented
}
