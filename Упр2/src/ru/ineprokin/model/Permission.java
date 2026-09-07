package ru.ineprokin.model;

public enum Permission {

    READ("Чтение", "read"),
    WRITE("Запись", "write"),
    GRANT("Передача прав", "grant");

    private final String title;
    private final String command;

    Permission(String title, String command) {
        this.title = title;
        this.command = command;
    }

    public String title() {
        return title;
    }

    public String command() {
        return command;
    }

    public static Permission byCommand(String value) {
        for (Permission permission : values()) {
            if (permission.command.equalsIgnoreCase(value.trim())) {
                return permission;
            }
        }
        return null;
    }
}
