package com.github.minfaatong.tool.codeworkbench.listener;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import lombok.extern.slf4j.Slf4j;

import static com.github.minfaatong.tool.codeworkbench.utils.GitUtils.extractRepoAndBranch;

@Slf4j
public class URLChangeListener implements ChangeListener<String> {

    private final GitUrlParserListener listener;

    public URLChangeListener(GitUrlParserListener listener) {
        this.listener = listener;
    }

    @Override
    public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
        String[] repoAndBranch = extractRepoAndBranch(newValue);
        listener.gitUrlParsed(repoAndBranch[0], repoAndBranch[1], repoAndBranch[2], repoAndBranch[3], repoAndBranch[4]);
    }
}
