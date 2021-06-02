package ch.epfl.tchu.net;

import ch.epfl.tchu.gui.GuiConstants;
import ch.epfl.tchu.gui.ObservableGameState;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ServerSocket;

public class RunClient extends Application {
    public static TextArea messages = new TextArea();
    public static ChattingConnection connection;

    public static void main(String[] args) {
        launch(args);
    }

    public static ChattingClient createClient(String IP) {
        return new ChattingClient(
                IP,
                NetConstants.Network.CHAT_DEFAULT_PORT,
                data ->
                        Platform.runLater(
                                () -> messages.appendText(data)));
    }

    @Override
    public void start(Stage stage) throws Exception {
        ObservableGameState.isServer.set(false);
        stage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/MainMenuClient.fxml"));
        stage.setTitle("TCHU \u2014 Client");
        stage.getIcons().add(new Image("logo.png"));
        stage.setScene(new Scene(loader.load(), 390, 570));
		((MainMenuClientController) loader.getController()).setStage(stage);
        stage.show();
    }
}
