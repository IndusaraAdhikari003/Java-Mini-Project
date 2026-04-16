module com.example.javaminiproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires org.controlsfx.controls;

    opens com.example.javaminiproject to javafx.fxml;
    opens com.example.javaminiproject.controller to javafx.fxml;
    opens com.example.javaminiproject.model to javafx.base;

    exports com.example.javaminiproject;
    exports com.example.javaminiproject.controller;
    exports com.example.javaminiproject.model;
    exports com.example.javaminiproject.dao;
    exports com.example.javaminiproject.util;
}