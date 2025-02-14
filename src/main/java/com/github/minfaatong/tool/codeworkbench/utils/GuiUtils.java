package com.github.minfaatong.tool.codeworkbench.utils;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
public class GuiUtils {


    public static void printProcessOutput(Process process, TextArea taLogConsole) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            log.info(line);
            final String finalLine = line;
            Platform.runLater(()-> taLogConsole.appendText(System.lineSeparator() + finalLine));
        }
    }
}
