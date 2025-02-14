package com.github.minfaatong.tool.codeworkbench.handler;

import com.github.minfaatong.tool.codeworkbench.config.Config;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.minfaatong.tool.codeworkbench.utils.GuiUtils.printProcessOutput;
import static com.github.minfaatong.tool.codeworkbench.utils.NotificationUiUtils.showErrorMessage;

@Slf4j
@RequiredArgsConstructor
public class CmdBuildButtonClickedEventEventHandler implements EventHandler<ActionEvent> {
    final TextField tfCurrentProjectPath;
    final Config config;
    private final TextArea taLogConsole;

    @Override
    public void handle(ActionEvent actionEvent) {
        buildProjectInTerminal(config.getAppConfig().getTerminalExePath(), tfCurrentProjectPath.getText());
    }

    protected void buildProjectInTerminal(String terminalExePath, String workingPath) {
        File workingDir;
        ProcessBuilder pb = null;

        try {
            workingDir = new File(workingPath);

            final List<String> command = constructBuildCommandInTerminal(terminalExePath, workingDir);
            pb = new ProcessBuilder(command);
            pb.directory(workingDir);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            printProcessOutput(process, taLogConsole);

            final int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("command exit abnormally with code {}", exitCode);
            }
        } catch (IOException e) {
            log.error("Error while open project in terminal \"{}\"",
                    (pb != null) ? pb.command() : "<null_cmd>", e);
            tfCurrentProjectPath.setText("");
            showErrorMessage("Error building project in terminal", e);
        } catch (InterruptedException e) {
            log.error("Thread interrupted while building project in terminal \"{}\"",
                    (pb != null) ? pb.command() : "<null_cmd>", e);
            tfCurrentProjectPath.setText("");
            showErrorMessage("Error building project in terminal", e);

            // Clean up whatever needs to be handled before interrupting
            Thread.currentThread().interrupt();
        }
    }

    private List<String> constructBuildCommandInTerminal(String terminalExePath, File workingDir) {
        List<String> command = new ArrayList<>();
        command.add(terminalExePath);
        command.add("--window"); // Changed from "-w 0"
        command.add("0");
        command.add("--");  // Separator for wt.exe arguments
        command.add("cmd");
        command.add("/c");
        command.add("cd /d " + workingDir + " && " +
                "mvn verify -U -V -B -P hsbc-configuration,repo " +
                "--global-toolchains " + config.getAppConfig().getToolchainPath() + " " +
//                    "-s " + config.getAppConfig().getMavenSettingsPath() + " " +
//                    "-Dmaven.repo.local=" + config.getAppConfig().getMavenRepositoryPath() + " " +
                "-Djavax.net.ssl.trustStore=" + config.getAppConfig().getTrustStorePath() + " " +
                "-Djavax.net.ssl.trustStorePassword=" + config.getAppConfig().getTrustStorePass());
        return command;
    }
}