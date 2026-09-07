package ru.ineprokin.viewmodel;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ru.ineprokin.model.AccessMatrix;
import ru.ineprokin.model.OperationResult;
import ru.ineprokin.model.Permission;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AccessViewModel {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final AccessMatrix matrix = AccessMatrix.ofVariant();
    private final StringBuilder journal = new StringBuilder();

    private final StringProperty loginInput = new SimpleStringProperty("");
    private final ReadOnlyStringWrapper currentUser = new ReadOnlyStringWrapper("");
    private final ReadOnlyStringWrapper message = new ReadOnlyStringWrapper("Введите идентификатор пользователя");
    private final ReadOnlyStringWrapper log = new ReadOnlyStringWrapper("");
    private final BooleanProperty loggedIn = new SimpleBooleanProperty(false);
    private final BooleanProperty lastOperationFailed = new SimpleBooleanProperty(false);

    private final ObservableList<RightsRow> rights = FXCollections.observableArrayList();
    private final ObservableList<List<String>> matrixRows = FXCollections.observableArrayList();
    private final ObservableList<String> targetUsers = FXCollections.observableArrayList();

    private final ObjectProperty<RightsRow> selectedObject = new SimpleObjectProperty<>();
    private final ObjectProperty<Permission> selectedPermission = new SimpleObjectProperty<>(Permission.READ);
    private final ObjectProperty<String> selectedTarget = new SimpleObjectProperty<>();

    public AccessViewModel() {
        refreshMatrix();
    }

    public record RightsRow(int index, String object, String rights) {
    }

    public StringProperty loginInputProperty() {
        return loginInput;
    }

    public ReadOnlyStringProperty currentUserProperty() {
        return currentUser.getReadOnlyProperty();
    }

    public ReadOnlyStringProperty messageProperty() {
        return message.getReadOnlyProperty();
    }

    public ReadOnlyStringProperty logProperty() {
        return log.getReadOnlyProperty();
    }

    public BooleanProperty loggedInProperty() {
        return loggedIn;
    }

    public BooleanProperty lastOperationFailedProperty() {
        return lastOperationFailed;
    }

    public ObservableList<RightsRow> rights() {
        return rights;
    }

    public ObservableList<List<String>> matrixRows() {
        return matrixRows;
    }

    public ObservableList<String> targetUsers() {
        return targetUsers;
    }

    public ObjectProperty<RightsRow> selectedObjectProperty() {
        return selectedObject;
    }

    public ObjectProperty<Permission> selectedPermissionProperty() {
        return selectedPermission;
    }

    public ObjectProperty<String> selectedTargetProperty() {
        return selectedTarget;
    }

    public List<String> objects() {
        return matrix.objects();
    }

    public String hint() {
        return "Вариант " + ru.ineprokin.Config.VARIANT + ": субъектов — " + matrix.users().size()
                + ", объектов — " + matrix.objects().size()
                + ". Администратор — " + matrix.users().get(ru.ineprokin.Config.ADMIN_INDEX)
                + " (полные права ко всем объектам), права остальных заполнены случайно.";
    }

    public void signIn() {
        String login = loginInput.get().trim();
        if (!matrix.hasUser(login)) {
            loggedIn.set(false);
            currentUser.set("");
            rights.clear();
            targetUsers.clear();
            fail("Идентификация не пройдена: пользователь "
                    + (login.isEmpty() ? "не указан" : login + " не зарегистрирован в системе"), "вход");
            return;
        }
        currentUser.set(login);
        loggedIn.set(true);
        targetUsers.setAll(matrix.users().stream().filter(u -> !u.equals(login)).toList());
        selectedTarget.set(targetUsers.isEmpty() ? null : targetUsers.get(0));
        refreshRights();
        succeed("Идентификация прошла успешно, добро пожаловать в систему"
                + (matrix.isAdmin(login) ? " (администратор)" : ""), "вход");
    }

    public void signOut() {
        String user = currentUser.get();
        loggedIn.set(false);
        currentUser.set("");
        loginInput.set("");
        rights.clear();
        targetUsers.clear();
        succeed("Работа пользователя " + user + " завершена. До свидания.", "выход");
    }

    public void perform(Permission permission) {
        RightsRow row = selectedObject.get();
        if (row == null) {
            fail("Выберите объект", permission.command());
            return;
        }
        apply(matrix.perform(currentUser.get(), row.index(), permission),
                permission.command() + " " + row.object());
    }

    public void grant() {
        RightsRow row = selectedObject.get();
        if (row == null || selectedTarget.get() == null) {
            fail("Выберите объект и пользователя", "grant");
            return;
        }
        OperationResult result = matrix.grant(currentUser.get(), selectedTarget.get(),
                row.index(), selectedPermission.get());
        apply(result, "grant " + selectedPermission.get().title() + " на " + row.object()
                + " → " + selectedTarget.get());
        if (result.success()) {
            refreshMatrix();
            refreshRights();
        }
    }

    private void apply(OperationResult result, String action) {
        if (result.success()) {
            succeed(result.message(), action);
        } else {
            fail(result.message(), action);
        }
    }

    private void refreshRights() {
        List<RightsRow> rows = new ArrayList<>();
        for (int i = 0; i < matrix.objects().size(); i++) {
            rows.add(new RightsRow(i, matrix.objects().get(i), matrix.rightsText(currentUser.get(), i)));
        }
        RightsRow previous = selectedObject.get();
        rights.setAll(rows);
        selectedObject.set(previous == null ? rows.get(0) : rows.get(previous.index()));
    }

    private void refreshMatrix() {
        List<List<String>> rows = new ArrayList<>();
        for (String user : matrix.users()) {
            List<String> row = new ArrayList<>();
            row.add(user + (matrix.isAdmin(user) ? " (администратор)" : ""));
            for (int i = 0; i < matrix.objects().size(); i++) {
                row.add(matrix.rightsText(user, i));
            }
            rows.add(row);
        }
        matrixRows.setAll(rows);
    }

    private void succeed(String text, String action) {
        lastOperationFailed.set(false);
        write(text, action);
    }

    private void fail(String text, String action) {
        lastOperationFailed.set(true);
        write(text, action);
    }

    private void write(String text, String action) {
        message.set(text);
        journal.append(TIME.format(LocalTime.now())).append(" [")
                .append(currentUser.get().isEmpty() ? "-" : currentUser.get()).append("] ")
                .append(action).append(": ").append(text).append('\n');
        log.set(journal.toString());
    }
}
