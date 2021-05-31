package ch.epfl.tchu.net;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class RunServer extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        stage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/MainMenuServer.fxml"));
        Parent content = loader.load();
        stage.setTitle("TCHU \u2014 Serveur");
        stage.getIcons().add(new Image("logo.png"));
        stage.setScene(new Scene(content, 455, 650));
        stage.show();
        ChatApp.isServer = true;
//        new ChatApp().start(new Stage());
    }
}
