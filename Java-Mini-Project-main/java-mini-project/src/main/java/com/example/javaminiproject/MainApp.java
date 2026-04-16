package com.example.javaminiproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        showLogin();
    }

    public static void showLogin() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("login.fxml"));
        Scene scene = new Scene(loader.load(), 900, 600);
        scene.getStylesheets().add(
                MainApp.class.getResource("styles.css").toExternalForm());
        primaryStage.setTitle("Faculty of Technology — LMS");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    public static void showScene(String fxmlFile, String title) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource(fxmlFile));
        Scene scene = new Scene(loader.load(), 1100, 700);
        scene.getStylesheets().add(
                MainApp.class.getResource("styles.css").toExternalForm());
        primaryStage.setTitle("LMS — " + title);
        primaryStage.setScene(scene);
    }

    public static Stage getStage() { return primaryStage; }

    public static void main(String[] args) {
        launch(args);
    }
}