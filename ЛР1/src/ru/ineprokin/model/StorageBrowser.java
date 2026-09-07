package ru.ineprokin.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StorageBrowser {

    public List<String> tables() {
        List<String> tables = new ArrayList<>();
        try (Connection connection = Database.connect();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%' ORDER BY name")) {
            while (rs.next()) {
                tables.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка чтения списка таблиц", e);
        }
        return tables;
    }

    public TableData read(String table) {
        if (!tables().contains(table)) {
            throw new IllegalArgumentException("Таблица не найдена: " + table);
        }
        List<String> columns = new ArrayList<>();
        List<List<String>> rows = new ArrayList<>();
        try (Connection connection = Database.connect();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM \"" + table + "\"")) {
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                columns.add(meta.getColumnLabel(i));
            }
            while (rs.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 1; i <= columns.size(); i++) {
                    Object value = rs.getObject(i);
                    row.add(value == null ? "" : value.toString());
                }
                rows.add(row);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка чтения таблицы " + table, e);
        }
        return new TableData(table, columns, rows);
    }
}
