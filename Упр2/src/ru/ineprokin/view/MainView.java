package ru.ineprokin.view;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import ru.ineprokin.Config;
import ru.ineprokin.model.Permission;
import ru.ineprokin.viewmodel.AccessViewModel;
import ru.ineprokin.viewmodel.AccessViewModel.RightsRow;

import java.util.List;

public class MainView extends BorderPane {

    private final AccessViewModel vm = new AccessViewModel();

    public MainView() {
        setPadding(new Insets(12));
        setTop(buildLogin());
        setCenter(buildWorkspace());
        setBottom(buildStatus());
    }

    private TitledPane buildLogin() {
        TextField loginField = new TextField();
        loginField.setPromptText("идентификатор пользователя");
        loginField.setPrefColumnCount(16);
        loginField.textProperty().bindBidirectional(vm.loginInputProperty());
        loginField.setOnAction(e -> vm.signIn());
        loginField.disableProperty().bind(vm.loggedInProperty());

        Button signIn = new Button("Войти");
        signIn.setOnAction(e -> vm.signIn());
        signIn.setDefaultButton(true);
        signIn.disableProperty().bind(vm.loggedInProperty());

        Button signOut = new Button("Выйти из системы");
        signOut.setOnAction(e -> vm.signOut());
        signOut.disableProperty().bind(vm.loggedInProperty().not());

        Label current = new Label();
        current.textProperty().bind(Bindings.concat("Текущий пользователь: ",
                Bindings.when(vm.loggedInProperty()).then(vm.currentUserProperty()).otherwise("не определён")));

        Label hint = new Label(vm.hint());
        hint.setStyle(Config.STYLE_HINT);
        hint.setWrapText(true);

        HBox row = new HBox(8, new Label("User:"), loginField, signIn, signOut, current);
        VBox box = new VBox(8, row, hint);
        box.setPadding(new Insets(10));
        TitledPane pane = new TitledPane("Идентификация пользователя", box);
        pane.setCollapsible(false);
        return pane;
    }

    private VBox buildWorkspace() {
        VBox box = new VBox(10, buildMatrix(), buildRights(), buildLog());
        VBox.setMargin(box, new Insets(10, 0, 10, 0));
        return box;
    }

    private TitledPane buildMatrix() {
        TableView<List<String>> table = new TableView<>(vm.matrixRows());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(140);

        TableColumn<List<String>, String> subject = new TableColumn<>("Субъект / Объект");
        subject.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().get(0)));
        table.getColumns().add(subject);
        for (int i = 0; i < vm.objects().size(); i++) {
            int index = i + 1;
            TableColumn<List<String>, String> column = new TableColumn<>(vm.objects().get(i));
            column.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().get(index)));
            table.getColumns().add(column);
        }

        TitledPane pane = new TitledPane("Матрица доступа", table);
        pane.setCollapsible(false);
        return pane;
    }

    private TitledPane buildRights() {
        TableView<RightsRow> table = new TableView<>(vm.rights());
        table.setPlaceholder(new Label("Войдите в систему, чтобы увидеть свои права"));
        table.setPrefHeight(140);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, value) -> vm.selectedObjectProperty().set(value));
        vm.selectedObjectProperty().addListener((obs, old, value) -> table.getSelectionModel().select(value));

        TableColumn<RightsRow, String> number = new TableColumn<>("№");
        number.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().index() + 1)));
        number.setPrefWidth(50);
        TableColumn<RightsRow, String> object = new TableColumn<>("Объект");
        object.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().object()));
        object.setPrefWidth(200);
        TableColumn<RightsRow, String> rights = new TableColumn<>("Права");
        rights.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().rights()));
        table.getColumns().add(number);
        table.getColumns().add(object);
        table.getColumns().add(rights);

        Button read = new Button("Чтение");
        read.setOnAction(e -> vm.perform(Permission.READ));
        Button write = new Button("Запись");
        write.setOnAction(e -> vm.perform(Permission.WRITE));

        ComboBox<Permission> permission = new ComboBox<>();
        permission.getItems().setAll(Permission.values());
        permission.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Permission value) {
                return value == null ? "" : value.title();
            }

            @Override
            public Permission fromString(String value) {
                return null;
            }
        });
        permission.valueProperty().bindBidirectional(vm.selectedPermissionProperty());

        ComboBox<String> target = new ComboBox<>(vm.targetUsers());
        target.valueProperty().bindBidirectional(vm.selectedTargetProperty());

        Button grant = new Button("Передать право");
        grant.setOnAction(e -> vm.grant());

        HBox operations = new HBox(8, new Label("Операции над выбранным объектом:"), read, write);
        HBox granting = new HBox(8, new Label("Передача права:"), permission,
                new Label("пользователю"), target, grant);
        for (javafx.scene.Node node : List.of(read, write, permission, target, grant)) {
            node.disableProperty().bind(vm.loggedInProperty().not());
        }

        VBox box = new VBox(10, table, operations, granting);
        box.setPadding(new Insets(10));
        TitledPane pane = new TitledPane("Перечень прав текущего пользователя", box);
        pane.setCollapsible(false);
        return pane;
    }

    private TitledPane buildLog() {
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setPrefHeight(160);
        area.setStyle("-fx-font-family: '" + Config.MONO_FONT + "';");
        area.textProperty().bind(vm.logProperty());
        area.textProperty().addListener((obs, old, value) -> area.setScrollTop(Double.MAX_VALUE));
        VBox.setVgrow(area, Priority.ALWAYS);
        TitledPane pane = new TitledPane("Журнал операций", area);
        pane.setCollapsible(false);
        return pane;
    }

    private Label buildStatus() {
        Label status = new Label();
        status.textProperty().bind(vm.messageProperty());
        status.setWrapText(true);
        status.styleProperty().bind(Bindings.when(vm.lastOperationFailedProperty())
                .then(Config.STYLE_ERROR).otherwise(Config.STYLE_OK));
        return status;
    }
}
