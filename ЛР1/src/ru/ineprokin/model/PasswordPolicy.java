package ru.ineprokin.model;

import ru.ineprokin.Config;

import java.util.Locale;

public final class PasswordPolicy {

    private PasswordPolicy() {
    }

    public static String validate(String password) {
        if (password.length() < Config.MIN_LENGTH) {
            return "Пароль короче " + Config.MIN_LENGTH + " символов";
        }
        if (groups(password) < Config.MIN_GROUPS) {
            return "Пароль должен содержать символы минимум " + Config.MIN_GROUPS
                    + " групп (регистры, цифры, спецсимволы)";
        }
        String lower = password.toLowerCase(Locale.ROOT);
        for (String word : Config.FORBIDDEN_WORDS) {
            if (lower.contains(word)) {
                return "Пароль содержит словарное слово: " + word;
            }
        }
        return null;
    }

    private static int groups(String password) {
        int groups = 0;
        if (password.chars().anyMatch(Character::isLowerCase)) groups++;
        if (password.chars().anyMatch(Character::isUpperCase)) groups++;
        if (password.chars().anyMatch(Character::isDigit)) groups++;
        if (password.chars().anyMatch(c -> !Character.isLetterOrDigit(c))) groups++;
        return groups;
    }
}
