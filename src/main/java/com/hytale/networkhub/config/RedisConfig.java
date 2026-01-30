package com.hytale.networkhub.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import com.hypixel.hytale.logger.HytaleLogger;
import java.util.logging.Level;

public class RedisConfig {
    private final HytaleLogger logger;
    private final Path configFile;
    private final Gson gson;
    private Config config;

    public RedisConfig(HytaleLogger logger, Path configDir) {
        this.logger = logger;
        this.configFile = configDir.resolve("redis-config.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.config = new Config();
    }

    public void load() {
        try {
            if (!Files.exists(configFile)) {
                logger.at(Level.INFO).log("Redis config not found, creating default");
                save();
                return;
            }

            try (BufferedReader reader = Files.newBufferedReader(configFile)) {
                Config loaded = gson.fromJson(reader, Config.class);
                if (loaded != null) {
                    this.config = loaded;
                    logger.at(Level.INFO).log("Loaded Redis config");
                } else {
                    logger.at(Level.WARNING).log("Redis config was empty, using defaults");
                    save();
                }
            }
        } catch (IOException e) {
            logger.at(Level.SEVERE).log("Failed to load Redis config: " + e.getMessage());
        }
    }

    public void save() {
        try {
            Files.createDirectories(configFile.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(configFile)) {
                gson.toJson(config, writer);
                logger.at(Level.INFO).log("Saved Redis config");
            }
        } catch (IOException e) {
            logger.at(Level.SEVERE).log("Failed to save Redis config: " + e.getMessage());
        }
    }

    public Config getConfig() {
        return config;
    }

    public static class Config {
        public boolean enabled = true;
        public String host = "localhost";
        public int port = 6379;
        public String password = null;
        public int database = 0;
        public PoolConfig pool = new PoolConfig();
        public Map<String, String> channels = Map.of(
            "playerJoin", "hytale:player:join",
            "playerQuit", "hytale:player:quit",
            "chatMessage", "hytale:chat:global",
            "staffChat", "hytale:chat:staff",
            "directMessage", "hytale:chat:dm",
            "serverStatus", "hytale:server:status",
            "transferRequest", "hytale:transfer:request",
            "announcement", "hytale:announcement",
            "moderation", "hytale:moderation"
        );
    }

    public static class PoolConfig {
        public int maxTotal = 20;
        public int maxIdle = 10;
        public int minIdle = 5;
        public boolean testOnBorrow = true;
        public boolean testOnReturn = false;
        public boolean testWhileIdle = true;
    }
}
