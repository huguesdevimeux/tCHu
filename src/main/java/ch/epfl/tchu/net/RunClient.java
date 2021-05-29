package ch.epfl.tchu.net;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RunClient extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        stage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/MainMenuClient.fxml"));
        stage.setScene(new Scene(loader.load(), 700, 650));
        stage.show();
    }
}
