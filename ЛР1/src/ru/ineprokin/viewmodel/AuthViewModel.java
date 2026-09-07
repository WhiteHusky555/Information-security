package ru.ineprokin.viewmodel;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import ru.ineprokin.Config;
import ru.ineprokin.model.AuthResponse;
import ru.ineprokin.model.AuthService;
import ru.ineprokin.model.AuthStatus;
import ru.ineprokin.model.UserRepository;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AuthViewModel {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern(Config.TIME_PATTERN);

    private final AuthService service = new AuthService(new UserRepository());
    private final StringBuilder journal = new StringBuilder();

    private final StringProperty login = new SimpleStringProperty("");
    private final StringProperty password = new SimpleStringProperty("");
    private final StringProperty confirm = new SimpleStringProperty("");
    private final StringProperty oldPassword = new SimpleStringProperty("");

    private final ReadOnlyStringWrapper message = new ReadOnlyStringWrapper("Введите логин и пароль");
    private final ReadOnlyStringWrapper log = new ReadOnlyStringWrapper("");
    private final ObjectProperty<AuthStatus> status = new SimpleObjectProperty<>(AuthStatus.UNKNOWN_USER);
    private final BooleanProperty locked = new SimpleBooleanProperty(false);

    private long lockDeadline;

    public StringProperty loginProperty() {
        return login;
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public StringProperty confirmProperty() {
        return confirm;
    }

    public StringProperty oldPasswordProperty() {
        return oldPassword;
    }

    public ReadOnlyStringProperty messageProperty() {
        return message.getReadOnlyProperty();
    }

    public ReadOnlyStringProperty logProperty() {
        return log.getReadOnlyProperty();
    }

    public ObjectProperty<AuthStatus> statusProperty() {
        return status;
    }

    public BooleanProperty lockedProperty() {
        return locked;
    }

    public String hint() {
        return "Требования: длина от " + Config.MIN_LENGTH + " символов, минимум "
                + Config.MIN_GROUPS + " группы символов, не словарное слово. Попыток входа: "
                + Config.MAX_ATTEMPTS + ", блокировка " + Config.LOCK_SECONDS
                + " с, срок действия пароля " + Config.MAX_AGE_DAYS + " дней.";
    }

    public void signIn() {
        apply(service.authenticate(login.get(), password.get()), "вход");
    }

    public void register() {
        apply(service.register(login.get(), password.get(), confirm.get()), "регистрация");
    }

    public void changePassword() {
        apply(service.changePassword(login.get(), oldPassword.get(), password.get(), confirm.get()), "смена пароля");
    }

    public long lockSecondsLeft() {
        long left = lockDeadline - System.currentTimeMillis();
        return left > 0 ? (left + 999) / 1000 : 0;
    }

    public void tickLock() {
        long left = lockSecondsLeft();
        if (left > 0) {
            message.set("Терминал отключён, осталось " + left + " с");
        } else if (locked.get()) {
            locked.set(false);
            status.set(AuthStatus.WRONG_PASSWORD);
            message.set("Терминал разблокирован, повторите ввод");
        }
    }

    private void apply(AuthResponse response, String action) {
        if (response.status() == AuthStatus.LOCKED && response.lockSeconds() > 0) {
            lockDeadline = System.currentTimeMillis() + response.lockSeconds() * 1000;
            locked.set(true);
        }
        status.set(response.status());
        message.set(response.message());
        journal.append(TIME.format(LocalTime.now())).append(" [").append(action).append("] ")
                .append(login.get().isBlank() ? "-" : login.get()).append(" -> ")
                .append(response.status()).append(": ").append(response.message()).append('\n');
        log.set(journal.toString());
    }
}
