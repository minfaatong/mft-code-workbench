package com.github.minfaatong.tool.codeworkbench.config;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class Config {
    AppConfig appConfig;

    @NoArgsConstructor
    @Getter
    @Setter
    public static class AppConfig {
        String gitPath;
        String workingPath;
        String defaultRepo;
        String defaultShortName;
        String ideExePath;
        String terminalExePath;
    }
}