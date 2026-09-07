package ru.ineprokin.model;

public record AuthResponse(AuthStatus status, String message, int attemptsLeft, long lockSeconds) {

    public boolean ok() {
        return status == AuthStatus.SUCCESS || status == AuthStatus.REGISTERED;
    }
}
