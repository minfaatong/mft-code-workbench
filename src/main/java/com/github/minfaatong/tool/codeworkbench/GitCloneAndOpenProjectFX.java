package com.github.minfaatong.tool.codeworkbench;

import com.github.minfaatong.tool.codeworkbench.config.Config;
import com.github.minfaatong.tool.codeworkbench.handler.CloneButtonClickedEventEventHandler;
import com.github.minfaatong.tool.codeworkbench.handler.CmdBuildButtonClickedEventEventHandler;
import com.github.minfaatong.tool.codeworkbench.handler.CmdButtonClickedEventEventHandler;
import com.github.minfaatong.tool.codeworkbench.handler.IDEButtonClickedEventEventHandler;
import com.github.minfaatong.tool.codeworkbench.listener.GitUrlParserListener;
import com.github.minfaatong.tool.codeworkbench.listener.ProjectPathChangedListener;
import com.github.minfaatong.tool.codeworkbench.listener.URLChangeListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

import static com.github.minfaatong.tool.codeworkbench.utils.NotificationUiUtils.showErrorMessage;
import static com.github.minfaatong.tool.codeworkbench.utils.ProjectMapConfigReader.readAppConfig;

@Slf4j
public class GitCloneAndOpenProjectFX extends Application implements GitUrlParserListener {
    MainController controller = null;

    CloneButtonClickedEventEventHandler cloneButtonClickedEventEventHandler;

    @Override
    public void start(Stage primaryStage) {
        log.info("UI Thread - {}", Platform.isFxApplicationThread() ? "UI Thread" : "Background Thread");

        try {
            URL fxmlUrl = getClass().getResource("fxml/git-clone-and-open-project.fxml");
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(fxmlUrl);
            Parent root = loader.load();

            controller = loader.getController();
            Config config = null;

            try {
                config = readAppConfig("config.yml");
            } catch (FileNotFoundException e) {
                log.warn("Error while reading app config (from 'config.yml' in current working path)", e);
            }

            if (config == null) {
                log.info("reading from 'configs/config.yml' in current working path instead");
                try {
                    config = readAppConfig("configs/config.yml");
                } catch (FileNotFoundException e) {
                    log.warn("Error while reading app config (from 'config/config.yml' in current working path)", e);
                }
            }
            if (config == null) {
                log.info("reading 'config.yml' from default classpath instead");
                try {
                    config = readAppConfig(GitCloneAndOpenProjectFX.class.getClassLoader().getResource("config.yml").getPath());
                } catch (FileNotFoundException e) {
                    log.error("Error while reading app config (from default 'classpath://config.yml' path)", e);
                }
            }

            controller.getMiExit().setOnAction(e -> {
                Platform.exit();
                System.exit(0);
            });

            controller.getMiSettings().setOnAction(e -> log.info("'Settings' clicked ..."));

            controller.getTfUrl().textProperty().addListener(new URLChangeListener(this));

            controller.getBtnOpenInIDE().setDisable(true);
            controller.getBtnOpenInTerm().setDisable(true);
            controller.getBtnBuildInTerm().setDisable(true);

            cloneButtonClickedEventEventHandler = new CloneButtonClickedEventEventHandler(
                    controller.getTfUrl(), controller.getTfShortName(),
                    controller.getTfCurrentProjectPath(),
                    controller.getTaLogConsole());
            controller.getBtnClone().setOnAction(cloneButtonClickedEventEventHandler);

            controller.getBtnOpenInIDE().setOnAction(new IDEButtonClickedEventEventHandler(
                    controller.getTfCurrentProjectPath(),
                    config,
                    controller.getTaLogConsole()));
            controller.getBtnOpenInTerm().setOnAction(new CmdButtonClickedEventEventHandler(
                    controller.getTfCurrentProjectPath(),
                    config,
                    controller.getTaLogConsole()));
            controller.getBtnBuildInTerm().setOnAction(new CmdBuildButtonClickedEventEventHandler(
                    controller.getTfCurrentProjectPath(),
                    config,
                    controller.getTaLogConsole()));

            controller.getTfCurrentProjectPath().textProperty().addListener(
                    new ProjectPathChangedListener(
                            controller.getBtnOpenInIDE(), controller.getBtnOpenInTerm(), controller.getBtnBuildInTerm()));

            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(this.getClass()
                            .getResource("/stylesheets/console.css"))
                    .toExternalForm());

            controller.getTaLogConsole()
                    .prefWidthProperty().bind(scene.widthProperty()
                            .subtract(controller.getBtnOpenInIDE().widthProperty().multiply(5)));
            controller.getTaLogConsole()
                    .prefHeightProperty().bind(
                            scene.heightProperty()
                                    .subtract(controller.getBtnOpenInIDE().heightProperty()));

            InputStream isIcon = Objects.requireNonNull(GitCloneAndOpenProjectFX.class.getResourceAsStream("fxml/my-assistant-logo.png"));
            primaryStage.getIcons().add(new Image(isIcon));
            primaryStage.setScene(scene);
            primaryStage.setTitle("Git Clone and Open Project");
            primaryStage.show();
        } catch (IOException e) {
            log.error("Error while creating form", e);
            showErrorMessage("Error loading FXML file", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void gitUrlParsed(String host, String organization, String repository, String branch, String shortName) {
        log.info("branch='{}', shortName='{}'", branch, shortName);
        if (StringUtils.isEmpty(controller.getTfShortName().getText())) {
            controller.getTfShortName().setText(shortName);
        }
    }
}