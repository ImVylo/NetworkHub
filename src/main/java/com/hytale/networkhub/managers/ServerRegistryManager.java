package com.hytale.networkhub.managers;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.database.DatabaseManager;
import com.hytale.networkhub.database.models.ServerRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import com.hypixel.hytale.logger.HytaleLogger;

public class ServerRegistryManager {
    private final HytaleLogger logger;
    private final DatabaseManager dbManager;
    private final NetworkConfig config;
    private final Map<String, ServerRecord> serverCache = new ConcurrentHashMap<>();
    private long lastCacheUpdate = 0;
    private static final long CACHE_TTL_MS = 30000; // 30 seconds

    public ServerRegistryManager(HytaleLogger logger, DatabaseManager dbManager, NetworkConfig config) {
        this.logger = logger;
        this.dbManager = dbManager;
        this.config = config;
    }

    public void registerServer() {
        NetworkConfig.ServerConfig serverCfg = config.getConfig().server;

        String sql = """
            INSERT INTO servers (server_id, server_name, host, port, server_type, is_hub, hub_priority, max_players, registered_at, last_updated)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (server_id) DO UPDATE SET
                server_name = EXCLUDED.server_name,
                host = EXCLUDED.host,
                port = EXCLUDED.port,
                server_type = EXCLUDED.server_type,
                is_hub = EXCLUDED.is_hub,
                hub_priority = EXCLUDED.hub_priority,
                max_players = EXCLUDED.max_players,
                last_updated = CURRENT_TIMESTAMP
        """;

        // For MySQL/MariaDB, use different syntax
        if (!isSQLSupported(sql)) {
            sql = """
                INSERT INTO servers (server_id, server_name, host, port, server_type, is_hub, hub_priority, max_players, registered_at, last_updated)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                ON DUPLICATE KEY UPDATE
                    server_name = VALUES(server_name),
                    host = VALUES(host),
                    port = VALUES(port),
                    server_type = VALUES(server_type),
                    is_hub = VALUES(is_hub),
                    hub_priority = VALUES(hub_priority),
                    max_players = VALUES(max_players),
                    last_updated = CURRENT_TIMESTAMP
            """;
        }

        int rows = dbManager.executeUpdate(sql,
            serverCfg.serverId,
            serverCfg.serverName,
            "localhost", // TODO: Get actual host
            25565, // TODO: Get actual port
            serverCfg.serverType,
            serverCfg.isHub,
            serverCfg.hubPriority,
            serverCfg.maxPlayers
        );

        if (rows > 0) {
            logger.at(Level.INFO).log("Registered server: " + serverCfg.serverName + " (" + serverCfg.serverId + ")");
        }
    }

    public void unregisterServer(String serverId) {
        String sql = "UPDATE server_health SET status = 'OFFLINE' WHERE server_id = ?";
        dbManager.executeUpdate(sql, serverId);
        logger.at(Level.INFO).log("Unregistered server: " + serverId);
    }

    public ServerRecord getServerById(String serverId) {
        // Check cache first
        if (serverCache.containsKey(serverId) &&
            System.currentTimeMillis() - lastCacheUpdate < CACHE_TTL_MS) {
            return serverCache.get(serverId);
        }

        String sql = """
            SELECT s.*, h.status, h.current_players
            FROM servers s
            LEFT JOIN server_health h ON s.server_id = h.server_id
            WHERE s.server_id = ?
        """;

        ServerRecord server = dbManager.executeQuery(sql, rs -> {
            if (rs.next()) {
                return mapServerRecord(rs);
            }
            return null;
        }, serverId);

        if (server != null) {
            serverCache.put(serverId, server);
        }

        return server;
    }

    public List<ServerRecord> getAllServers() {
        // Refresh cache if expired
        if (System.currentTimeMillis() - lastCacheUpdate > CACHE_TTL_MS) {
            refreshCache();
        }

        return new ArrayList<>(serverCache.values());
    }

    public List<ServerRecord> getHubServers() {
        return getAllServers().stream()
            .filter(ServerRecord::isHub)
            .toList();
    }

    public List<ServerRecord> getOnlineServers() {
        return getAllServers().stream()
            .filter(s -> s.getStatus() == ServerRecord.ServerStatus.ONLINE)
            .toList();
    }

    private void refreshCache() {
        String sql = """
            SELECT s.*, h.status, h.current_players
            FROM servers s
            LEFT JOIN server_health h ON s.server_id = h.server_id
        """;

        List<ServerRecord> servers = dbManager.executeQuery(sql, rs -> {
            List<ServerRecord> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapServerRecord(rs));
            }
            return list;
        });

        if (servers != null) {
            serverCache.clear();
            servers.forEach(s -> serverCache.put(s.getServerId(), s));
            lastCacheUpdate = System.currentTimeMillis();
        }
    }

    private ServerRecord mapServerRecord(ResultSet rs) throws SQLException {
        ServerRecord server = new ServerRecord();
        server.setServerId(rs.getString("server_id"));
        server.setServerName(rs.getString("server_name"));
        server.setHost(rs.getString("host"));
        server.setPort(rs.getInt("port"));
        server.setServerType(rs.getString("server_type"));
        server.setHub(rs.getBoolean("is_hub"));
        server.setHubPriority(rs.getInt("hub_priority"));
        server.setMaxPlayers(rs.getInt("max_players"));
        server.setMotd(rs.getString("motd"));
        server.setRegisteredAt(rs.getTimestamp("registered_at"));
        server.setLastUpdated(rs.getTimestamp("last_updated"));

        // Status and current players from server_health
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            server.setStatus(ServerRecord.ServerStatus.valueOf(statusStr));
            server.setCurrentPlayers(rs.getInt("current_players"));
        } else {
            server.setStatus(ServerRecord.ServerStatus.OFFLINE);
            server.setCurrentPlayers(0);
        }

        return server;
    }

    private boolean isSQLSupported(String sql) {
        // Check if this is PostgreSQL-style ON CONFLICT
        return sql.contains("ON CONFLICT");
    }
}
