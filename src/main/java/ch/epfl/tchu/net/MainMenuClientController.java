package ch.epfl.tchu.net;
import ch.epfl.tchu.gui.GraphicalPlayerAdapter;
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
        String serverIP = InetAddress.getLocalHost().getHostAddress();
        getIP.setOnMouseClicked(e -> IpField.setText(serverIP));
        copyIP.setOnMouseClicked(event -> {});

        joinGame.setOnMouseClicked(
                e -> {
                    try {
                        new Thread(() -> {
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