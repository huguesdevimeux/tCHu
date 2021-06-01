package ch.epfl.tchu.net;

import ch.epfl.tchu.gui.GraphicalPlayerAdapter;
import ch.epfl.tchu.gui.GuiConstants;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.UnknownHostException;

public class MainMenuClientController {
    String defaultIp = NetConstants.Network.DEFAULT_IP;
    int defaultPort = NetConstants.Network.DEFAULT_PORT;
    @FXML private Button joinGame, configNgrok, setFirstNumbers;
    @FXML private TextField IpField, port;

    public static String IpFieldText;
    public void setFirstNumbers() throws UnknownHostException {
        IpField.setText("128.179.");
    }

    public void ngrokConfigAction() {
        scaleButton(configNgrok);
        GuiConstants.openNgrokConfigInfoStage();
    }

    public void joinGameAction() throws Exception {
        IpFieldText = IpField.getText();
        RunClient.connection = RunClient.createClient(IpFieldText);
        RunClient.connection.startConnection();
        scaleButton(joinGame);
        String ip;
        int port;
        if (IpField.getText().isEmpty()) {
            IpField.setText(defaultIp);
            ip = defaultIp;
        } else ip = IpField.getText();

        if (this.port.getText().isEmpty()) {
            this.port.setText(String.valueOf(defaultPort));
            port = defaultPort;
        } else port = Integer.parseInt(this.port.getText());
        clientThread(ip, port).start();
    }

    private void scaleButton(Button button) {
        GuiConstants.scaleButton(button);
    }

    private Thread clientThread(String ip, int port) {
        return new Thread(
                () -> new RemotePlayerClient(new GraphicalPlayerAdapter(), ip, port).run());
    }
}
