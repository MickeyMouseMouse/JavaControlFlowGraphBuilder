module com.example.lab1_javacfg {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.github.javaparser.core;
    requires guru.nidi.graphviz;
    requires org.testng;

    opens com.example.lab1_javacfg to javafx.fxml;
    exports com.example.lab1_javacfg to javafx.graphics;
    exports com.example.lab1_javacfg.model to org.testng;
    exports com.example.lab1_javacfg.model.cfg;
}