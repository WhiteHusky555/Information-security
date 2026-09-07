package ru.ineprokin.view;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import ru.ineprokin.Config;
import ru.ineprokin.model.AuthStatus;
import ru.ineprokin.viewmodel.AuthViewModel;

public class AuthView extends BorderPane {

    private final AuthViewModel vm;
    private final Label statusLabel = new Label();

    public AuthView(AuthViewModel viewModel) {
        this.vm = viewModel;
        setPadding(new Insets(12));
        setTop(buildForm());
        setCenter(buildLog());
        setBottom(buildStatus());
        startLockTimer();
    }

    private TitledPane buildForm() {
        TextField loginField = new TextField();
        PasswordField passwordField = new PasswordField();
        PasswordField confirmField = new PasswordField();
        PasswordField oldPasswordField = new PasswordField();

        loginField.textProperty().bindBidirectional(vm.loginProperty());
        passwordField.textProperty().bindBidirectional(vm.passwordProperty());
        confirmField.textProperty().bindBidirectional(vm.confirmProperty());
        oldPasswordField.textProperty().bindBidirectional(vm.oldPasswordProperty());
        passwordField.setOnAction(e -> vm.signIn());

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        grid.addRow(0, new Label("Логин:"), loginField);
        grid.addRow(1, new Label("Пароль:"), passwordField);
        grid.addRow(2, new Label("Подтверждение пароля:"), confirmField);
        grid.addRow(3, new Label("Старый пароль (для смены):"), oldPasswordField);

        Button signIn = new Button("Вход");
        Button register = new Button("Регистрация");
        Button change = new Button("Сменить пароль");
        signIn.setOnAction(e -> vm.signIn());
        register.setOnAction(e -> vm.register());
        change.setOnAction(e -> vm.changePassword());
        signIn.disableProperty().bind(vm.lockedProperty());
        register.disableProperty().bind(vm.lockedProperty());
        change.disableProperty().bind(vm.lockedProperty());

        HBox buttons = new HBox(8, signIn, register, change);
        Label hint = new Label(vm.hint() + "\nДемонстрационная учётная запись: "
                + Config.DEMO_LOGIN + " / " + Config.DEMO_PASSWORD);
        hint.setStyle(Config.STYLE_HINT);
        hint.setWrapText(true);

        VBox box = new VBox(10, grid, buttons, hint);
        TitledPane pane = new TitledPane("Простой пароль: идентификация пользователя", box);
        pane.setCollapsible(false);
        return pane;
    }

    private TitledPane buildLog() {
        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setStyle("-fx-font-family: '" + Config.MONO_FONT + "';");
        logArea.textProperty().bind(vm.logProperty());
        logArea.textProperty().addListener((obs, old, value) -> logArea.setScrollTop(Double.MAX_VALUE));
        TitledPane pane = new TitledPane("Журнал обращений", logArea);
        pane.setCollapsible(false);
        BorderPane.setMargin(pane, new Insets(10, 0, 10, 0));
        return pane;
    }

    private Label buildStatus() {
        statusLabel.textProperty().bind(vm.messageProperty());
        statusLabel.setWrapText(true);
        vm.statusProperty().addListener((obs, old, value) -> statusLabel.setStyle(style(value)));
        statusLabel.setStyle(style(vm.statusProperty().get()));
        return statusLabel;
    }

    private void startLockTimer() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> vm.tickLock()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private String style(AuthStatus status) {
        return switch (status) {
            case SUCCESS, REGISTERED -> Config.STYLE_OK;
            case LOCKED -> Config.STYLE_ERROR;
            default -> Config.STYLE_WARNING;
        };
    }
}
