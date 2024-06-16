package com.github.minfaatong.tool.codeworkbench.listener;

public interface GitUrlParserListener {
    void gitUrlParsed(String host, String organization, String repository, String branch, String shortName);
}
