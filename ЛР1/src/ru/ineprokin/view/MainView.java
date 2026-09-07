package ru.ineprokin.view;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import ru.ineprokin.viewmodel.AuthViewModel;
import ru.ineprokin.viewmodel.StorageViewModel;
import ru.ineprokin.viewmodel.StrengthViewModel;

public class MainView extends TabPane {

    public MainView() {
        StorageViewModel storageViewModel = new StorageViewModel();
        Tab storageTab = new Tab("Хранилище (SQLite)", new StorageView(storageViewModel));

        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        getTabs().addAll(
                new Tab("Часть 1. Простой пароль", new AuthView(new AuthViewModel())),
                new Tab("Часть 2. Оценка стойкости", new StrengthView(new StrengthViewModel())),
                storageTab);

        storageTab.setOnSelectionChanged(e -> {
            if (storageTab.isSelected()) {
                storageViewModel.refresh();
            }
        });
    }
}
