package ch.epfl.tchu.gui;

import ch.epfl.tchu.gui.InfoViewCreator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatViewCreator {
    @FXML private TextField textField;
    @FXML private VBox vBox;

    public static void openPage() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(InfoViewCreator.class.getResource("/chatPage.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 320, 350);
            Stage stage = new Stage();
            stage.setTitle("Chat ici");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void displayMessage() {
        vBox.getChildren().add(new Text(textField.getText()));
    }
}