package ru.ineprokin;

public final class Config {

    public static final String VARIANT = "17";
    public static final int USER_COUNT = 3;
    public static final int OBJECT_COUNT = 4;

    public static final String[] USER_NAMES = {"Ivan", "Sergey", "Boris"};
    public static final String[] OBJECT_NAMES = {"Файл_1", "Файл_2", "CD-RW", "Дисковод"};

    public static final int ADMIN_INDEX = 0;

    public static final String TITLE = "ПР №2. Дискреционная модель политики безопасности — вариант " + VARIANT;
    public static final double WINDOW_WIDTH = 980;
    public static final double WINDOW_HEIGHT = 700;
    public static final String MONO_FONT = "Consolas";

    public static final String STYLE_OK = "-fx-text-fill: #1b7a2f; -fx-font-weight: bold;";
    public static final String STYLE_ERROR = "-fx-text-fill: #b00000; -fx-font-weight: bold;";
    public static final String STYLE_HINT = "-fx-text-fill: #555555; -fx-font-size: 11px;";

    private Config() {
    }
}
