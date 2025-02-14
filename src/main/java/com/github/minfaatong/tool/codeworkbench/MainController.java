package com.github.minfaatong.tool.codeworkbench;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import lombok.Getter;

@Getter
public class MainController {
    @FXML
    private TextField tfUrl;
    @FXML
    private TextField tfShortName;
    @FXML
    private TextArea taLogConsole;
    @FXML
    private TextField tfCurrentProjectPath;
    @FXML
    private Button btnOpenInTerm;
    @FXML
    private Button btnOpenInIDE;
    @FXML
    private Button btnClone;
    @FXML
    private GridPane grid;
    @FXML
    private MenuItem miSettings;
    @FXML
    private MenuItem miExit;
    @FXML
    private Button btnBuildInTerm;
}
