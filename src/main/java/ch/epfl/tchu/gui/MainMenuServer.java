package ch.epfl.tchu.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class MainMenuServer {


    public Stage mainMenuView() throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/MainMenuLayout.fxml"));
        Parent content = loader.load();
        stage.setScene(new Scene(content, 700,650));
        return stage;
    }
}
