package com.github.minfaatong.tool.codeworkbench.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class GitUtils {
    public static final String REGEX_GIT_PATH_BOTH = "^\\/[a-zA-Z0-9\\-]*\\/[a-zA-Z0-9\\-]*(\\/tree\\/[a-zA-Z0-9\\-\\/]*[a-zA-Z0-9\\-]*)?";
    public static final String REGEX_GIT_PATH_WITH_BRANCH = "^\\/[a-zA-Z0-9\\-]*\\/[a-zA-Z0-9\\-]*\\/tree\\/[a-zA-Z0-9\\-\\/]*[a-zA-Z0-9\\-]*";
    public static final String REGEX_MASTER_GIT_PATH = "^\\/[a-zA-Z0-9\\-]*\\/[a-zA-Z0-9\\-]*";
    public static final String MASTER_BRANCH = "master";

    /**
     * Extract the repository name and branch name from the URL
     *
     * @param url git url
     * @return String array consists of 0=host, 1=organization name, 2=repository name, 3=branch name, 4=short name
     * @throws IllegalArgumentException while malformed url being passed
     */
    public static String[] extractRepoAndBranch(String url) throws IllegalArgumentException {
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

        if (isPathWithBranch) {
            String[] pathParts = uri.getPath().split("/");
            String branchName = uri.getPath().replace(
                    String.format("/%s/%s/tree/", orgName, repoName), "");
            String shortName = pathParts[pathParts.length-1];
            if (StringUtils.isEmpty(branchName)) {
                branchName = MASTER_BRANCH;
                shortName = MASTER_BRANCH;
            }
            return new String[] { host, orgName, repoName, branchName, shortName };
        }
        if (isMasterPath) return new String[]{host, orgName, repoName, MASTER_BRANCH, MASTER_BRANCH};
        return new String[] { host, orgName, repoName, MASTER_BRANCH, MASTER_BRANCH};
    }
}
