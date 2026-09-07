package ru.ineprokin.view;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import ru.ineprokin.Config;
import ru.ineprokin.model.AlphabetOption;
import ru.ineprokin.model.SpeedUnit;
import ru.ineprokin.model.TimeUnit;
import ru.ineprokin.util.Fmt;
import ru.ineprokin.viewmodel.StrengthViewModel;

public class StrengthView extends BorderPane {

    private final StrengthViewModel vm;

    public StrengthView(StrengthViewModel viewModel) {
        this.vm = viewModel;
        setPadding(new Insets(12));
        setTop(buildInputs());
        setCenter(buildTable());
        setBottom(buildGenerator());
    }

    private TitledPane buildInputs() {
        TextField pField = new TextField();
        TextField speedField = new TextField();
        TextField timeField = new TextField();
        pField.setPrefColumnCount(6);
        speedField.setPrefColumnCount(4);
        timeField.setPrefColumnCount(4);
        pField.textProperty().bindBidirectional(vm.pProperty());
        speedField.textProperty().bindBidirectional(vm.speedProperty());
        timeField.textProperty().bindBidirectional(vm.timeProperty());

        ComboBox<SpeedUnit> speedUnit = new ComboBox<>();
        speedUnit.getItems().setAll(SpeedUnit.values());
        speedUnit.valueProperty().bindBidirectional(vm.speedUnitProperty());
        ComboBox<TimeUnit> timeUnit = new ComboBox<>();
        timeUnit.getItems().setAll(TimeUnit.values());
        timeUnit.valueProperty().bindBidirectional(vm.timeUnitProperty());

        Button calculate = new Button("Рассчитать");
        calculate.setOnAction(e -> vm.calculate());
        calculate.setDefaultButton(true);

        HBox inputs = new HBox(8,
                new Label("P:"), pField,
                new Label("V:"), speedField, speedUnit,
                new Label("T:"), timeField, timeUnit,
                calculate);
        inputs.setPadding(new Insets(10));

        Label attempts = new Label();
        attempts.textProperty().bind(vm.attemptsTextProperty());
        Label bound = new Label();
        bound.textProperty().bind(vm.boundTextProperty());
        bound.setStyle(Config.STYLE_INFO);
        Label error = new Label();
        error.textProperty().bind(vm.errorTextProperty());
        error.setStyle(Config.STYLE_ERROR);

        VBox box = new VBox(6, inputs, attempts, bound, error);
        box.setPadding(new Insets(0, 10, 10, 10));
        TitledPane pane = new TitledPane("Исходные данные варианта " + Config.VARIANT
                + ": P = 10^-4, V = 10 паролей/мин, T = 3 недели", box);
        pane.setCollapsible(false);
        return pane;
    }

    private TitledPane buildTable() {
        TableView<AlphabetOption> table = new TableView<>(vm.options());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<AlphabetOption, String> nameColumn = new TableColumn<>("Алфавит");
        nameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().alphabet().toString()));
        nameColumn.setPrefWidth(340);
        TableColumn<AlphabetOption, Number> powerColumn = new TableColumn<>("A");
        powerColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().alphabet().power()));
        TableColumn<AlphabetOption, Number> lengthColumn = new TableColumn<>("L");
        lengthColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().length()));
        TableColumn<AlphabetOption, String> totalColumn = new TableColumn<>("S = A^L");
        totalColumn.setCellValueFactory(c -> new SimpleStringProperty(Fmt.sci(c.getValue().total())));
        TableColumn<AlphabetOption, String> probabilityColumn = new TableColumn<>("P = V·T / S");
        probabilityColumn.setCellValueFactory(c -> new SimpleStringProperty(Fmt.sci(c.getValue().probability())));
        table.getColumns().add(nameColumn);
        table.getColumns().add(powerColumn);
        table.getColumns().add(lengthColumn);
        table.getColumns().add(totalColumn);
        table.getColumns().add(probabilityColumn);

        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, value) -> vm.selectedProperty().set(value));
        vm.selectedProperty().addListener((obs, old, value) -> table.getSelectionModel().select(value));
        table.getSelectionModel().select(vm.selectedProperty().get());

        TitledPane pane = new TitledPane("Подбор A и L при условии S* ≤ A^L (выберите строку)", table);
        pane.setCollapsible(false);
        BorderPane.setMargin(pane, new Insets(10, 0, 10, 0));
        return pane;
    }

    private VBox buildGenerator() {
        Label choice = new Label();
        choice.textProperty().bind(vm.choiceTextProperty());
        choice.setStyle(Config.STYLE_OK);

        Spinner<Integer> count = new Spinner<>();
        count.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                1, Config.MAX_PASSWORD_COUNT, Config.DEFAULT_PASSWORD_COUNT));
        count.setPrefWidth(80);
        count.valueProperty().addListener((obs, old, value) -> vm.countProperty().set(value));

        Button generate = new Button("Сгенерировать пароли");
        generate.setOnAction(e -> vm.generate());

        ListView<String> passwords = new ListView<>(vm.passwords());
        passwords.setStyle("-fx-font-family: '" + Config.MONO_FONT + "'; -fx-font-size: 14px;");
        passwords.setPrefHeight(160);
        VBox.setVgrow(passwords, Priority.ALWAYS);

        HBox controls = new HBox(8, new Label("Количество паролей:"), count, generate);
        VBox box = new VBox(8, choice, controls, passwords);
        return box;
    }
}
