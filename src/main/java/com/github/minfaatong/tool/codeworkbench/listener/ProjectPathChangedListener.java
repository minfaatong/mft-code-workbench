
package com.github.minfaatong.tool.codeworkbench.listener;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;

public class ProjectPathChangedListener implements ChangeListener<String> {
    private final Button[] buttons;

    public ProjectPathChangedListener(Button... buttons) {
        this.buttons = buttons;
    }

    @Override
    public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
        final boolean shouldProjectActionButtonsDisabled = StringUtils.isEmpty(newValue);
        Arrays.stream(buttons).filter(Objects::nonNull).forEach(btn -> btn.setDisable(shouldProjectActionButtonsDisabled));
    }
}
