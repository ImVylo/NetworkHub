package com.hytale.networkhub.database;

import com.hytale.networkhub.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class SchemaInitializer {
    private final Logger logger;
    private final DatabaseManager dbManager;
    private final String dbType;

    public SchemaInitializer(Logger logger, DatabaseManager dbManager, DatabaseConfig config) {
        this.logger = logger;
        this.dbManager = dbManager;
        this.dbType = config.getConfig().type.toUpperCase();
    }

    public void initialize() {
        logger.info("Initializing database schema...");

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // Create servers table
            stmt.execute(getServersTableSQL());
            logger.info("Created/verified servers table");

            // Create server_health table
            stmt.execute(getServerHealthTableSQL());
            logger.info("Created/verified server_health table");

            // Create player_locations table
            stmt.execute(getPlayerLocationsTableSQL());
            logger.info("Created/verified player_locations table");

            // Create teleporters table
            stmt.execute(getTeleportersTableSQL());
            logger.info("Created/verified teleporters table");

            // Create transfer_history table
            stmt.execute(getTransferHistoryTableSQL());
            logger.info("Created/verified transfer_history table");

            // Create server_queues table
            stmt.execute(getServerQueuesTableSQL());
            logger.info("Created/verified server_queues table");

            // Create chat_messages table
            stmt.execute(getChatMessagesTableSQL());
            logger.info("Created/verified chat_messages table");

            // Create announcements table
            stmt.execute(getAnnouncementsTableSQL());
            logger.info("Created/verified announcements table");

            // Create moderation_actions table
            stmt.execute(getModerationActionsTableSQL());
            logger.info("Created/verified moderation_actions table");

            // Create indexes
            createIndexes(stmt);

            logger.info("Database schema initialized successfully");

        } catch (SQLException e) {
            logger.severe("Failed to initialize database schema: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getServersTableSQL() {
        if (dbType.contains("POSTGRES")) {
            return """
                CREATE TABLE IF NOT EXISTS servers (
                    server_id VARCHAR(64) PRIMARY KEY,
                    server_name VARCHAR(128) NOT NULL,
                    host VARCHAR(255) NOT NULL,
                    port INTEGER NOT NULL,
                    server_type VARCHAR(32) NOT NULL,
                    is_hub BOOLEAN DEFAULT FALSE,
                    hub_priority INTEGER DEFAULT 0,
                    max_players INTEGER DEFAULT 100,
                    motd TEXT,
                    metadata JSONB,
                    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE(host, port)
                )
            """;
        } else {
            return """
                CREATE TABLE IF NOT EXISTS servers (
                    server_id VARCHAR(64) PRIMARY KEY,
                    server_name VARCHAR(128) NOT NULL,
                    host VARCHAR(255) NOT NULL,
                    port INTEGER NOT NULL,
                    server_type VARCHAR(32) NOT NULL,
                    is_hub BOOLEAN DEFAULT FALSE,
                    hub_priority INTEGER DEFAULT 0,
                    max_players INTEGER DEFAULT 100,
                    motd TEXT,
                    metadata JSON,
                    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    UNIQUE(host, port)
                )
            """;
        }
    }

    private String getServerHealthTableSQL() {
        return """
            CREATE TABLE IF NOT EXISTS server_health (
                server_id VARCHAR(64) PRIMARY KEY,
                status VARCHAR(32) NOT NULL,
                last_heartbeat TIMESTAMP NOT NULL,
                current_players INTEGER DEFAULT 0,
                tps DECIMAL(5,2),
                memory_used_mb INTEGER,
                memory_max_mb INTEGER,
                cpu_percent DECIMAL(5,2),
                uptime_seconds BIGINT,
                consecutive_failures INTEGER DEFAULT 0,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
    }

    private String getPlayerLocationsTableSQL() {
        return """
            CREATE TABLE IF NOT EXISTS player_locations (
                player_uuid VARCHAR(36) PRIMARY KEY,
                player_name VARCHAR(64) NOT NULL,
                server_id VARCHAR(64),
                joined_at TIMESTAMP NOT NULL,
                last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                world_name VARCHAR(128),
                x DOUBLE PRECISION,
                y DOUBLE PRECISION,
                z DOUBLE PRECISION
            )
        """;
    }

    private String getTeleportersTableSQL() {
        if (dbType.contains("POSTGRES")) {
            return """
                CREATE TABLE IF NOT EXISTS teleporters (
                    teleporter_id SERIAL PRIMARY KEY,
                    server_id VARCHAR(64) NOT NULL,
                    world_name VARCHAR(128) NOT NULL,
                    x INTEGER NOT NULL,
                    y INTEGER NOT NULL,
                    z INTEGER NOT NULL,
                    destination_server_id VARCHAR(64) NOT NULL,
                    destination_world VARCHAR(128),
                    destination_x DOUBLE PRECISION,
                    destination_y DOUBLE PRECISION,
                    destination_z DOUBLE PRECISION,
                    display_name VARCHAR(128),
                    permission VARCHAR(128),
                    cooldown_seconds INTEGER DEFAULT 0,
                    enabled BOOLEAN DEFAULT TRUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE(server_id, world_name, x, y, z)
                )
            """;
        } else {
            return """
                CREATE TABLE IF NOT EXISTS teleporters (
                    teleporter_id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    server_id VARCHAR(64) NOT NULL,
                    world_name VARCHAR(128) NOT NULL,
                    x INTEGER NOT NULL,
                    y INTEGER NOT NULL,
                    z INTEGER NOT NULL,
                    destination_server_id VARCHAR(64) NOT NULL,
                    destination_world VARCHAR(128),
                    destination_x DOUBLE,
                    destination_y DOUBLE,
                    destination_z DOUBLE,
                    display_name VARCHAR(128),
                    permission VARCHAR(128),
                    cooldown_seconds INTEGER DEFAULT 0,
                    enabled BOOLEAN DEFAULT TRUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    UNIQUE(server_id, world_name, x, y, z)
                )
            """;
        }
    }

    private String getTransferHistoryTableSQL() {
        if (dbType.contains("POSTGRES")) {
            return """
                CREATE TABLE IF NOT EXISTS transfer_history (
                    transfer_id BIGSERIAL PRIMARY KEY,
                    player_uuid VARCHAR(36) NOT NULL,
                    player_name VARCHAR(64) NOT NULL,
                    from_server_id VARCHAR(64),
                    to_server_id VARCHAR(64),
                    transfer_type VARCHAR(32) NOT NULL,
                    initiated_by VARCHAR(36),
                    reason TEXT,
                    success BOOLEAN DEFAULT TRUE,
                    transferred_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
        } else {
            return """
                CREATE TABLE IF NOT EXISTS transfer_history (
                    transfer_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    player_uuid VARCHAR(36) NOT NULL,
                    player_name VARCHAR(64) NOT NULL,
                    from_server_id VARCHAR(64),
                    to_server_id VARCHAR(64),
                    transfer_type VARCHAR(32) NOT NULL,
                    initiated_by VARCHAR(36),
                    reason TEXT,
                    success BOOLEAN DEFAULT TRUE,
                    transferred_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
        }
    }

    private String getServerQueuesTableSQL() {
        if (dbType.contains("POSTGRES")) {
            return """
                CREATE TABLE IF NOT EXISTS server_queues (
                    queue_id SERIAL PRIMARY KEY,
                    server_id VARCHAR(64) NOT NULL,
                    player_uuid VARCHAR(36) NOT NULL,
                    player_name VARCHAR(64) NOT NULL,
                    priority INTEGER DEFAULT 0,
                    joined_queue_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    position INTEGER,
                    notified BOOLEAN DEFAULT FALSE,
                    UNIQUE(server_id, player_uuid)
                )
            """;
        } else {
            return """
                CREATE TABLE IF NOT EXISTS server_queues (
                    queue_id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    server_id VARCHAR(64) NOT NULL,
                    player_uuid VARCHAR(36) NOT NULL,
                    player_name VARCHAR(64) NOT NULL,
                    priority INTEGER DEFAULT 0,
                    joined_queue_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    position INTEGER,
                    notified BOOLEAN DEFAULT FALSE,
                    UNIQUE(server_id, player_uuid)
                )
            """;
        }
    }

    private String getChatMessagesTableSQL() {
        if (dbType.contains("POSTGRES")) {
            return """
                CREATE TABLE IF NOT EXISTS chat_messages (
                    message_id BIGSERIAL PRIMARY KEY,
                    player_uuid VARCHAR(36) NOT NULL,
                    player_name VARCHAR(64) NOT NULL,
                    server_id VARCHAR(64),
                    message_type VARCHAR(32) NOT NULL,
                    recipient_uuid VARCHAR(36),
                    content TEXT NOT NULL,
                    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
        } else {
            return """
                CREATE TABLE IF NOT EXISTS chat_messages (
                    message_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    player_uuid VARCHAR(36) NOT NULL,
                    player_name VARCHAR(64) NOT NULL,
                    server_id VARCHAR(64),
                    message_type VARCHAR(32) NOT NULL,
                    recipient_uuid VARCHAR(36),
                    content TEXT NOT NULL,
                    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
        }
    }

    private String getAnnouncementsTableSQL() {
        if (dbType.contains("POSTGRES")) {
            return """
                CREATE TABLE IF NOT EXISTS announcements (
                    announcement_id BIGSERIAL PRIMARY KEY,
                    created_by_uuid VARCHAR(36) NOT NULL,
                    created_by_name VARCHAR(64) NOT NULL,
                    title TEXT,
                    subtitle TEXT,
                    action_bar TEXT,
                    display_type VARCHAR(32) NOT NULL,
                    target_servers TEXT,
                    target_permissions VARCHAR(128),
                    duration_seconds INTEGER NOT NULL,
                    priority INTEGER DEFAULT 0,
                    sound VARCHAR(128),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    expires_at TIMESTAMP
                )
            """;
        } else {
            return """
                CREATE TABLE IF NOT EXISTS announcements (
                    announcement_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    created_by_uuid VARCHAR(36) NOT NULL,
                    created_by_name VARCHAR(64) NOT NULL,
                    title TEXT,
                    subtitle TEXT,
                    action_bar TEXT,
                    display_type VARCHAR(32) NOT NULL,
                    target_servers TEXT,
                    target_permissions VARCHAR(128),
                    duration_seconds INTEGER NOT NULL,
                    priority INTEGER DEFAULT 0,
                    sound VARCHAR(128),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    expires_at TIMESTAMP
                )
            """;
        }
    }

    private String getModerationActionsTableSQL() {
        if (dbType.contains("POSTGRES")) {
            return """
                CREATE TABLE IF NOT EXISTS moderation_actions (
                    action_id BIGSERIAL PRIMARY KEY,
                    action_type VARCHAR(32) NOT NULL,
                    target_uuid VARCHAR(36) NOT NULL,
                    target_name VARCHAR(64) NOT NULL,
                    moderator_uuid VARCHAR(36) NOT NULL,
                    moderator_name VARCHAR(64) NOT NULL,
                    reason TEXT,
                    server_id VARCHAR(64),
                    active BOOLEAN DEFAULT TRUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    expires_at TIMESTAMP,
                    unbanned_by VARCHAR(36),
                    unbanned_at TIMESTAMP
                )
            """;
        } else {
            return """
                CREATE TABLE IF NOT EXISTS moderation_actions (
                    action_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    action_type VARCHAR(32) NOT NULL,
                    target_uuid VARCHAR(36) NOT NULL,
                    target_name VARCHAR(64) NOT NULL,
                    moderator_uuid VARCHAR(36) NOT NULL,
                    moderator_name VARCHAR(64) NOT NULL,
                    reason TEXT,
                    server_id VARCHAR(64),
                    active BOOLEAN DEFAULT TRUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    expires_at TIMESTAMP,
                    unbanned_by VARCHAR(36),
                    unbanned_at TIMESTAMP
                )
            """;
        }
    }

    private void createIndexes(Statement stmt) throws SQLException {
        String[] indexes = {
            "CREATE INDEX IF NOT EXISTS idx_server_health_heartbeat ON server_health(last_heartbeat)",
            "CREATE INDEX IF NOT EXISTS idx_servers_is_hub ON servers(is_hub, hub_priority DESC)",
            "CREATE INDEX IF NOT EXISTS idx_player_locations_server ON player_locations(server_id)",
            "CREATE INDEX IF NOT EXISTS idx_teleporters_location ON teleporters(server_id, world_name)",
            "CREATE INDEX IF NOT EXISTS idx_server_queues_server ON server_queues(server_id, priority DESC, joined_queue_at ASC)",
            "CREATE INDEX IF NOT EXISTS idx_server_queues_player ON server_queues(player_uuid)",
            "CREATE INDEX IF NOT EXISTS idx_chat_messages_time ON chat_messages(sent_at DESC)",
            "CREATE INDEX IF NOT EXISTS idx_chat_messages_recipient ON chat_messages(recipient_uuid, sent_at DESC)",
            "CREATE INDEX IF NOT EXISTS idx_announcements_time ON announcements(created_at DESC)",
            "CREATE INDEX IF NOT EXISTS idx_moderation_target ON moderation_actions(target_uuid, active)",
            "CREATE INDEX IF NOT EXISTS idx_transfer_history_player ON transfer_history(player_uuid, transferred_at DESC)"
        };

        for (String index : indexes) {
            try {
                stmt.execute(index);
            } catch (SQLException e) {
                // Index might already exist, continue
                logger.fine("Index creation note: " + e.getMessage());
            }
        }

        logger.info("Created/verified all indexes");
    }
}
