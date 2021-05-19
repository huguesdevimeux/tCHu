package ch.epfl.tchu.gui;

import ch.epfl.tchu.net.PlayersIPAddress;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;

public class MainMenuController {
  @FXML private Button numericalIP, joinGame, hostGame, copyIP, configNgrok;
  @FXML private TextField playerName, playersNumericalIP;

  public void setMenuActions() throws Exception {
    String numericalIP = PlayersIPAddress.getIPAddress();

    this.numericalIP.setOnMouseClicked(e -> playersNumericalIP.setText(numericalIP));

    configNgrok.setOnMouseClicked(
        e -> {
          try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(MainMenu.class.getResource("/NgrokConfig.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 390, 90);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
          } catch (IOException ioException) {
            ioException.printStackTrace();
          }
        });

    hostGame.setOnMouseClicked(
        e -> {
          disableButtons();
          hostGame.setText(hostGame.getText() + "...");
            try {
                //somethign
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });

    joinGame.setOnMouseClicked(
        e -> {
          disableButtons();
          joinGame.setText(joinGame.getText() + "...");
          try {
            //something
          } catch (Exception exception) {
            exception.printStackTrace();
          }
        });

    copyIP.setOnMouseClicked(
        e ->
            Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(playersNumericalIP.getText()), null));
  }

  private void disableButtons() {
    hostGame.setDisable(true);
    joinGame.setDisable(true);
  }
}


