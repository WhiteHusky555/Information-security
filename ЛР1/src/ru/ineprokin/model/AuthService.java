package ru.ineprokin.model;

import ru.ineprokin.Config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class AuthService {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern(Config.DATE_TIME_PATTERN);

    private final UserRepository repository;
    private final SecureRandom random = new SecureRandom();

    public AuthService(UserRepository repository) {
        this.repository = repository;
        if (repository.find(Config.DEMO_LOGIN).isEmpty()) {
            repository.insert(newAccount(Config.DEMO_LOGIN, Config.DEMO_PASSWORD));
        }
    }

    public AuthResponse register(String login, String password, String confirm) {
        if (login.isBlank()) {
            return fail(AuthStatus.UNKNOWN_USER, "Введите логин");
        }
        if (repository.find(login).isPresent()) {
            return fail(AuthStatus.DUPLICATE_LOGIN, "Пользователь уже зарегистрирован");
        }
        if (!password.equals(confirm)) {
            return fail(AuthStatus.WEAK_PASSWORD, "Пароли не совпадают");
        }
        String problem = PasswordPolicy.validate(password);
        if (problem != null) {
            return fail(AuthStatus.WEAK_PASSWORD, problem);
        }
        repository.insert(newAccount(login, password));
        return new AuthResponse(AuthStatus.REGISTERED, "Пользователь " + login + " зарегистрирован", 0, 0);
    }

    public AuthResponse authenticate(String login, String password) {
        Optional<Account> found = repository.find(login);
        if (found.isEmpty()) {
            return fail(AuthStatus.UNKNOWN_USER, "Неудовлетворительное обращение: пользователь не найден");
        }
        Account account = found.get();
        long lock = account.lockSecondsLeft();
        if (lock > 0) {
            return new AuthResponse(AuthStatus.LOCKED, "Терминал отключён, ожидайте " + lock + " с", 0, lock);
        }
        if (!hash(password, account.salt()).equals(account.hash())) {
            account.registerFailure();
            int left = Config.MAX_ATTEMPTS - account.failures();
            if (left <= 0) {
                account.lockFor(Config.LOCK_SECONDS);
            }
            repository.update(account);
            if (left <= 0) {
                return new AuthResponse(AuthStatus.LOCKED,
                        "Превышено число попыток, терминал отключён на " + Config.LOCK_SECONDS + " с",
                        0, Config.LOCK_SECONDS);
            }
            return new AuthResponse(AuthStatus.WRONG_PASSWORD,
                    "Неверный пароль, осталось попыток: " + left, left, 0);
        }
        long age = Duration.between(account.changedAt(), LocalDateTime.now()).toDays();
        if (age > Config.MAX_AGE_DAYS) {
            return fail(AuthStatus.EXPIRED, "Срок действия пароля истёк, требуется смена");
        }
        String previous = account.lastLogin() == null
                ? "первый вход в систему"
                : "предыдущий вход: " + FORMAT.format(account.lastLogin());
        account.setLastLogin(LocalDateTime.now());
        account.resetFailures();
        repository.update(account);
        return new AuthResponse(AuthStatus.SUCCESS, "Доступ разрешён (" + previous + ")", Config.MAX_ATTEMPTS, 0);
    }

    public AuthResponse changePassword(String login, String oldPassword, String password, String confirm) {
        AuthResponse check = authenticate(login, oldPassword);
        if (!check.ok()) {
            return check;
        }
        if (!password.equals(confirm)) {
            return fail(AuthStatus.WEAK_PASSWORD, "Пароли не совпадают");
        }
        String problem = PasswordPolicy.validate(password);
        if (problem != null) {
            return fail(AuthStatus.WEAK_PASSWORD, problem);
        }
        Account account = repository.find(login).orElseThrow();
        String salt = newSalt();
        account.setSecret(salt, hash(password, salt), LocalDateTime.now());
        repository.update(account);
        return new AuthResponse(AuthStatus.REGISTERED, "Пароль изменён", 0, 0);
    }

    private Account newAccount(String login, String password) {
        String salt = newSalt();
        return new Account(login, salt, hash(password, salt), LocalDateTime.now());
    }

    private AuthResponse fail(AuthStatus status, String message) {
        return new AuthResponse(status, message, 0, 0);
    }

    private String newSalt() {
        byte[] bytes = new byte[Config.SALT_BYTES];
        random.nextBytes(bytes);
        return toHex(bytes);
    }

    private String hash(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(Config.HASH_ALGORITHM);
            return toHex(digest.digest((salt + password).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
