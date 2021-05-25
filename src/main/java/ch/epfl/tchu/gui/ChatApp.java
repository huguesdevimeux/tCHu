package ch.epfl.tchu.gui;

import ch.epfl.tchu.net.ChattingClient;
import ch.epfl.tchu.net.ChattingConnection;
import ch.epfl.tchu.net.ChattingServer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatApp extends Application {
    private static boolean isServer;
    private static TextArea messages = new TextArea();
    private static final ChattingConnection connection =
            isServer ? createServer(messages) : createClient(messages);
    private static TextField input;

    public static Parent createContent(boolean isServer) {
        // messages = new TextArea();
        input = new TextField();
        messages.setPrefHeight(350);
        messages.setEditable(false);
        input.getStylesheets().add("chatpage.css");
        input.setPromptText("Tapez vôtre message ici...");

        input.setOnAction(
                e -> {
                    String message = isServer ? "server : " : "client: ";
                    message += input.getText();
                    input.clear();
                    messages.appendText(message + "\n");
                    try {
                        connection.send(message);
                    } catch (IOException ioException) {
                        messages.appendText("FAILED");
                    }
                });
        VBox root = new VBox(messages, input);
        root.setPrefSize(320, 360);
        return root;
    }

    private static ChattingServer createServer(TextArea messages) {
        return new ChattingServer(
                5108,
                data -> {
                    Platform.runLater(() -> messages.appendText((data) + "\n"));
                });
    }

    private static ChattingClient createClient(TextArea messages) {
        return new ChattingClient(
                "localhost",
                5108,
                data -> {
                    Platform.runLater(() -> messages.appendText(data + "\n"));
                });
    }

    public static void setIsServer(boolean isServer) {
        ChatApp.isServer = isServer;
    }

    @Override
    public void init() {
        connection.startConnection();
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(ChatApp.createContent(true)));
        stage.setTitle(isServer ? "server" : "client");
        Stage stage1 = new Stage();
        stage1.setScene(new Scene(ChatApp.createContent(false)));
        stage.show();
        stage1.show();
    }
}
