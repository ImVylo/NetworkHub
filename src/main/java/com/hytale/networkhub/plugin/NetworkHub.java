package com.hytale.networkhub.plugin;

import com.google.gson.Gson;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.config.DatabaseConfig;
import com.hytale.networkhub.config.RedisConfig;
import com.hytale.networkhub.database.DatabaseManager;
import com.hytale.networkhub.database.SchemaInitializer;
import com.hytale.networkhub.managers.*;
import com.hytale.networkhub.redis.RedisManager;
import com.hytale.networkhub.tasks.*;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * NetworkHub - Multi-server coordination plugin for Hytale
 *
 * Features:
 * - Server registry and health monitoring
 * - Cross-server player tracking
 * - Physical teleporter blocks
 * - Priority-based queue system
 * - Cross-server messaging and chat
 * - Network-wide announcements
 * - Interactive GUIs and persistent HUD
 * - Cross-server moderation
 */
public class NetworkHub extends JavaPlugin {

    private static NetworkHub instance;

    // Configuration
    private NetworkConfig config;
    private Gson gson;

    // Database
    private DatabaseManager databaseManager;

    // Redis
    private RedisManager redisManager;

    // Managers
    private ServerRegistryManager serverRegistryManager;
    private HeartbeatManager heartbeatManager;
    private HubManager hubManager;
    private PlayerTrackingManager playerTrackingManager;
    private TransferManager transferManager;
    private TeleporterManager teleporterManager;
    private QueueManager queueManager;
    private MessagingManager messagingManager;
    private ChatManager chatManager;
    private AnnouncementManager announcementManager;
    private ModerationManager moderationManager;

    // Scheduler
    private ScheduledExecutorService scheduler;

    public NetworkHub(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        getLogger().at(Level.INFO).log("NetworkHub setting up...");

        try {
            // Initialize Gson
            gson = new Gson();

            // Load configurations
            Path configPath = getFile().getParent().resolve("config");
            config = new NetworkConfig(getLogger(), configPath);
            config.load();
            getLogger().at(Level.INFO).log("Configuration loaded for server: %s", config.getConfig().server.serverId);

            // Load database config
            DatabaseConfig dbConfig = new DatabaseConfig(getLogger(), configPath);
            dbConfig.load();

            // Initialize database
            databaseManager = new DatabaseManager(getLogger(), dbConfig);
            databaseManager.initialize();
            getLogger().at(Level.INFO).log("Database connection established");

            // Initialize schema
            SchemaInitializer schemaInitializer = new SchemaInitializer(getLogger(), databaseManager, dbConfig);
            schemaInitializer.initialize();
            getLogger().at(Level.INFO).log("Database schema initialized");

            // Initialize Redis (if enabled)
            if (config.getConfig().redis.enabled) {
                RedisConfig redisConfig = new RedisConfig(getLogger(), configPath);
                redisConfig.load();
                redisManager = new RedisManager(getLogger(), redisConfig, gson);
                redisManager.initialize();
                getLogger().at(Level.INFO).log("Redis connection established");
            } else {
                getLogger().at(Level.WARNING).log("Redis is disabled - cross-server features will not work");
            }

            // Initialize managers
            serverRegistryManager = new ServerRegistryManager(getLogger(), databaseManager, config);
            heartbeatManager = new HeartbeatManager(getLogger(), config, databaseManager);
            hubManager = new HubManager(getLogger(), databaseManager, serverRegistryManager);
            playerTrackingManager = new PlayerTrackingManager(getLogger(), databaseManager, config);
            transferManager = new TransferManager(getLogger(), databaseManager, config);
            teleporterManager = new TeleporterManager(getLogger(), databaseManager, config);
            queueManager = new QueueManager(getLogger(), databaseManager, config, serverRegistryManager, transferManager, playerTrackingManager);
            messagingManager = new MessagingManager(getLogger(), databaseManager, redisManager, playerTrackingManager, config, gson);
            chatManager = new ChatManager(getLogger(), databaseManager, redisManager, config, gson);
            announcementManager = new AnnouncementManager(getLogger(), databaseManager, redisManager, config, gson);
            moderationManager = new ModerationManager(getLogger(), config, databaseManager, redisManager, gson);

            getLogger().at(Level.INFO).log("Managers initialized");

            // Register server in database
            serverRegistryManager.registerServer();
            getLogger().at(Level.INFO).log("Server registered in network");

            getLogger().at(Level.INFO).log("NetworkHub setup complete");

        } catch (Exception e) {
            getLogger().at(Level.SEVERE).withCause(e).log("Failed to setup NetworkHub");
            throw new RuntimeException("NetworkHub setup failed", e);
        }
    }

