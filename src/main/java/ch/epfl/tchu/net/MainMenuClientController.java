package ch.epfl.tchu.net;

import ch.epfl.tchu.gui.GraphicalPlayerAdapter;
import ch.epfl.tchu.gui.GuiConstants;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainMenuClientController {
    String defaultIp = NetConstants.Network.DEFAULT_IP;
    int defaultPort = NetConstants.Network.DEFAULT_PORT;
    @FXML private Button joinGame, configNgrok;
    @FXML private TextField IpField, port;

    public void menuActions() throws UnknownHostException {
        configNgrok.setOnMouseClicked(
                e -> {
                    scaleButton(configNgrok);
                    GuiConstants.openNgrokConfigInfoStage();
                });
        joinGame.setOnMouseClicked(
                e -> {
                    scaleButton(joinGame);
                    String ip;
                    int port;
                    if (IpField.getText().isEmpty()){
                        IpField.setText(defaultIp);
                        ip = defaultIp;
                    } else ip = IpField.getText();
                    if (this.port.getText().isEmpty()) {
                        this.port.setText(String.valueOf(defaultPort));
                        port = defaultPort;
                    } else port = Integer.parseInt(this.port.getText());
                    clientThread(ip, port).start();
                });
    }

    private void scaleButton(Button button) {
        GuiConstants.scaleButton(button);
    }

    private Thread clientThread(String ip, int port) {
        return new Thread(() -> new RemotePlayerClient(new GraphicalPlayerAdapter(), ip, port).run());
    }
}
