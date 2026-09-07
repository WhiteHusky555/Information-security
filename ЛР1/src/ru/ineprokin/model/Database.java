package ru.ineprokin.model;

import ru.ineprokin.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {

    private static boolean ready;

    private Database() {
    }

    public static synchronized Connection connect() {
        try {
            Connection connection = DriverManager.getConnection(Config.DB_URL);
            if (!ready) {
                createSchema(connection);
                ready = true;
            }
            return connection;
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось открыть базу " + Config.DB_FILE, e);
        }
    }

    private static void createSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS %s (
                        login      TEXT PRIMARY KEY,
                        salt       TEXT NOT NULL,
                        hash       TEXT NOT NULL,
                        changed_at TEXT NOT NULL,
                        last_login TEXT,
                        failures   INTEGER NOT NULL DEFAULT 0,
                        lock_until INTEGER NOT NULL DEFAULT 0)
                    """.formatted(Config.TABLE_USERS));
        }
    }
}
