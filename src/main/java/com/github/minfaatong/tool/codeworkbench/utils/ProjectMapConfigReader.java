
package com.github.minfaatong.tool.codeworkbench.utils;

import com.github.minfaatong.tool.codeworkbench.config.ProjectMapConfig;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class ProjectMapConfigReader {
    private final ProjectMapConfig projCfg;

    public ProjectMapConfigReader() {
        projCfg = readConfig("src/main/resources/project-config.json");
    }

    private static ProjectMapConfigReader _INSTANCE;

    static {
        if (_INSTANCE == null) {
            _INSTANCE = new ProjectMapConfigReader();
        }
    }

    public static ProjectMapConfig readConfig(String configPath) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (JsonReader reader = new JsonReader(new FileReader(configPath))) {
            return gson.fromJson(reader, ProjectMapConfig.class);
        } catch (IOException e) {
            log.error("Error while reading project config - {}", configPath, e);
            return null;
        }
    }

    public static ProjectMapConfig getProjectConfig() {
        return _INSTANCE.projCfg;
    }

    public static String deriveLocalGitPathByGitUrl(String gitUrl) {
        for (ProjectMapConfig.ProjectMapSubDir parent : _INSTANCE.projCfg.getSubDir()) {
            for (ProjectMapConfig.ProjectMapSubDirChild child : parent.getChildren()) {
                if (gitUrl.equals(child.getGitUrl())) {
                    return new StringBuffer(_INSTANCE.projCfg.getRootDir()).append("/").append(parent.getOrgFolderName()).append("/").append(child.getFolderName()).append("/").append("br").toString();
                }
            }
        }
        return null;
    }

    public static String deriveLocalWorkingPath(String gitUrl, String shortName) {
        for (ProjectMapConfig.ProjectMapSubDir parent : _INSTANCE.projCfg.getSubDir()) {
            for (ProjectMapConfig.ProjectMapSubDirChild child : parent.getChildren()) {
                if (gitUrl.equals(child.getGitUrl()))
                    return new StringBuffer(_INSTANCE.projCfg.getWorkDir()).append("/").append(child.getFolderPrefix()).append("_").append(shortName).toString();
            }
        }
        return null;
    }
}
