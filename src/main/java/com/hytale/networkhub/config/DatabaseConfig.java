package com.hytale.networkhub.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import com.hypixel.hytale.logger.HytaleLogger;
import java.util.logging.Level;

public class DatabaseConfig {
    private final HytaleLogger logger;
    private final Path configFile;
    private final Gson gson;
    private Config config;

    public DatabaseConfig(HytaleLogger logger, Path configDir) {
        this.logger = logger;
        this.configFile = configDir.resolve("database-config.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.config = new Config();
    }

    public void load() {
        try {
            if (!Files.exists(configFile)) {
                logger.at(Level.INFO).log("Database config not found, creating default");
                save();
                return;
            }

            try (BufferedReader reader = Files.newBufferedReader(configFile)) {
                Config loaded = gson.fromJson(reader, Config.class);
                if (loaded != null) {
                    this.config = loaded;
                    logger.at(Level.INFO).log("Loaded database config");
                } else {
                    logger.at(Level.WARNING).log("Database config was empty, using defaults");
                    save();
                }
            }
        } catch (IOException e) {
            logger.at(Level.SEVERE).log("Failed to load database config: " + e.getMessage());
        }
    }

    public void save() {
        try {
            Files.createDirectories(configFile.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(configFile)) {
                gson.toJson(config, writer);
                logger.at(Level.INFO).log("Saved database config");
            }
        } catch (IOException e) {
            logger.at(Level.SEVERE).log("Failed to save database config: " + e.getMessage());
        }
    }

    public Config getConfig() {
        return config;
    }

    public String getJdbcUrl() {
        String type = config.type.toLowerCase();
        switch (type) {
            case "postgresql":
            case "postgres":
                return String.format("jdbc:postgresql://%s:%d/%s",
                    config.host, config.port, config.database);
            case "mysql":
                return String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true",
                    config.host, config.port, config.database);
            case "mariadb":
                return String.format("jdbc:mariadb://%s:%d/%s",
                    config.host, config.port, config.database);
            default:
                throw new IllegalArgumentException("Unsupported database type: " + config.type);
        }
    }

    public static class Config {
        public String type = "POSTGRESQL";  // POSTGRESQL, MYSQL, MARIADB
        public String host = "localhost";
        public int port = 5432;
        public String database = "hytale_network";
        public String username = "hytale_user";
        public String password = "CHANGE_ME";
        public ConnectionPoolConfig connectionPool = new ConnectionPoolConfig();
    }

    public static class ConnectionPoolConfig {
        public int minimumIdle = 5;
        public int maximumPoolSize = 20;
        public int connectionTimeout = 30000;
        public int idleTimeout = 600000;
        public int maxLifetime = 1800000;
    }
}
