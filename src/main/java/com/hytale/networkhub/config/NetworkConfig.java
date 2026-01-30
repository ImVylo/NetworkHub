package com.hytale.networkhub.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import com.hypixel.hytale.logger.HytaleLogger;
import java.util.logging.Level;

public class NetworkConfig {
    private final HytaleLogger logger;
    private final Path configFile;
    private final Gson gson;
    private Config config;

    public NetworkConfig(HytaleLogger logger, Path configDir) {
        this.logger = logger;
        this.configFile = configDir.resolve("network-config.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.config = new Config();
    }

    public void load() {
        try {
            if (!Files.exists(configFile)) {
                logger.at(Level.INFO).log("Network config not found, creating default");
                save();
                return;
            }

            try (BufferedReader reader = Files.newBufferedReader(configFile)) {
                Config loaded = gson.fromJson(reader, Config.class);
                if (loaded != null) {
                    this.config = loaded;
                    logger.at(Level.INFO).log("Loaded network config");
                } else {
                    logger.at(Level.WARNING).log("Network config was empty, using defaults");
                    save();
                }
            }
        } catch (IOException e) {
            logger.at(Level.SEVERE).log("Failed to load network config: " + e.getMessage());
        }
    }

    public void save() {
        try {
            Files.createDirectories(configFile.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(configFile)) {
                gson.toJson(config, writer);
                logger.at(Level.INFO).log("Saved network config");
            }
        } catch (IOException e) {
            logger.at(Level.SEVERE).log("Failed to save network config: " + e.getMessage());
        }
    }

    public Config getConfig() {
        return config;
    }

    public static class Config {
        public ServerConfig server = new ServerConfig();
        public HeartbeatConfig heartbeat = new HeartbeatConfig();
        public FallbackConfig fallback = new FallbackConfig();
        public TeleporterConfig teleporter = new TeleporterConfig();
        public QueueConfig queue = new QueueConfig();
        public MessagingConfig messaging = new MessagingConfig();
        public RedisConfigSettings redis = new RedisConfigSettings();
        public GUIConfig gui = new GUIConfig();
        public AnnouncementConfig announcements = new AnnouncementConfig();
        public HUDConfig hud = new HUDConfig();
        public ModerationConfig moderation = new ModerationConfig();
    }

    public static class ServerConfig {
        public String serverId = "game-01";
        public String serverName = "Game Server 1";
        public String serverType = "GAME";
        public boolean isHub = false;
        public int hubPriority = 0;
        public int maxPlayers = 100;
    }

    public static class HeartbeatConfig {
        public int intervalSeconds = 10;
        public int timeoutSeconds = 30;
        public int failureThreshold = 3;
    }

    public static class FallbackConfig {
        public boolean enabled = true;
        public boolean triggerOnShutdown = true;
        public int transferDelaySeconds = 1;
    }

    public static class TeleporterConfig {
        public int confirmationTimeoutSeconds = 10;
        public int cooldownSeconds = 5;
    }

    public static class QueueConfig {
        public boolean enabled = true;
        public int maxQueueSize = 100;
        public int processIntervalSeconds = 2;
        public boolean autoJoinOnFull = true;
        public boolean notifyPosition = true;
        public int vipPriority = 100;
        public int defaultPriority = 0;
    }

    public static class MessagingConfig {
        public boolean enabled = true;
        public boolean globalChatEnabled = true;
        public boolean staffChatEnabled = true;
        public boolean directMessagingEnabled = true;
        public boolean persistChatHistory = false;
        public int chatHistoryDays = 7;
    }

    public static class RedisConfigSettings {
        public boolean enabled = true;
        public int reconnectAttempts = 5;
        public int reconnectDelaySeconds = 5;
    }

    public static class GUIConfig {
        public boolean enabled = true;
        public String serverSelectorCommand = "servers";
        public String adminPanelCommand = "networkgui";
        public boolean allowPlayerServerSelection = true;
        public boolean showPlayerCounts = true;
        public boolean showServerStatus = true;
        public int refreshIntervalSeconds = 5;
    }

    public static class AnnouncementConfig {
        public boolean enabled = true;
        public int maxDurationSeconds = 300;
        public int defaultDurationSeconds = 10;
        public boolean allowSounds = true;
        public boolean requirePermission = true;
        public boolean logToDatabase = true;
    }

    public static class HUDConfig {
        public boolean enabled = true;
        public boolean hubOnly = true;
        public int updateIntervalSeconds = 2;
        public List<String> displayLines = List.of(
            "network_name", "separator", "total_players", "server_list",
            "separator", "server_health", "queue_info", "blank", "your_server"
        );
        public boolean showServerHealth = true;
        public boolean showPlayerDistribution = true;
        public boolean showQueueCounts = true;
        public boolean showNotifications = true;
        public boolean compactMode = false;
    }

    public static class ModerationConfig {
        public boolean enabled = true;
        public boolean syncBans = true;
        public boolean syncKicks = true;
        public boolean syncMutes = true;
        public int banDurationHours = 24;
        public boolean logToDatabase = true;
    }
}
