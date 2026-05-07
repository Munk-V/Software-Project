package com.planner.ui;
// Nat

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        AppContext context = new AppContext();
        MainWindow mainWindow = new MainWindow(context);
        Scene scene = new Scene(mainWindow.getRoot(), 900, 600);
        stage.setTitle("Software Project Planner");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
