package ch.epfl.tchu.net;

import ch.epfl.tchu.net.ChattingConnection;
import ch.epfl.tchu.net.ChattingServer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class ChatServerMain extends Application {
    private static TextArea messages = new TextArea();
    private final boolean isServer = true;
    ChattingConnection connection = createServer();

    private static ChattingServer createServer() {
        return new ChattingServer(
                5108,
                data -> {
                    Platform.runLater(() -> messages.appendText((data) + "\n"));
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
