package com.github.minfaatong.tool.codeworkbench;

import javafx.scene.control.*;

public class FolderPrompt {
    public static ButtonType createButton = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
    public static ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

    public static Alert showFolderPrompt(String promptMessage, String folderName) {
        Alert alert = new Alert(Alert.AlertType.WARNING,
                promptMessage,
                createButton,
                cancelButton);
        alert.setTitle("Auto folder creation?");
        return alert;
    }
}
