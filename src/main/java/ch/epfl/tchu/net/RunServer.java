package ch.epfl.tchu.net;

import ch.epfl.tchu.gui.InfoViewCreator;
import ch.epfl.tchu.gui.ObservableGameState;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

public class RunServer extends Application {
    private static TextArea messages = new TextArea();
    private static final ChattingConnection connection = createServer();

    public static Parent createContent() {
        messages.setPrefHeight(500);
        TextField input = new TextField();
        input.setOnAction(
                e -> {
                    String message = "server: " + input.getText();
                    input.clear();
                    messages.appendText(message + "\n");
                    try {
                        connection.send(message);
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                });

        VBox root = new VBox(20, messages, input);
        root.setPrefSize(100, 300);
        return root;
    }

    private static ChattingServer createServer() {
        return new ChattingServer(
                5010,
                data ->
                        Platform.runLater(
                                () -> {
                                    messages.appendText(data);
                                }));
    }

    @Override
    public void start(Stage stage) throws Exception {
        ObservableGameState.isServer.set(true);
        stage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/MainMenuServer.fxml"));
        Parent content = loader.load();
        stage.setTitle("TCHU \u2014 Serveur");
        stage.getIcons().add(new Image("logo.png"));
        stage.setScene(new Scene(content, 455, 650));
        stage.show();

//        Stage chat = new Stage();
//        chat.setScene(new Scene(createContent()));
//        chat.show();
    }

    public static void showChatPage(){
        Stage chat = new Stage();
        chat.setTitle("Server");
        chat.setScene(new Scene(createContent()));
        chat.show();
    }

    public void tryToConnect() throws IOException {
        connection.tryToConnect(new ServerSocket(5108));
    }

    @Override
    public void init() throws Exception {
        connection.startConnection();
    }
}
