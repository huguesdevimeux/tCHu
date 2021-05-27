package ch.epfl.tchu.gui;

import javafx.application.Application;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class RunClient extends Application {
    public static void main(String[] args) {

        launch(args);
    }

    private static List<String> params = new ArrayList<>();
    @Override
    public void start(Stage stage) throws Exception {
        MainMenuClient mainMenuClient = new MainMenuClient();
        mainMenuClient.mainMenuView().show();
    }

    public static List<String> getParams(){
        return params;
    }
}
