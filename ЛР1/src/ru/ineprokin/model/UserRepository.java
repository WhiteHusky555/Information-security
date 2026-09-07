package ru.ineprokin.model;

import ru.ineprokin.Config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

public class UserRepository {

    private static final String SELECT = "SELECT * FROM " + Config.TABLE_USERS + " WHERE login = ?";
    private static final String INSERT = "INSERT INTO " + Config.TABLE_USERS
            + "(login, salt, hash, changed_at, last_login, failures, lock_until) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE " + Config.TABLE_USERS
            + " SET salt = ?, hash = ?, changed_at = ?, last_login = ?, failures = ?, lock_until = ? WHERE login = ?";

    public Optional<Account> find(String login) {
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(SELECT)) {
            statement.setString(1, login);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(read(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка чтения учётной записи", e);
        }
    }

    public void insert(Account account) {
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(INSERT)) {
            statement.setString(1, account.login());
            statement.setString(2, account.salt());
            statement.setString(3, account.hash());
            statement.setString(4, account.changedAt().toString());
            statement.setString(5, text(account.lastLogin()));
            statement.setInt(6, account.failures());
            statement.setLong(7, account.lockUntil());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка записи учётной записи", e);
        }
    }

    public void update(Account account) {
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setString(1, account.salt());
            statement.setString(2, account.hash());
            statement.setString(3, account.changedAt().toString());
            statement.setString(4, text(account.lastLogin()));
            statement.setInt(5, account.failures());
            statement.setLong(6, account.lockUntil());
            statement.setString(7, account.login());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка обновления учётной записи", e);
        }
    }

    private Account read(ResultSet rs) throws SQLException {
        return new Account(rs.getString("login"), rs.getString("salt"), rs.getString("hash"),
                LocalDateTime.parse(rs.getString("changed_at")), time(rs.getString("last_login")),
                rs.getInt("failures"), rs.getLong("lock_until"));
    }

    private static String text(LocalDateTime value) {
        return value == null ? null : value.toString();
    }

    private static LocalDateTime time(String value) {
        return value == null ? null : LocalDateTime.parse(value);
    }
}
