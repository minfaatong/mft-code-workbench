package com.github.minfaatong.tool.codeworkbench.handler;

import com.github.minfaatong.tool.codeworkbench.config.Config;
import com.github.minfaatong.tool.codeworkbench.utils.FileUtils;
import com.github.minfaatong.tool.codeworkbench.utils.ProjectMapConfigReader;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.minfaatong.tool.codeworkbench.utils.NotificationUiUtils.showErrorMessage;
import static com.github.minfaatong.tool.codeworkbench.utils.NotificationUiUtils.showSuccessMessage;

@Slf4j
@RequiredArgsConstructor
public class CloneButtonClickedEventEventHandler implements EventHandler<ActionEvent> {
    public static final String REGEX_GIT_PATH_BOTH = "^\\/[a-zA-Z0-9\\-]*\\/[a-zA-Z0-9\\-]*(\\/tree\\/[a-zA-Z0-9\\-\\/]*[a-zA-Z0-9\\-]*)?";
    public static final String REGEX_GIT_PATH_WITH_BRANCH = "^\\/[a-zA-Z0-9\\-]*\\/[a-zA-Z0-9\\-]*\\/tree\\/[a-zA-Z0-9\\-\\/]*[a-zA-Z0-9\\-]*";
    public static final String REGEX_MASTER_GIT_PATH = "^\\/[a-zA-Z0-9\\-]*\\/[a-zA-Z0-9\\-]*";

    private final TextField tfUrl;
    private final TextField tfShortName;
    private final TextField tfCurrentProjectPath;
    private final TextArea taLogConsole;
    private Config config;

    @Override
    public void handle(ActionEvent event) {
        config = readConfig("src/main/resources/config.yml");

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
            throw new RuntimeException(e);
        }
        final String cloneUrl = String.format("%s://%s/%s/%s.git", urlGit.getProtocol(), urlGit.getHost(), orgName, repoName);
        log.info("git url = {}", cloneUrl);
        final String gitPath =  ProjectMapConfigReader.deriveLocalGitPathByGitUrl(cloneUrl);
        final String localPath = ProjectMapConfigReader.deriveLocalWorkingPath(cloneUrl, shortName);
        log.info("git path = {}", gitPath);
        final String gitClonePath = cloneRepo(gitPath, host, orgName, repoName, branchName, shortName);
        log.info("working path = {}", localPath);

        // Step 3: Download the project into the specified local path
        copyProjectToWorkingSpace(gitClonePath, localPath);
        tfCurrentProjectPath.setText(localPath);

        // Show success message
        showSuccessMessage();
    }

    protected Config readConfig(String configPath) {
        Yaml yaml = new Yaml();
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(configPath);
        } catch (FileNotFoundException e) {
            log.error("Error while reading app config - {}", configPath, e);
            tfCurrentProjectPath.setText("");
        }
        return yaml.loadAs(fileInputStream, Config.class);
    }
    // Step 1: Extract the repository name and branch name from the URL
    protected String[] extractRepoAndBranch(String url) throws IllegalArgumentException {
        final URI uri = URI.create(url);
        final Pattern patternGitPathBoth = Pattern.compile(REGEX_GIT_PATH_BOTH);
        if (!patternGitPathBoth.matcher(uri.getPath()).find()) {
            throw new IllegalArgumentException(String.format("not expected git path passed - %s", uri.getPath()));
        }
        final String[] parts = uri.getPath().split("/");
        final String flattenParts = Arrays.stream(parts).collect(Collectors.joining(",", "[", "]"));
        log.info("schema={}, host={}, port={}, path={}, path.split={}",
                uri.getScheme(),
                uri.getHost(),
                uri.getPort(),
                uri.getPath(),
                flattenParts);

        final String host = uri.getHost();
        final String orgName = parts[1];
        final String repoName = parts[2];
        final Pattern patternGitPathWithBranch = Pattern.compile(REGEX_GIT_PATH_WITH_BRANCH);
        final Pattern patternMasterGitPath = Pattern.compile(REGEX_MASTER_GIT_PATH);
        final boolean isPathWithBranch = patternGitPathWithBranch.matcher(uri.getPath()).find();
        final boolean isMasterPath = patternMasterGitPath.matcher(uri.getPath()).find();

        String branchName = null;
        if (isPathWithBranch) {
            branchName = uri.getPath().replace(
                    String.format("/%s/%s/tree/", orgName, repoName), "");
        }
        if (isMasterPath) {
            branchName = "master";
        }
        if (StringUtils.isEmpty(branchName)) {
            branchName = "master";
        }

        return new String[] { host, orgName, repoName, branchName };
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