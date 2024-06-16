package com.github.minfaatong.tool.codeworkbench.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class ProjectMapConfig {
    String rootDir;
    String workDir;
    List<ProjectMapSubDir> subDir;

    @NoArgsConstructor
    @Getter
    @Setter
    public static class ProjectMapSubDir {
        String orgFolderName;
        List<ProjectMapSubDirChild> children;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class ProjectMapSubDirChild {
        String folderName;
        String gitUrl;
        String folderPrefix;
        String desc;
        boolean archived;
    }
}