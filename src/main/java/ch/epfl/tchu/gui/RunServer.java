package ch.epfl.tchu.gui;

import javafx.application.Application;
import javafx.stage.Stage;

public class RunServer extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        ClientMain clientMain = new ClientMain();
        ServerMain serverMain = new ServerMain();
        MainMenuServer mainMenu = new MainMenuServer();
        mainMenu.mainMenuView().show();
        }
}