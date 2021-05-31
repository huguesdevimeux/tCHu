package ch.epfl.tchu.net;

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
    private static boolean isServer = false;
    private static TextArea messages = new TextArea();
    private static final ChattingConnection connection = isServer ? createServer() : createClient();

    public static Parent createContent() {
        messages.setPrefHeight(550);
        TextField input = new TextField();

        input.setOnAction(e -> {
            String message = isServer ? "server: " : "client: ";
            message += input.getText();
            input.clear();

            messages.appendText(message + System.lineSeparator());
            try {
                connection.send(message);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        });

        VBox root = new VBox(20, messages, input);
        root.setPrefSize(600, 600);
        return root;
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static ChattingServer createServer() {
        return new ChattingServer(
                5108,
                data ->
                        Platform.runLater(
                                () -> {
                                    messages.appendText(data);
                                }));
    }

    private static ChattingClient createClient() {
        return new ChattingClient(
                "localhost",
                5108,
                data ->
                        Platform.runLater(
                                () -> {
                                    messages.appendText(data + "\n");
                                }));
    }

    public void init() {
      //  connection.startConnection();
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.show();
    }
}
