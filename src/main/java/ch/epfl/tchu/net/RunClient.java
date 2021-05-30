package ch.epfl.tchu.net;

import ch.epfl.tchu.game.Client;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RunClient extends Application {
    static Client client;
    @Override
    public void start(Stage stage) throws Exception {
        client = new Client();
        client.connect();
        stage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/MainMenuClient.fxml"));
        stage.setScene(new Scene(loader.load(), 700, 650));
        stage.show();
    }

    public static Client getClient() {
        return client;
    }
}
