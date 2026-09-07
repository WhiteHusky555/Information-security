package ru.ineprokin.model;

import java.time.LocalDateTime;

public class Account {

    private final String login;
    private String salt;
    private String hash;
    private LocalDateTime changedAt;
    private LocalDateTime lastLogin;
    private int failures;
    private long lockUntil;

    public Account(String login, String salt, String hash, LocalDateTime changedAt) {
        this(login, salt, hash, changedAt, null, 0, 0);
    }

    public Account(String login, String salt, String hash, LocalDateTime changedAt,
                   LocalDateTime lastLogin, int failures, long lockUntil) {
        this.login = login;
        this.salt = salt;
        this.hash = hash;
        this.changedAt = changedAt;
        this.lastLogin = lastLogin;
        this.failures = failures;
        this.lockUntil = lockUntil;
    }

    public String login() {
        return login;
    }

    public String salt() {
        return salt;
    }

    public String hash() {
        return hash;
    }

    public LocalDateTime changedAt() {
        return changedAt;
    }

    public LocalDateTime lastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime value) {
        this.lastLogin = value;
    }

    public void setSecret(String salt, String hash, LocalDateTime changedAt) {
        this.salt = salt;
        this.hash = hash;
        this.changedAt = changedAt;
    }

    public int failures() {
        return failures;
    }

    public long lockUntil() {
        return lockUntil;
    }

    public void registerFailure() {
        failures++;
    }

    public void resetFailures() {
        failures = 0;
        lockUntil = 0;
    }

    public void lockFor(long seconds) {
        lockUntil = System.currentTimeMillis() + seconds * 1000;
        failures = 0;
    }

    public long lockSecondsLeft() {
        long left = lockUntil - System.currentTimeMillis();
        return left > 0 ? (left + 999) / 1000 : 0;
    }
}
