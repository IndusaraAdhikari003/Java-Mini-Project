package com.example.javaminiproject.controller;

import com.example.javaminiproject.MainApp;
import com.example.javaminiproject.dao.UserDAO;
import com.example.javaminiproject.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }

        try {
            User user = userDAO.login(username, password);
            if (user == null) {
                errorLabel.setText("Invalid username or password.");
                return;
            }
            openDashboard(user);

        } catch (Exception e) {
            errorLabel.setText("Connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openDashboard(User user) throws Exception {
        String fxml = switch (user.getRole()) {
            case "ADMIN"        -> "admin_dashboard.fxml";
            case "LECTURER"     -> "lecturer_dashboard.fxml";
            case "TECH_OFFICER" -> "techofficer_dashboard.fxml";
            default             -> "undergraduate_dashboard.fxml";
        };

        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxml));
        Scene scene = new Scene(loader.load(), 1100, 700);
        scene.getStylesheets().add(
                MainApp.class.getResource("styles.css").toExternalForm());

        // Get the com.example.javaminiproject.controller AFTER load, then pass the user
        switch (user.getRole()) {
            case "ADMIN" -> {
                AdminController ctrl = loader.getController();
                ctrl.setUser(user);
                ctrl.initialize();
            }
            case "LECTURER" -> {
                LecturerController ctrl = loader.getController();
                ctrl.setUser(user);
                ctrl.initialize();
            }
            case "TECH_OFFICER" -> {
                TechOfficerController ctrl = loader.getController();
                ctrl.setUser(user);
                ctrl.initialize();
            }
            default -> {
                UndergraduateController ctrl = loader.getController();
                ctrl.setUser(user);
                ctrl.initialize();
            }
        }

        Stage stage = MainApp.getStage();
        stage.setTitle("LMS — " + user.getDashboardTitle());
        stage.setScene(scene);
    }
}