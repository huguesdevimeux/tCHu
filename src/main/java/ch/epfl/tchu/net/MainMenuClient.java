package ch.epfl.tchu.net;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainMenuClient {
    public Stage mainMenuView() throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/MainMenuClient.fxml"));
        Parent content = loader.load();
        stage.setScene(new Scene(content, 700, 650));
        return stage;
    }
}