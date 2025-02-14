package com.github.minfaatong.tool.codeworkbench.handler;

import com.github.minfaatong.tool.codeworkbench.config.Config;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static com.github.minfaatong.tool.codeworkbench.utils.GuiUtils.printProcessOutput;
import static com.github.minfaatong.tool.codeworkbench.utils.NotificationUiUtils.showErrorMessage;

@Slf4j
@RequiredArgsConstructor
public class IDEButtonClickedEventEventHandler implements EventHandler<ActionEvent> {
    final TextField tfCurrentProjectPath;
    final Config config;
    private final TextArea taLogConsole;

    @Override
    public void handle(ActionEvent actionEvent) {
        openProjectInIntelliJ(config.getAppConfig().getIdeExePath(), tfCurrentProjectPath.getText());
    }

    // Step 5: Open the project in a new window in IntelliJ IDEA
    protected void openProjectInIntelliJ(String ideExecutablePath, String localPath) {
        // Open the project in a new window in IntelliJ IDEA
        // You would need to implement this part based on your specific setup
        try {
            final String command = String.format("\"%s\" %s", ideExecutablePath, localPath);
            Process process = Runtime.getRuntime().exec(command);

            printProcessOutput(process, taLogConsole);

            final int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("command exit abnormally with code {}", exitCode);
            }
        } catch (IOException e) {
            log.error("Error while opening project with Intellij", e);
            showErrorMessage("Error Open Project", e);
            tfCurrentProjectPath.setText("");
        } catch (InterruptedException e) {
            log.error("Thread interrupted while opening project with Intellij", e);
            showErrorMessage("Error Open Project", e);
            tfCurrentProjectPath.setText("");

            /* Clean up whatever needs to be handled before interrupting  */
            Thread.currentThread().interrupt();
        }
    }
}
