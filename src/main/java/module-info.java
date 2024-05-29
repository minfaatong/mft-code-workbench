module my.assistant {
    requires javafx.controls;
    requires static lombok;
    requires javafx.fxml;
    requires org.slf4j;
    requires org.apache.commons.lang3;
    requires org.yaml.snakeyaml;
    requires com.google.gson;

    exports com.github.minfaatong.tool.codeworkbench;
    exports com.github.minfaatong.tool.codeworkbench.config;
    exports com.github.minfaatong.tool.codeworkbench.handler;
    exports com.github.minfaatong.tool.codeworkbench.utils;
    exports com.github.minfaatong.tool.codeworkbench.listener;
}