package ch.epfl.tchu.net;

import ch.epfl.tchu.gui.GraphicalPlayerAdapter;
import ch.epfl.tchu.gui.GuiConstants;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Objects;

public class MainMenuClientController {
    public static boolean checkBoxSelected;
    public static String IpFieldText;
    private final FileChooser fileChooser = createFileChooser();
    String defaultIp = NetConstants.Network.DEFAULT_IP;
    int defaultPort = NetConstants.Network.DEFAULT_PORT;
    @FXML private Button joinGame, configNgrok, indications;
    @FXML private TextField IpField, port, chatPort;
    @FXML private CheckBox multiPortEnabled;
    private Window currentWindow;
    private URL chosenPictureURL = NetConstants.Image.DEFAULT_PROFILE_CLIENT;

    public void setStage(Stage stage) {
        currentWindow = stage.getScene().getWindow();
    }

    public void openIndications() {
        GuiConstants.openIndications();
    }

    private FileChooser createFileChooser() {
        FileChooser temp = new FileChooser();
        temp.getExtensionFilters().add(new FileChooser.ExtensionFilter("Only png images", "*.png"));
        return temp;
    }

    public void setFieldVisible() {
        chatPort.setVisible(multiPortEnabled.isSelected());
    }

    public void ngrokConfigAction() {
        scaleButton(configNgrok);
        GuiConstants.openNgrokConfigInfoStage();
    }

    public void joinGameAction() {
        scaleButton(joinGame);
        checkBoxSelected = multiPortEnabled.isSelected();
        IpFieldText = IpField.getText();
        RunClient.connection = RunClient.createClient(IpFieldText);
        RunClient.connection.startConnection();
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

        if (chatPort.getText().isEmpty())
            chatPort.setText(String.valueOf(NetConstants.Network.CHAT_DEFAULT_PORT));

        try (Socket imageSocket = new Socket(ip, port)) {
            ProfileImagesUtils.sendImage(
                    imageSocket.getOutputStream(),
                    ProfileImagesUtils.validateImage(ImageIO.read(chosenPictureURL)));
            // Get images from network.
            var images = ProfileImagesUtils.retrieveImages(imageSocket.getInputStream());
            // Save locally the images.
            for (var playerAndPicture : Objects.requireNonNull(images).entrySet()) {
                ProfileImagesUtils.saveImageFor(
                        playerAndPicture.getKey(), playerAndPicture.getValue());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        clientThread(ip, port).start();
    }

    @FXML
    public void setPicture() throws MalformedURLException {
        File f = fileChooser.showOpenDialog(currentWindow);
        if (f != null) chosenPictureURL = f.toURI().toURL();
    }

    private void scaleButton(Button button) {
        GuiConstants.scaleButton(button);
    }

    private Thread clientThread(String ip, int port) {
        return new Thread(
                () -> new RemotePlayerClient(new GraphicalPlayerAdapter(), ip, port).run());
    }
}
