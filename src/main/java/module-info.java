module codeworkbench {
    requires static lombok;
    requires org.slf4j;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.yaml.snakeyaml;
    requires org.apache.commons.lang3;
    requires com.google.gson;

    opens com.github.minfaatong.tool.codeworkbench to javafx.fxml;
    opens com.github.minfaatong.tool.codeworkbench.config to com.google.gson;

//   opens com.github.minfaatong.tool.codegenerator to javafx.fxml;
//           javafx.application.Application;
//import javafx.scene.Group;
//import javafx.scene.Scene;
//import javafx.stage.Stage;

    exports com.github.minfaatong.tool.codeworkbench;
    exports com.github.minfaatong.tool.codeworkbench.config;
    exports com.github.minfaatong.tool.codeworkbench.handler;
    exports com.github.minfaatong.tool.codeworkbench.utils;
    exports com.github.minfaatong.tool.codeworkbench.listener;

//    exports com.github.minfaatong.tool.codegenerator;
}