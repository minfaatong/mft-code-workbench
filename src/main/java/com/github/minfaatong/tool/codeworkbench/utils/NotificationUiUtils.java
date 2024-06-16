package com.github.minfaatong.tool.codeworkbench.utils;

import javafx.scene.control.Alert;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NotificationUiUtils {

    public static void showErrorMessage(String title, Exception e) {
        showErrorMessage(title, e.getLocalizedMessage());
    }

    public static void showErrorMessage(String title, String details) {
        log.warn(details);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(details);
        alert.showAndWait();
    }

    public static void showMessage(String title, String detail) {
        log.info(detail);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Message");
        alert.setHeaderText(title);
        alert.setContentText(detail);
        alert.showAndWait();
    }
}