package com.github.minfaatong.tool.codeworkbench.handler;

import com.github.minfaatong.tool.codeworkbench.FolderPrompt;
import com.github.minfaatong.tool.codeworkbench.config.Config;
import com.github.minfaatong.tool.codeworkbench.config.ProjectMapConfig;
import com.github.minfaatong.tool.codeworkbench.utils.FileUtils;
import com.github.minfaatong.tool.codeworkbench.utils.ProjectMapConfigReader;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.github.minfaatong.tool.codeworkbench.FolderPrompt.cancelButton;
import static com.github.minfaatong.tool.codeworkbench.FolderPrompt.createButton;
import static com.github.minfaatong.tool.codeworkbench.utils.GitUtils.extractRepoAndBranch;
import static com.github.minfaatong.tool.codeworkbench.utils.NotificationUiUtils.showErrorMessage;
import static com.github.minfaatong.tool.codeworkbench.utils.NotificationUiUtils.showMessage;
import static com.github.minfaatong.tool.codeworkbench.utils.ProjectMapConfigReader.getProjectConfig;

@Slf4j
@RequiredArgsConstructor
public class CloneButtonClickedEventEventHandler implements EventHandler<ActionEvent> {

    private final TextField tfUrl;
    private final TextField tfShortName;
    private final TextField tfCurrentProjectPath;
    private final TextArea taLogConsole;
    private Config config;

    @Override
    public void handle(ActionEvent event) {
        // clear project path
        tfCurrentProjectPath.setText("");
        final String url = StringUtils.isNotEmpty(tfUrl.getText()) ? tfUrl.getText() : config.getAppConfig().getDefaultRepo();
        final String shortName = StringUtils.isNotEmpty(tfShortName.getText()) ? tfShortName.getText() : config.getAppConfig().getDefaultShortName();

        // Step 1: Extract the repository name and branch name from the URL
        final String[] repoAndBranch = extractRepoAndBranch(url);
        final String host = repoAndBranch[0];
        final String orgName = repoAndBranch[1];

        final String repoName = repoAndBranch[2];
        final String branchName = repoAndBranch[3];

        // Step 2: Clone the repository using the extracted repository name and branch name
        URL urlGit = null;

        try {
            urlGit = new URL(url);
        } catch (MalformedURLException e) {
            log.error("Error while parsing git branch url", e);
            showErrorMessage("Git operation error", e);
        }
        final String cloneUrl = String.format("%s://%s/%s/%s.git", urlGit.getProtocol(), urlGit.getHost(), orgName, repoName);
        log.info("organization name = {}", orgName);
        log.info("repository name = {}", repoName);
        log.info("git url = {}", cloneUrl);
        String gitPath =  ProjectMapConfigReader.deriveLocalGitPathByGitUrl(cloneUrl);
        String localPath = ProjectMapConfigReader.deriveLocalWorkingPath(cloneUrl, shortName);
        log.info("git path = {}", gitPath);

        if (gitPath == null) {
            ProjectMapConfig config = getProjectConfig();
            String gitRootDir = config.getRootDir();
            String altFolderName = new StringBuffer(gitRootDir).append("/")
                    .append(orgName).append("/")
                    .append(repoName).append("/")
                    .append("br").toString();
            gitPath = altFolderName;
            log.info("git path = '{}' (proposed)", altFolderName);

            String promptMsg = String.format("Git project not exists, create one instead? '%s'", altFolderName);
            Alert gitFolderPrompt = FolderPrompt.showFolderPrompt(promptMsg, altFolderName);

            Optional<ButtonType> result = gitFolderPrompt.showAndWait();
            if (result.orElse(cancelButton) == createButton) {
                java.io.File dir = new java.io.File(altFolderName);
                if (!dir.exists()) {
                    dir.mkdirs();
                    log.info("git path '{}' created", gitPath);
                }
            }
        }

        if (localPath == null) {
            ProjectMapConfig config = getProjectConfig();
            String workDir = config.getWorkDir();
            String altFolderName = new StringBuffer(workDir).append("/")
                    .append(repoName).append("_").append(shortName).toString();
            localPath = altFolderName;
            log.info("working path = '{}' (proposed)", altFolderName);
        }

        try {
            verifyShortNameNotExists(String.format("%s/%s", gitPath, shortName), localPath);
            final String gitClonePath = cloneRepo(gitPath, host, orgName, repoName, branchName, shortName);
            log.info("working path = {}", localPath);

            // Step 3: Download the project into the specified local path
            copyProjectToWorkingSpace(gitClonePath, localPath);
            tfCurrentProjectPath.setText(localPath);

            // Show success message
            showMessage(null, "Project cloned and opened successfully!");
        } catch (IllegalArgumentException e) {
            showErrorMessage("Validation Error", e);
        }
    }

    private void verifyShortNameNotExists(String gitPath, String localPath) throws IllegalArgumentException {
        if (Files.exists(Path.of(gitPath))) {
            throw new IllegalArgumentException(String.format("Git path '%s' already exists, choose another short name", gitPath));
        }
        if (Files.exists(Path.of(localPath))) {
            throw new IllegalArgumentException(String.format("Working path '%s' already exists, choose another short name", localPath));
        }
    }

    // Step 2: Clone the repository using the extracted repository name and branch name
    protected String cloneRepo(String gitPath, String gitHost, String orgName, String repoName, String branchName, String shortName) {
        final String cloneUrl = String.format("git@%s:%s/%s.git", gitHost, orgName, repoName);
        File workingDir;
        ProcessBuilder pb = null;

        try {
            workingDir = new File(gitPath);
            pb = new ProcessBuilder("git", "clone", "--filter=blob:none", cloneUrl, "-b", branchName, shortName);
            pb.directory(workingDir);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            printProcessOutput(process);

            final int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("command exit abnormally with code {}", exitCode);
                throw new IllegalStateException(String.format("Failure to run git clone repo command with exit code - %d", exitCode));
            }
        } catch (IOException e) {
            log.error("Error while git cloning repository \"{}\"",
                    (pb != null) ? pb.command(): "<null_cmd>", e);
            tfCurrentProjectPath.setText("");
            showErrorMessage("Error cloning project", e);
        } catch (InterruptedException e) {
            log.error("Thread interrupted while git cloning repository \"{}\"",
                    (pb != null) ? pb.command(): "<null_cmd>", e);
            tfCurrentProjectPath.setText("");
            showErrorMessage("Error cloning project", e);

            /* Clean up whatever needs to be handled before interrupting  */
            Thread.currentThread().interrupt();
        }
        return String.format("%s%s%s", gitPath, File.separator, shortName);
    }

    // Step 3: Download the project into the specified local path
    protected void copyProjectToWorkingSpace(String sourcePath, String destPath) {
        final Path mSourcePath = Paths.get(sourcePath);
        final Path mDestPath = Paths.get(destPath);
        log.info("Copying from \"{}\" to \"{}\" ...", mSourcePath, mDestPath);
        try {
            FileUtils.copyDirectory(mSourcePath.toFile(), mDestPath.toFile());
        } catch (IOException e) {
            log.error("Error while downloading project from \"{}\" to \"{}\"",
                    sourcePath, destPath, e);
            tfCurrentProjectPath.setText("");
            showErrorMessage("Error copy project", e);
        }
    }

    public void printProcessOutput(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            log.info(line);
            final String finalLine = line;
            Platform.runLater(()-> taLogConsole.appendText(System.lineSeparator() + finalLine));
        }
    }
}