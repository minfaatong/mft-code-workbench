package com.github.minfaatong.tool.codeworkbench;

import com.github.minfaatong.tool.codeworkbench.config.Config;
import com.github.minfaatong.tool.codeworkbench.handler.CloneButtonClickedEventEventHandler;
import com.github.minfaatong.tool.codeworkbench.handler.CmdButtonClickedEventEventHandler;
import com.github.minfaatong.tool.codeworkbench.handler.IDEButtonClickedEventEventHandler;
import com.github.minfaatong.tool.codeworkbench.listener.ProjectPathChangedListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static com.github.minfaatong.tool.codeworkbench.utils.NotificationUiUtils.showErrorMessage;

@Slf4j
public class GitCloneAndOpenProjectFX2 extends Application {

    private TextField tfUrl;
    private TextField tfShortName;
    private TextArea taLogConsole;

    private TextField tfCurrentProjectPath;

    private Config config = null;
    private Button btnOpenInIDE;
    private Button btnOpenInTerm;

    private Parent root;
    private GridPane grid;

    CloneButtonClickedEventEventHandler cloneButtonClickedEventEventHandler;

    @Override
    public void start(Stage primaryStage) throws IOException {
        log.info("UI Thread - {}", Platform.isFxApplicationThread() ? "UI Thread" : "Background Thread");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("git-clone-and-open-project.fxml"));
            root = loader.load();
            grid = (GridPane) root.lookup("#grid");

            tfUrl = (TextField) grid.lookup("#tfUrl");
            tfShortName = (TextField) grid.lookup("#tfShortName");
            tfCurrentProjectPath = (TextField) grid.lookup("#tfCurrentProjectPath");
            taLogConsole = (TextArea) grid.lookup("#taLogConsole");
            btnOpenInIDE = (Button) grid.lookup("#btnOpenInIDE");
            btnOpenInTerm = (Button) grid.lookup("#btnOpenInTerm");
            Button btnClone = (Button) grid.lookup("#btnClone");

            cloneButtonClickedEventEventHandler = new CloneButtonClickedEventEventHandler(
                    tfUrl, tfShortName, tfCurrentProjectPath, taLogConsole);
            btnClone.setOnAction(cloneButtonClickedEventEventHandler);

            btnOpenInIDE.setOnAction(new IDEButtonClickedEventEventHandler(
                    cloneButtonClickedEventEventHandler,
                    tfCurrentProjectPath,
                    config));

            btnOpenInTerm.setOnAction(new CmdButtonClickedEventEventHandler(
                    cloneButtonClickedEventEventHandler,
                    tfCurrentProjectPath,
                    config));

            tfCurrentProjectPath.textProperty().addListener(new ProjectPathChangedListener(
                    btnOpenInIDE, btnOpenInTerm));

            Scene scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Git Clone and Open Project");
            primaryStage.show();
        } catch (IOException e) {
            showErrorMessage("Error loading FXML file", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
