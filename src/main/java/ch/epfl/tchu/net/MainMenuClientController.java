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
    @FXML private Button getIP, joinGame, configNgrok;
    @FXML private TextField IpField, port;

    public void menuActions() throws UnknownHostException {
        String serverIP = InetAddress.getLocalHost().getHostAddress();
        getIP.setOnMouseClicked(
                e -> {
                    scaleButton(getIP);
                    IpField.setText(serverIP);
                });
        configNgrok.setOnMouseClicked(
                e -> {
                    scaleButton(configNgrok);
                    GuiConstants.openNgrokConfigInfoStage();
                });
        joinGame.setOnMouseClicked(
                e -> {
                    System.out.println(RunClient.getClient().getReceivePacket().getAddress().getHostAddress());
                    scaleButton(joinGame);
                    if (IpField.getText().isEmpty()) IpField.setText(defaultIp);
                    if (port.getText().isEmpty()) port.setText(String.valueOf(defaultPort));
                    String ip = IpField.getText().isEmpty() ? defaultIp : IpField.getText();
                    int port = this.port.getText().isEmpty() ? defaultPort : Integer.parseInt(this.port.getText());
                    clientThread(ip, port).start();
                });
    }

    private void scaleButton(Button button) {
        GuiConstants.scaleButton(button);
    }

    private Thread clientThread(String ip, int port) {
        return new Thread(
                () -> new RemotePlayerClient(new GraphicalPlayerAdapter(), ip, port).run());
    }
}
