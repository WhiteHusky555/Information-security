package ru.ineprokin.model;

public enum Alphabet {
    DIGITS("Цифры (0-9)", "0123456789"),
    LOWER("Малые латинские (a-z)", "abcdefghijklmnopqrstuvwxyz"),
    LOWER_DIGITS("Малые латинские + цифры", "abcdefghijklmnopqrstuvwxyz0123456789"),
    LETTERS("Латинские обоих регистров", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"),
    LETTERS_DIGITS("Латинские обоих регистров + цифры",
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"),
    LETTERS_DIGITS_SPEC("Латинские + цифры + спецсимволы",
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!\"#$%&'"),
    RUSSIAN("Русские буквы обоих регистров",
            "абвгдежзийклмнопрстуфхцчшщъыьэюяАБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ");

    private final String title;
    private final String chars;

    Alphabet(String title, String chars) {
        this.title = title;
        this.chars = chars;
    }

    public String chars() {
        return chars;
    }

    public int power() {
        return chars.length();
    }

    @Override
    public String toString() {
        return title + " [A = " + power() + "]";
    }
}
