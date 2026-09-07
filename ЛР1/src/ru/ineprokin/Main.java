package ru.ineprokin;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.ineprokin.view.MainView;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle(Config.TITLE);
        stage.setScene(new Scene(new MainView(), Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
