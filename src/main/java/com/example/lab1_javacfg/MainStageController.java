package com.example.lab1_javacfg;

import com.example.lab1_javacfg.model.JavaCFGBuilder;
import com.github.javaparser.ParseProblemException;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.io.File;
import java.io.IOException;

public class MainStageController {
    private HostServices hostServices;
    @FXML
    private TextArea textArea;

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
        textArea.setText("""
                public class MyClass {
                \tpublic int myFun(int n) {
                \t\t
                \t}
                }
                """);
        textArea.positionCaret(52);
    }

    public void copyDOT() {
        String cfgDescription;
        try {
            cfgDescription = JavaCFGBuilder.getCFGDescription(textArea.getText());
            ClipboardContent content = new ClipboardContent();
            content.putString(cfgDescription);
            Clipboard.getSystemClipboard().setContent(content);
        } catch (ParseProblemException e) {
            showError();
        }
    }

    public void showGraphPhoto() {
        String cfgDescription;
        try {
            cfgDescription = JavaCFGBuilder.getCFGDescription(textArea.getText());
            //System.out.println("\n" + cfgDescription + "\n");
        } catch (ParseProblemException e) {
            showError();
            return;
        }
        try {
            MutableGraph graph = new Parser().read(cfgDescription);
            File graphFile = File.createTempFile("graph", ".png");
            Graphviz.fromGraph(graph).render(Format.PNG).toFile(graphFile);
            hostServices.showDocument(graphFile.getAbsolutePath());
            graphFile.deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void showError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Parsing error. Check your code.");
        alert.showAndWait();
    }
}