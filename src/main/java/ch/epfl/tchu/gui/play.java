package ch.epfl.tchu.gui;

import ch.epfl.tchu.net.ChattingConnection;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class play extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Button a = new Button("clikc");
        Button b = new Button("er");

        primaryStage.setScene(new Scene(new AnchorPane(new VBox(a,b))));
        ChatApp chatApp1 = new ChatApp();
        ChatApp chatApp2 = new ChatApp();
        //chatApp.init();
        a.setOnAction(
                e -> {
                    try {
                       chatApp1.start(new Stage());
                    } catch (Exception exception) {
                        System.out.println("ouch");
                        exception.printStackTrace();
                    }
                });
        b.setOnAction(e-> {
            try {
                chatApp2.start(new Stage());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
        primaryStage.show();
    }
}
