package ch.epfl.tchu.net;

import ch.epfl.tchu.gui.GraphicalPlayerAdapter;
import ch.epfl.tchu.gui.GuiConstants;
import ch.epfl.tchu.gui.ObservableGameState;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

import java.net.UnknownHostException;

public class MainMenuClientController {
    String defaultIp = NetConstants.Network.DEFAULT_IP;
    int defaultPort = NetConstants.Network.DEFAULT_PORT;
    @FXML private Button joinGame, configNgrok;
    @FXML private TextField IpField, port;
    @FXML private CheckBox otherServicesUsed;
    public static boolean checkBoxSelected;

    public static String IpFieldText;
    public void setFirstNumbers() {
        IpField.setText("128.179.");
    }

    public void ngrokConfigAction() {
        scaleButton(configNgrok);
        GuiConstants.openNgrokConfigInfoStage();
    }
    public void joinGameAction() {
        checkBoxSelected = otherServicesUsed.isSelected();
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