    @Override
    protected void start() {
        super.start();
        getLogger().at(Level.INFO).log("NetworkHub starting...");

        try {
            // Initialize scheduler
            scheduler = Executors.newScheduledThreadPool(4, r -> {
                Thread t = new Thread(r, "NetworkHub-Worker");
                t.setDaemon(true);
                return t;
            });

            // Load teleporters for this server
            teleporterManager.loadTeleporters();
            getLogger().at(Level.INFO).log("Loaded teleporters");

            // Schedule heartbeat task (every 10 seconds)
            scheduler.scheduleAtFixedRate(
                new HeartbeatTask(getLogger(), config, heartbeatManager),
                0,
                config.getConfig().heartbeat.intervalSeconds,
                TimeUnit.SECONDS
            );

            // Schedule health check task (every 15 seconds)
            scheduler.scheduleAtFixedRate(
                new HealthCheckTask(getLogger(), config, databaseManager),
                15,
                15,
                TimeUnit.SECONDS
            );

            // Schedule player location update task (every 30 seconds)
            scheduler.scheduleAtFixedRate(
                new PlayerLocationUpdateTask(getLogger(), config, playerTrackingManager),
                30,
                30,
                TimeUnit.SECONDS
            );

            // Schedule queue processing task (every 2 seconds)
            scheduler.scheduleAtFixedRate(
                new QueueProcessTask(getLogger(), queueManager),
                2,
                2,
                TimeUnit.SECONDS
            );

            // Schedule cleanup task (every 5 minutes)
            scheduler.scheduleAtFixedRate(
                new CleanupTask(getLogger(), config, databaseManager),
                5,
                5,
                TimeUnit.MINUTES
            );

            getLogger().at(Level.INFO).log("Scheduled tasks started");

            // TODO: Register commands
            // TODO: Register event listeners
            // TODO: Subscribe to Redis channels

            getLogger().at(Level.INFO).log("NetworkHub started successfully!");

        } catch (Exception e) {
            getLogger().at(Level.SEVERE).withCause(e).log("Failed to start NetworkHub");
            throw new RuntimeException("NetworkHub start failed", e);
        }
    }

    @Override
    protected void shutdown() {
        getLogger().at(Level.INFO).log("NetworkHub shutting down...");

        try {
            // Stop scheduled tasks
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown();
                try {
                    if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                        scheduler.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    scheduler.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }

            // Unregister server
            if (serverRegistryManager != null) {
                serverRegistryManager.unregisterServer(config.getConfig().server.serverId);
                getLogger().at(Level.INFO).log("Server unregistered from network");
            }

            // Close Redis connection
            if (redisManager != null) {
                redisManager.close();
                getLogger().at(Level.INFO).log("Redis connection closed");
            }

            // Close database connection
            if (databaseManager != null) {
                databaseManager.close();
                getLogger().at(Level.INFO).log("Database connection closed");
            }

            getLogger().at(Level.INFO).log("NetworkHub shutdown complete");

        } catch (Exception e) {
            getLogger().at(Level.SEVERE).withCause(e).log("Error during NetworkHub shutdown");
        }
    }

    // === Getters ===

    public static NetworkHub getInstance() {
        return instance;
    }

    public NetworkConfig getConfig() {
        return config;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public RedisManager getRedisManager() {
        return redisManager;
    }

    public ServerRegistryManager getServerRegistryManager() {
        return serverRegistryManager;
    }

    public HeartbeatManager getHeartbeatManager() {
        return heartbeatManager;
    }

    public HubManager getHubManager() {
        return hubManager;
    }

    public PlayerTrackingManager getPlayerTrackingManager() {
        return playerTrackingManager;
    }

    public TransferManager getTransferManager() {
        return transferManager;
    }

    public TeleporterManager getTeleporterManager() {
        return teleporterManager;
    }

    public QueueManager getQueueManager() {
        return queueManager;
    }

    public MessagingManager getMessagingManager() {
        return messagingManager;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public AnnouncementManager getAnnouncementManager() {
        return announcementManager;
    }

    public ModerationManager getModerationManager() {
        return moderationManager;
    }

    public Gson getGson() {
        return gson;
    }
}
