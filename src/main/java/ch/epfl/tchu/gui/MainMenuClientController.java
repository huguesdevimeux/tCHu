package ch.epfl.tchu.gui;

import ch.epfl.tchu.net.PlayersIPAddress;
import ch.epfl.tchu.net.RemotePlayerClient;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainMenuClientController {
    @FXML private Button getIP, joinGame, copyIP;
    @FXML private TextField IpField, port;

    public void setMenuActions() throws UnknownHostException {
        String numericalIp = PlayersIPAddress.getIPAddress();
        getIP.setOnMouseClicked(e -> IpField.setText(numericalIp));
        copyIP.setOnMouseClicked(
                event -> {

                });

        joinGame.setOnMouseClicked(
                e -> {
                    try {

                        new Thread(
                                        () ->
                                        {
                                            try {
                                                new RemotePlayerClient(
                                                                new GraphicalPlayerAdapter(),
                                                        InetAddress.getLocalHost().getHostAddress(),
                                                                Integer.parseInt(port.getText()))
                                                        .run();
                                            } catch (UnknownHostException unknownHostException) {
                                                unknownHostException.printStackTrace();
                                            }
                                        })
                                .start();
                    } catch (UncheckedIOException exception) {
                        System.out.println("fuck u");
                    }
                });
    }
}
