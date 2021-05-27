package ch.epfl.tchu.gui;

import javafx.application.Application;
import javafx.stage.Stage;

public class RunClient extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        MainMenuClient mainMenuClient = new MainMenuClient();
        mainMenuClient.mainMenuView().show();
    }
}
