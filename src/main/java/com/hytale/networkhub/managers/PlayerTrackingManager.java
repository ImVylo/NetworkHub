package com.hytale.networkhub.managers;

import com.hytale.networkhub.config.NetworkConfig;
import com.hytale.networkhub.database.DatabaseManager;
import com.hytale.networkhub.database.models.PlayerLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class PlayerTrackingManager {
    private final Logger logger;
    private final DatabaseManager dbManager;
    private final NetworkConfig config;

    public PlayerTrackingManager(Logger logger, DatabaseManager dbManager, NetworkConfig config) {
        this.logger = logger;
        this.dbManager = dbManager;
        this.config = config;
    }

    public void trackJoin(UUID playerUuid, String playerName) {
        String serverId = config.getConfig().server.serverId;

        String sql = """
            INSERT INTO player_locations (player_uuid, player_name, server_id, joined_at, last_seen)
            VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (player_uuid) DO UPDATE SET
                player_name = EXCLUDED.player_name,
                server_id = EXCLUDED.server_id,
                joined_at = CURRENT_TIMESTAMP,
                last_seen = CURRENT_TIMESTAMP
        """;

        // For MySQL/MariaDB
        if (isMySQL()) {
            sql = """
                INSERT INTO player_locations (player_uuid, player_name, server_id, joined_at, last_seen)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                ON DUPLICATE KEY UPDATE
                    player_name = VALUES(player_name),
                    server_id = VALUES(server_id),
                    joined_at = CURRENT_TIMESTAMP,
                    last_seen = CURRENT_TIMESTAMP
            """;
        }

        dbManager.executeUpdate(sql, playerUuid.toString(), playerName, serverId);
        logger.fine("Tracked player join: " + playerName + " on " + serverId);
    }

    public void trackQuit(UUID playerUuid) {
        String sql = "UPDATE player_locations SET last_seen = CURRENT_TIMESTAMP WHERE player_uuid = ?";
        dbManager.executeUpdate(sql, playerUuid.toString());
    }

    public void updateLocation(UUID playerUuid, String worldName, double x, double y, double z) {
        String sql = """
            UPDATE player_locations
            SET world_name = ?, x = ?, y = ?, z = ?, last_seen = CURRENT_TIMESTAMP
            WHERE player_uuid = ?
        """;
        dbManager.executeUpdateAsync(sql, worldName, x, y, z, playerUuid.toString());
    }

    public PlayerLocation findPlayer(UUID playerUuid) {
        String sql = """
            SELECT pl.*, s.server_name
            FROM player_locations pl
            LEFT JOIN servers s ON pl.server_id = s.server_id
            WHERE pl.player_uuid = ?
        """;

        return dbManager.executeQuery(sql, rs -> {
            if (rs.next()) {
                PlayerLocation location = new PlayerLocation();
                location.setPlayerUuid(UUID.fromString(rs.getString("player_uuid")));
                location.setPlayerName(rs.getString("player_name"));
                location.setServerId(rs.getString("server_id"));
                location.setJoinedAt(rs.getTimestamp("joined_at"));
                location.setLastSeen(rs.getTimestamp("last_seen"));
                location.setWorldName(rs.getString("world_name"));
                location.setX(rs.getDouble("x"));
                location.setY(rs.getDouble("y"));
                location.setZ(rs.getDouble("z"));
                return location;
            }
            return null;
        }, playerUuid.toString());
    }

    public PlayerLocation findPlayerByName(String playerName) {
        String sql = """
            SELECT pl.*, s.server_name
            FROM player_locations pl
            LEFT JOIN servers s ON pl.server_id = s.server_id
            WHERE LOWER(pl.player_name) = LOWER(?)
        """;

        return dbManager.executeQuery(sql, rs -> {
            if (rs.next()) {
                PlayerLocation location = new PlayerLocation();
                location.setPlayerUuid(UUID.fromString(rs.getString("player_uuid")));
                location.setPlayerName(rs.getString("player_name"));
                location.setServerId(rs.getString("server_id"));
                location.setJoinedAt(rs.getTimestamp("joined_at"));
                location.setLastSeen(rs.getTimestamp("last_seen"));
                location.setWorldName(rs.getString("world_name"));
                location.setX(rs.getDouble("x"));
                location.setY(rs.getDouble("y"));
                location.setZ(rs.getDouble("z"));
                return location;
            }
            return null;
        }, playerName);
    }

    public List<PlayerLocation> getPlayersOnServer(String serverId) {
        String sql = "SELECT * FROM player_locations WHERE server_id = ?";

        return dbManager.executeQuery(sql, rs -> {
            List<PlayerLocation> players = new ArrayList<>();
            while (rs.next()) {
                PlayerLocation location = new PlayerLocation();
                location.setPlayerUuid(UUID.fromString(rs.getString("player_uuid")));
                location.setPlayerName(rs.getString("player_name"));
                location.setServerId(rs.getString("server_id"));
                location.setJoinedAt(rs.getTimestamp("joined_at"));
                location.setLastSeen(rs.getTimestamp("last_seen"));
                location.setWorldName(rs.getString("world_name"));
                location.setX(rs.getDouble("x"));
                location.setY(rs.getDouble("y"));
                location.setZ(rs.getDouble("z"));
                players.add(location);
            }
            return players;
        }, serverId);
    }

    private boolean isMySQL() {
        return true; // Simplified for now
    }
}
