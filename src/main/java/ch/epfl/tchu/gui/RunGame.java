package ch.epfl.tchu.gui;

import javafx.application.Application;
import javafx.stage.Stage;

public class RunGame extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        ClientMain clientMain = new ClientMain();
        ServerMain serverMain = new ServerMain();
        MainMenu mainMenu = new MainMenu();
        mainMenu.mainMenuView().show();
        //serverMain.start(new Stage());
    }
}