package ru.ineprokin;

import java.util.List;

public final class Config {

    // требования к паролю и подсистеме аутентификации
    public static final int MIN_LENGTH = 6;
    public static final int MIN_GROUPS = 2;
    public static final int MAX_ATTEMPTS = 3;
    public static final long LOCK_SECONDS = 15;
    public static final long MAX_AGE_DAYS = 21;
    public static final int SALT_BYTES = 8;
    public static final String HASH_ALGORITHM = "SHA-256";

    public static final List<String> FORBIDDEN_WORDS = List.of(
            "password", "qwerty", "admin", "user", "ivan", "12345", "123456", "пароль", "иван");

    // хранилище (SQLite)
    public static final String DB_FILE = "lab1.db";
    public static final String DB_URL = "jdbc:sqlite:" + DB_FILE;
    public static final String TABLE_USERS = "users";

    // демонстрационная учётная запись
    public static final String DEMO_LOGIN = "ivanov";
    public static final String DEMO_PASSWORD = "Zi#2026lab";

    // вариант 17 (табл. 4.1) — метод парольной защиты для части 1
    public static final String VARIANT = "17";

    // вариант 7 (табл. 3) — исходные данные для части 2
    public static final String VARIANT_PART2 = "7";
    public static final String PART2_TASK = "P = 10^-6, V = 20 паролей/мин, T = 3 недели";
    public static final String DEFAULT_P = "1e-6";
    public static final String DEFAULT_SPEED = "20";
    public static final String DEFAULT_TIME = "3";
    public static final int DEFAULT_PASSWORD_COUNT = 5;
    public static final int MAX_PASSWORD_COUNT = 50;

    // форматы
    public static final String DATE_TIME_PATTERN = "dd.MM.yyyy HH:mm:ss";
    public static final String TIME_PATTERN = "HH:mm:ss";
    public static final int SIGNIFICANT_DIGITS = 4;
    public static final int PROBABILITY_DIGITS = 6;

    // окно и оформление
    public static final String TITLE = "ЛР №1. Парольная защита — вариант " + VARIANT
            + " (часть 2 — вариант " + VARIANT_PART2 + ")";
    public static final double WINDOW_WIDTH = 980;
    public static final double WINDOW_HEIGHT = 700;
    public static final String MONO_FONT = "Consolas";

    public static final String STYLE_OK = "-fx-text-fill: #1b7a2f; -fx-font-weight: bold;";
    public static final String STYLE_ERROR = "-fx-text-fill: #b00000; -fx-font-weight: bold;";
    public static final String STYLE_WARNING = "-fx-text-fill: #c06000; -fx-font-weight: bold;";
    public static final String STYLE_INFO = "-fx-text-fill: #3060a0; -fx-font-weight: bold;";
    public static final String STYLE_HINT = "-fx-text-fill: #555555; -fx-font-size: 11px;";

    private Config() {
    }
}
