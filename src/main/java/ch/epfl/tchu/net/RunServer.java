package ch.epfl.tchu.net;

import ch.epfl.tchu.game.PlayerId;
import ch.epfl.tchu.gui.ObservableGameState;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Map;

public class RunServer extends Application {
    public static ObservableList<Map.Entry<PlayerId, String>> messages =
            FXCollections.observableArrayList();
    public static final ChattingConnection connection = createServer();

    private static ChattingServer createServer() {
        return new ChattingServer(
                NetConstants.Network.CHAT_DEFAULT_PORT,
                data ->
                        Platform.runLater(
                                () -> messages.add(Map.entry(PlayerId.PLAYER_2, data))));
    }

    @Override
    public void start(Stage stage) throws Exception {
        ObservableGameState.isServer.set(true);
        connection.startConnection();
        stage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/MainMenuServer.fxml"));
        Parent content = loader.load();
        stage.setTitle("TCHU \u2014 Serveur");
        stage.getIcons().add(new Image("logo.png"));
        stage.setScene(new Scene(content, 455, 650));
        ((MainMenuServerController) loader.getController()).setStage(stage);
        stage.show();
    }
}
