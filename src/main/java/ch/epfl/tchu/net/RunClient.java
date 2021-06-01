package ch.epfl.tchu.net;

import ch.epfl.tchu.gui.GuiConstants;
import javafx.application.Application;
import javafx.application.Platform;
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
    public static boolean isServer = false;
    private static TextArea messages = new TextArea();
    private static final ChattingConnection connection = createClient();

    public static Parent createContent() {
        messages.setPrefHeight(200);
        TextField input = new TextField();
        input.setOnAction(
                e -> {
                    String message ="client: " + input.getText();
                    input.clear();
                    messages.appendText(message + "\n");
                    try {
                        connection.send(message);
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                });

        VBox root = new VBox(20, messages, input);
        root.setPrefSize(100, 100);
        return root;
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static ChattingClient createClient() {
        return new ChattingClient(
                "localhost",
                5010,
                data ->
                        Platform.runLater(
                                () -> {
                                    messages.appendText(data);
                                }));
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/MainMenuClient.fxml"));
        stage.setTitle("TCHU \u2014 Client");
        stage.getIcons().add(new Image("logo.png"));
        stage.setScene(new Scene(loader.load(), 390, 570));
        stage.show();
    }

    public static void showChatPage(){
        Stage chat = new Stage();
        chat.setTitle("client");
        chat.setScene(new Scene(createContent()));
        chat.show();
    }

    @Override
    public void init() throws Exception {
        connection.startConnection();
    }
}
