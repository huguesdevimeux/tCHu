package ch.epfl.tchu.gui;

import javafx.application.Application;
import javafx.stage.Stage;

public class RunServer extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        MainMenuServer mainMenu = new MainMenuServer();
        mainMenu.mainMenuView().show();
        }
}