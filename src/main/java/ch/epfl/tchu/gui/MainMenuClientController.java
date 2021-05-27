package ch.epfl.tchu.gui;

import ch.epfl.tchu.net.PlayersIPAddress;
import ch.epfl.tchu.net.RemotePlayerClient;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.UnknownHostException;

public class MainMenuClientController {
    @FXML
    private Button getIP, joinGame, copyIP;
    @FXML private TextField IpField, port;


    public void setMenuActions() throws UnknownHostException {
        String numericalIp = PlayersIPAddress.getIPAddress();
        getIP.setOnMouseClicked(e -> IpField.setText(numericalIp));

        joinGame.setOnMouseClicked(e -> {
            new Thread(
                    () ->
                            new RemotePlayerClient(
                                    new GraphicalPlayerAdapter(),
                                    IpField.getText(),
                                    Integer.parseInt(port.getText()))
                                    .run())
                    .start();
        });
    }

}
