package ru.ineprokin.model;

import java.util.List;

public record TableData(String table, List<String> columns, List<List<String>> rows) {
}
