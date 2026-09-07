package ru.ineprokin.model;

import java.security.SecureRandom;

public final class PasswordGenerator {

    private final SecureRandom random = new SecureRandom();

    public String generate(Alphabet alphabet, int length) {
        String chars = alphabet.chars();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
