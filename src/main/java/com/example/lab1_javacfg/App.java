package com.example.lab1_javacfg;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    @Override
    public void start(Stage mainStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("MainStageView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        ((MainStageController) fxmlLoader.getController()).setHostServices(getHostServices());
        mainStage.setTitle("Java - Control Flow Graph builder");
        mainStage.setScene(scene);
        mainStage.setMinWidth(550);
        mainStage.setWidth(550);
        mainStage.setMinHeight(600);
        mainStage.setHeight(600);
        mainStage.show();
    }

    public static void main(String[] args) { launch(); }
}