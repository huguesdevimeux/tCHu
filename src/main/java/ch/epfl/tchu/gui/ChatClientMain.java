package ch.epfl.tchu.gui;

import ch.epfl.tchu.net.ChattingClient;
import ch.epfl.tchu.net.ChattingConnection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class ChatClientMain extends Application {
    private static TextArea messages = new TextArea();
    private final boolean isServer = false;
    ChattingConnection connection = createClient();

    private static ChattingClient createClient() {
        return new ChattingClient(
                "localhost",
                5108,
                data -> {
                    Platform.runLater(() -> messages.appendText(data + "\n"));
                });
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        connection.startConnection();
    }

    @Override
    public void start(Stage stage) throws Exception {
//        stage.setScene(new Scene(ChatApp.createContent(messages, connection, isServer)));
        stage.show();
    }
}
