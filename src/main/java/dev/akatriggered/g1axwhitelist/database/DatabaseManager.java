package dev.akatriggered.g1axwhitelist.database;

import java.sql.*;
import java.io.File;

public class DatabaseManager {
    private Connection connection;
    private final File dbFile;
    
    public DatabaseManager(File dataFolder) {
        this.dbFile = new File(dataFolder, "autowhitelisted.db");
    }
    
    public void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        createTable();
    }
    
    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
    
    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS autowhitelisted (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "username TEXT NOT NULL, " +
                    "tier TEXT NOT NULL, " +
                    "whitelisted_at INTEGER NOT NULL)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    public void addPlayer(String uuid, String username, String tier) throws SQLException {
        String sql = "INSERT OR REPLACE INTO autowhitelisted (uuid, username, tier, whitelisted_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            stmt.setString(2, username);
            stmt.setString(3, tier);
            stmt.setLong(4, System.currentTimeMillis() / 1000);
            stmt.executeUpdate();
        }
    }
    
    public boolean isAutoWhitelisted(String uuid) throws SQLException {
        String sql = "SELECT 1 FROM autowhitelisted WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}
