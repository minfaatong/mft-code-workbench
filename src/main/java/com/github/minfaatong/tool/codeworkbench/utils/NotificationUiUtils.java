package com.github.minfaatong.tool.codeworkbench.utils;

import javafx.scene.control.Alert;

public class NotificationUiUtils {
    public static void showErrorMessage(String title, Exception e) {
        showErrorMessage(title, e.getLocalizedMessage());
    }

    public static void showErrorMessage(String title, String details) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(details);
        alert.showAndWait();
    }

    public static void showSuccessMessage() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Project cloned and opened successfully!");
        alert.showAndWait();
    }
}