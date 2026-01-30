package com.hytale.networkhub.database;

import com.hytale.networkhub.config.DatabaseConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import com.hypixel.hytale.logger.HytaleLogger;
import java.util.logging.Level;

public class DatabaseManager {
    private final HytaleLogger logger;
    private final DatabaseConfig config;
    private HikariDataSource dataSource;
    private final Executor asyncExecutor;

    public DatabaseManager(HytaleLogger logger, DatabaseConfig config) {
        this.logger = logger;
        this.config = config;
        this.asyncExecutor = Executors.newFixedThreadPool(4);
    }

    public void initialize() throws SQLException {
        logger.at(Level.INFO).log("Initializing database connection pool...");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getJdbcUrl());
        hikariConfig.setUsername(config.getConfig().username);
        hikariConfig.setPassword(config.getConfig().password);

        DatabaseConfig.ConnectionPoolConfig poolConfig = config.getConfig().connectionPool;
        hikariConfig.setMinimumIdle(poolConfig.minimumIdle);
        hikariConfig.setMaximumPoolSize(poolConfig.maximumPoolSize);
        hikariConfig.setConnectionTimeout(poolConfig.connectionTimeout);
        hikariConfig.setIdleTimeout(poolConfig.idleTimeout);
        hikariConfig.setMaxLifetime(poolConfig.maxLifetime);

        // Performance optimizations
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");

        dataSource = new HikariDataSource(hikariConfig);

        // Test connection
        try (Connection conn = dataSource.getConnection()) {
            logger.at(Level.INFO).log("Database connection successful: " + config.getConfig().type);
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database not initialized");
        }
        return dataSource.getConnection();
    }

    public int executeUpdate(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt, params);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            logger.at(Level.SEVERE).log("Failed to execute update: " + e.getMessage());
            return 0;
        }
    }

    public <T> T executeQuery(String sql, ResultSetHandler<T> handler, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                return handler.handle(rs);
            }
        } catch (SQLException e) {
            logger.at(Level.SEVERE).log("Failed to execute query: " + e.getMessage());
            return null;
        }
    }

    public CompletableFuture<Integer> executeUpdateAsync(String sql, Object... params) {
        return CompletableFuture.supplyAsync(() -> executeUpdate(sql, params), asyncExecutor);
    }

    public <T> CompletableFuture<T> executeQueryAsync(String sql, ResultSetHandler<T> handler, Object... params) {
        return CompletableFuture.supplyAsync(() -> executeQuery(sql, handler, params), asyncExecutor);
    }

    private void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }

    public boolean isMySQL() {
        String dbType = config.getConfig().type.toLowerCase();
        return dbType.equals("mysql") || dbType.equals("mariadb");
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.at(Level.INFO).log("Database connection pool closed");
        }
    }

    @FunctionalInterface
    public interface ResultSetHandler<T> {
        T handle(ResultSet rs) throws SQLException;
    }
}
