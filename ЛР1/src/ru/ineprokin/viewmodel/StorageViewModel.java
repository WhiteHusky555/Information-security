package ru.ineprokin.viewmodel;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ru.ineprokin.Config;
import ru.ineprokin.model.StorageBrowser;
import ru.ineprokin.model.TableData;

public class StorageViewModel {

    private final StorageBrowser browser = new StorageBrowser();

    private final ObservableList<String> tables = FXCollections.observableArrayList();
    private final ObjectProperty<String> selectedTable = new SimpleObjectProperty<>(Config.TABLE_USERS);
    private final ReadOnlyObjectWrapper<TableData> data = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyStringWrapper info = new ReadOnlyStringWrapper();
    private final ReadOnlyStringWrapper error = new ReadOnlyStringWrapper("");

    public StorageViewModel() {
        selectedTable.addListener((obs, old, value) -> load());
        refresh();
    }

    public ObservableList<String> tables() {
        return tables;
    }

    public ObjectProperty<String> selectedTableProperty() {
        return selectedTable;
    }

    public ReadOnlyObjectProperty<TableData> dataProperty() {
        return data.getReadOnlyProperty();
    }

    public ReadOnlyStringProperty infoProperty() {
        return info.getReadOnlyProperty();
    }

    public ReadOnlyStringProperty errorProperty() {
        return error.getReadOnlyProperty();
    }

    public void refresh() {
        try {
            tables.setAll(browser.tables());
            if (!tables.contains(selectedTable.get())) {
                selectedTable.set(tables.isEmpty() ? null : tables.get(0));
            }
            load();
        } catch (RuntimeException e) {
            error.set(e.getMessage());
        }
    }

    private void load() {
        String table = selectedTable.get();
        if (table == null) {
            data.set(null);
            info.set("");
            return;
        }
        try {
            TableData loaded = browser.read(table);
            data.set(loaded);
            info.set("Файл базы: " + Config.DB_FILE + " · таблица «" + loaded.table()
                    + "» · полей: " + loaded.columns().size() + " · записей: " + loaded.rows().size());
            error.set("");
        } catch (RuntimeException e) {
            error.set(e.getMessage());
        }
    }
}
