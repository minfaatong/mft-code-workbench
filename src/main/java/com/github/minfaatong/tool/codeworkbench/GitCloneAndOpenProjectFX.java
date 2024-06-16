package com.github.minfaatong.tool.codeworkbench;

import com.github.minfaatong.tool.codeworkbench.config.Config;
import com.github.minfaatong.tool.codeworkbench.handler.CloneButtonClickedEventEventHandler;
import com.github.minfaatong.tool.codeworkbench.handler.CmdButtonClickedEventEventHandler;
import com.github.minfaatong.tool.codeworkbench.handler.IDEButtonClickedEventEventHandler;
import com.github.minfaatong.tool.codeworkbench.listener.ProjectPathChangedListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Objects;

@Slf4j
public class GitCloneAndOpenProjectFX extends Application {

    CloneButtonClickedEventEventHandler cloneButtonClickedEventEventHandler;

    @Override
    public void start(Stage primaryStage) {
        log.info("UI Thread - {}", Platform.isFxApplicationThread() ? "UI Thread" : "Background Thread");
        Config config = readConfig("src/main/resources/config.yml");

        primaryStage.setTitle("Git Clone and Open Project");

        GridPane grid = new GridPane();
        TextField tfUrl = new TextField();
        TextField tfShortName = new TextField();
        TextField tfCurrentProjectPath = new TextField();
        TextArea taLogConsole = new TextArea();
        grid.setPadding(new Insets(8, 8, 8, 8));
        grid.setVgap(5);
        grid.setHgap(5);
        Label lblUrl = new Label("URL:");
        GridPane.setConstraints(lblUrl, 0, 0);
        GridPane.setConstraints(tfUrl, 1, 0);

        Label lblShortName = new Label("Shortname:");
        GridPane.setConstraints(lblShortName, 0, 1);
        GridPane.setConstraints(tfShortName, 1, 1);

        Button btnClone = new Button("Clone");
        GridPane.setConstraints(btnClone, 0, 2);
        cloneButtonClickedEventEventHandler = new CloneButtonClickedEventEventHandler(
                tfUrl, tfShortName, tfCurrentProjectPath, taLogConsole, config);
        btnClone.setOnAction(cloneButtonClickedEventEventHandler);

        Button btnOpenInIDE = new Button("IDE");
        ImageView imgIDE = new ImageView(Objects.requireNonNull(getClass().getResource("fxml/IntelliJ_IDEA_Icon.png")).toString());
        imgIDE.setFitWidth(30);
        imgIDE.setFitHeight(30);
        btnOpenInIDE.setGraphic(imgIDE);
        btnOpenInIDE.setDisable(true);
        GridPane.setConstraints(btnOpenInIDE, 2, 3);
        btnOpenInIDE.setOnAction(new IDEButtonClickedEventEventHandler(
                cloneButtonClickedEventEventHandler,
                tfCurrentProjectPath,
                config));

        Button btnOpenInTerm = new Button("TERM");
        ImageView imgTerm = new ImageView(Objects.requireNonNull(getClass().getResource("fxml/cmd-terminal-icon.png")).toString());
        imgTerm.setFitWidth(30);
        imgTerm.setFitHeight(30);
        btnOpenInTerm.setGraphic(imgTerm);
        btnOpenInTerm.setDisable(true);
        GridPane.setConstraints(btnOpenInTerm, 3, 3);
        btnOpenInTerm.setOnAction(new CmdButtonClickedEventEventHandler(
                cloneButtonClickedEventEventHandler,
                tfCurrentProjectPath,
                config));

        Label lblCurrProj = new Label("Current Project:");
        GridPane.setConstraints(lblCurrProj, 0, 3);
        tfCurrentProjectPath.setEditable(false);
        tfCurrentProjectPath.textProperty().addListener(new ProjectPathChangedListener(
                btnOpenInIDE, btnOpenInTerm));
        GridPane.setConstraints(tfCurrentProjectPath, 1, 3);

        Label lblConsole = new Label("Console:");
        GridPane.setConstraints(lblConsole, 1, 4);
        taLogConsole.setEditable(false);
        taLogConsole.setWrapText(true);
        taLogConsole.setMinHeight(200.0);
        GridPane.setConstraints(taLogConsole, 1, 5);

        // create a menu
        MenuBar menuBar = new MenuBar();
        Menu mnFile = new Menu("File");
        MenuItem miSettings = new MenuItem("Settings");
        miSettings.setOnAction(t -> log.info("Setting menu pressed"));
        mnFile.getItems().add(miSettings);
        menuBar.getMenus().addAll(mnFile);

        grid.getChildren().addAll(lblUrl, tfUrl, lblShortName,
                tfShortName, btnClone,
                btnOpenInIDE, btnOpenInTerm, lblCurrProj, tfCurrentProjectPath,
                lblConsole, taLogConsole);


        VBox vbox = new VBox(menuBar);
        vbox.setPadding(new Insets(8, 8, 8, 8));
        vbox.getChildren().addAll(grid);

        Scene scene = new Scene(vbox, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    protected Config readConfig(String configPath) {
        Yaml yaml = new Yaml();
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(configPath);
        } catch (FileNotFoundException e) {
            log.error("Error while reading app config - {}", configPath, e);
        }
        return yaml.loadAs(fileInputStream, Config.class);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
