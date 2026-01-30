package com.hytale.networkhub.managers;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.database.DatabaseManager;
import com.hytale.networkhub.database.models.TeleporterData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class TeleporterManager {
    private final Logger logger;
    private final DatabaseManager dbManager;
    private final NetworkConfig config;
    private final String serverId;
    private final Map<String, TeleporterData> teleporterCache = new ConcurrentHashMap<>();
    private final Map<UUID, Map<Integer, Long>> playerCooldowns = new ConcurrentHashMap<>();

    public TeleporterManager(Logger logger, DatabaseManager dbManager, NetworkConfig config) {
        this.logger = logger;
        this.dbManager = dbManager;
        this.config = config;
        this.serverId = config.getConfig().server.serverId;
    }

    public void loadTeleporters() {
        String sql = "SELECT * FROM teleporters WHERE server_id = ? AND enabled = true";

        List<TeleporterData> teleporters = dbManager.executeQuery(sql, rs -> {
            List<TeleporterData> list = new ArrayList<>();
            while (rs.next()) {
                TeleporterData tp = new TeleporterData();
                tp.setTeleporterId(rs.getInt("teleporter_id"));
                tp.setServerId(rs.getString("server_id"));
                tp.setWorldName(rs.getString("world_name"));
                tp.setX(rs.getInt("x"));
                tp.setY(rs.getInt("y"));
                tp.setZ(rs.getInt("z"));
                tp.setDestinationServerId(rs.getString("destination_server_id"));
                tp.setDestinationWorld(rs.getString("destination_world"));
                tp.setDestinationX(rs.getDouble("destination_x"));
                tp.setDestinationY(rs.getDouble("destination_y"));
                tp.setDestinationZ(rs.getDouble("destination_z"));
                tp.setDisplayName(rs.getString("display_name"));
                tp.setPermission(rs.getString("permission"));
                tp.setCooldownSeconds(rs.getInt("cooldown_seconds"));
                tp.setEnabled(rs.getBoolean("enabled"));
                list.add(tp);
            }
            return list;
        }, serverId);

        if (teleporters != null) {
            teleporterCache.clear();
            teleporters.forEach(tp -> {
                String key = makeKey(tp.getWorldName(), tp.getX(), tp.getY(), tp.getZ());
                teleporterCache.put(key, tp);
            });
            logger.info("Loaded " + teleporters.size() + " teleporters for server " + serverId);
        }
    }

    public void createTeleporter(String worldName, int x, int y, int z, String destinationServerId,
                                String displayName, String permission, int cooldownSeconds) {
        String sql = """
            INSERT INTO teleporters (server_id, world_name, x, y, z, destination_server_id,
                                    display_name, permission, cooldown_seconds, enabled, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """;

        int rows = dbManager.executeUpdate(sql, serverId, worldName, x, y, z, destinationServerId,
            displayName, permission, cooldownSeconds);

        if (rows > 0) {
            logger.info("Created teleporter at " + worldName + " (" + x + "," + y + "," + z + ")");
            loadTeleporters(); // Refresh cache
        }
    }

    public void removeTeleporter(String worldName, int x, int y, int z) {
        String sql = "DELETE FROM teleporters WHERE server_id = ? AND world_name = ? AND x = ? AND y = ? AND z = ?";
        int rows = dbManager.executeUpdate(sql, serverId, worldName, x, y, z);

        if (rows > 0) {
            logger.info("Removed teleporter at " + worldName + " (" + x + "," + y + "," + z + ")");
            String key = makeKey(worldName, x, y, z);
            teleporterCache.remove(key);
        }
    }

    public TeleporterData getTeleporter(String worldName, int x, int y, int z) {
        String key = makeKey(worldName, x, y, z);
        return teleporterCache.get(key);
    }

    public List<TeleporterData> getAllTeleporters() {
        return new ArrayList<>(teleporterCache.values());
    }

    public boolean hasCooldown(UUID playerUuid, int teleporterId) {
        Map<Integer, Long> cooldowns = playerCooldowns.get(playerUuid);
        if (cooldowns == null) return false;

        Long cooldownExpiry = cooldowns.get(teleporterId);
        if (cooldownExpiry == null) return false;

        return System.currentTimeMillis() < cooldownExpiry;
    }

    public long getRemainingCooldown(UUID playerUuid, int teleporterId) {
        Map<Integer, Long> cooldowns = playerCooldowns.get(playerUuid);
        if (cooldowns == null) return 0;

        Long cooldownExpiry = cooldowns.get(teleporterId);
        if (cooldownExpiry == null) return 0;

        long remaining = cooldownExpiry - System.currentTimeMillis();
        return Math.max(0, remaining / 1000); // Return seconds
    }

    public void applyCooldown(UUID playerUuid, int teleporterId, int cooldownSeconds) {
        long expiryTime = System.currentTimeMillis() + (cooldownSeconds * 1000L);
        playerCooldowns.computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>())
            .put(teleporterId, expiryTime);
    }

    private String makeKey(String worldName, int x, int y, int z) {
        return worldName + ":" + x + ":" + y + ":" + z;
    }
}
