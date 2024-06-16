package com.github.minfaatong.tool.codeworkbench.handler;

import com.github.minfaatong.tool.codeworkbench.config.Config;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

import static com.github.minfaatong.tool.codeworkbench.utils.NotificationUiUtils.showErrorMessage;

@Slf4j
@RequiredArgsConstructor
public class CmdButtonClickedEventEventHandler implements EventHandler<ActionEvent> {
    final CloneButtonClickedEventEventHandler cloneButtonClickedEventEventHandler;
    final TextField tfCurrentProjectPath;
    final Config config;

    @Override
    public void handle(ActionEvent actionEvent) {
        openProjectInTerminal(config.getAppConfig().getTerminalExePath(), tfCurrentProjectPath.getText());
    }


    protected void openProjectInTerminal(String terminalExePath, String workingPath) {
        File workingDir;
        ProcessBuilder pb = null;

        try {
            workingDir = new File(workingPath);
            pb = new ProcessBuilder(String.format("\"%s\"", terminalExePath));
            pb.directory(workingDir);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            cloneButtonClickedEventEventHandler.printProcessOutput(process);

            final int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("command exit abnormally with code {}", exitCode);
            }
        } catch (IOException e) {
            log.error("Error while open project in terminal \"{}\"",
                    (pb != null) ? pb.command() : "<null_cmd>", e);
            tfCurrentProjectPath.setText("");
            showErrorMessage("Error open project in terminal", e);
        } catch (InterruptedException e) {
            log.error("Thread interrupted while open project in terminal \"{}\"",
                    (pb != null) ? pb.command() : "<null_cmd>", e);
            tfCurrentProjectPath.setText("");
            showErrorMessage("Error open project in terminal", e);

            /* Clean up whatever needs to be handled before interrupting  */
            Thread.currentThread().interrupt();
        }
    }
}