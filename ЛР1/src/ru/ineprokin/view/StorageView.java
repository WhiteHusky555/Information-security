package ru.ineprokin.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ru.ineprokin.Config;
import ru.ineprokin.model.TableData;
import ru.ineprokin.viewmodel.StorageViewModel;

import java.util.List;

public class StorageView extends BorderPane {

    private final StorageViewModel vm;
    private final TableView<List<String>> table = new TableView<>();

    public StorageView(StorageViewModel viewModel) {
        this.vm = viewModel;
        setPadding(new Insets(12));
        setTop(buildControls());
        setCenter(table);
        table.setPlaceholder(new Label("Таблица пуста"));
        vm.dataProperty().addListener((obs, old, value) -> show(value));
        show(vm.dataProperty().get());
    }

    private VBox buildControls() {
        ComboBox<String> tables = new ComboBox<>(vm.tables());
        tables.valueProperty().bindBidirectional(vm.selectedTableProperty());

        Button refresh = new Button("Обновить");
        refresh.setOnAction(e -> vm.refresh());

        Label info = new Label();
        info.textProperty().bind(vm.infoProperty());
        Label error = new Label();
        error.textProperty().bind(vm.errorProperty());
        error.setStyle(Config.STYLE_ERROR);

        Label note = new Label("Пароли в базе не хранятся в открытом виде: сохраняются только "
                + "случайная соль и " + Config.HASH_ALGORITHM + "(соль + пароль).");
        note.setStyle(Config.STYLE_HINT);
        note.setWrapText(true);

        HBox controls = new HBox(8, new Label("Таблица:"), tables, refresh);
        VBox box = new VBox(6, controls, info, note, error);
        box.setPadding(new Insets(0, 0, 10, 0));
        return box;
    }

    private void show(TableData data) {
        table.getColumns().clear();
        table.getItems().clear();
        if (data == null) {
            return;
        }
        for (int i = 0; i < data.columns().size(); i++) {
            int index = i;
            TableColumn<List<String>, String> column = new TableColumn<>(data.columns().get(i));
            column.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().get(index)));
            column.setPrefWidth(data.columns().get(i).equals("hash") ? 420 : 140);
            table.getColumns().add(column);
        }
        table.getItems().setAll(data.rows());
    }
}
