package ch.epfl.tchu.net;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RunServer extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Thread discoveryThread = new Thread(Server.getInstance());
        discoveryThread.start();
        stage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/MainMenuLayout.fxml"));
        Parent content = loader.load();
        stage.setScene(new Scene(content, 700, 650));
        stage.show();
    }
}
