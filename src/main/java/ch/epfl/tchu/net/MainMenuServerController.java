package ch.epfl.tchu.net;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.ChMap;
import ch.epfl.tchu.game.Game;
import ch.epfl.tchu.game.Player;
import ch.epfl.tchu.game.PlayerId;
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
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.*;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainMenuServerController {
    private final ServerSocket serverSocket = new ServerSocket(NetConstants.Network.DEFAULT_PORT);
    Map<PlayerId, String> playersNames = new HashMap<>();
    Map<PlayerId, Player> players = new HashMap<>();
    @FXML private Button hostGame, configNgrok, play, getIP;
    @FXML
    private TextField firstPlayerName,
            secondPlayerName,
            IpField,
            awaitingConnectionText;

    private static final String WAITING_FOR_CONNECTION = "En attente d'une connexion";
    private static final String CONNECTION_ESTABLISHED = "Un joueur est connecté!";

    private Window currentWindow;
    private final FileChooser fileChooser = createFileChooser();
    private URL chosenPictureURL = NetConstants.Image.DEFAULT_PROFILE_CLIENT;

    public MainMenuServerController() throws IOException {}

    public void setStage(Stage stage) {
        currentWindow = stage.getScene().getWindow();
    }

    private FileChooser createFileChooser() {
        FileChooser temp = new FileChooser();
        temp.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Only png images", "png"));
        return temp;
    }

    public void hostGameAction() {
        try {
            awaitingConnectionText.setText(WAITING_FOR_CONNECTION);
            scaleButton(hostGame);
            hostGame.setDisable(true);
            players.put(PlayerId.PLAYER_1, new GraphicalPlayerAdapter());

            EnumMap<PlayerId, BufferedImage> images = new EnumMap<>(PlayerId.class);

            Socket imagesSocket = serverSocket.accept();
            // Store the images of the players. The first player is considered as the host.
            images.put(
                    PlayerId.PLAYER_1,
                    ProfileImagesUtils.validateImage(ImageIO.read(chosenPictureURL)));
            BufferedImage bufferedImage =
                    ImageIO.read(ImageIO.createImageInputStream(imagesSocket.getInputStream()));
            images.put(PlayerId.PLAYER_2, bufferedImage);

            OutputStream outputStream = imagesSocket.getOutputStream();
            for (PlayerId playerid : PlayerId.ALL) {
                ProfileImagesUtils.saveImageFor(playerid, images.get(playerid));
                ImageIO.write(
                        ProfileImagesUtils.loadImageFor(playerid),
                        NetConstants.Image.EXTENSION_IMAGE,
                        outputStream);
                outputStream.flush();
            }

            new Thread(
                            () -> {
                                try {
                                    players.put(
                                            PlayerId.PLAYER_2,
                                            new RemotePlayerProxy(serverSocket.accept()));
                                    play.setDisable(false);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                awaitingConnectionText.setText(CONNECTION_ESTABLISHED);
                            })
                    .start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playAction() {
        String[] names = configureNames();
        PlayerId.ALL.forEach(playerId -> playersNames.put(playerId, names[playerId.ordinal()]));
        scaleButton(play);
        serverThread().start();
        play.setDisable(true);
    }

    public void getIPAction() throws UnknownHostException {
        String playersIp = PlayerIPAddress.getPublicIPAddress();
        IpField.setText(playersIp);
        scaleButton(getIP);
    }

    public void ngrokConfigAction() {
        scaleButton(configNgrok);
        GuiConstants.openNgrokConfigInfoStage();
    }

    public void copyIpAction() {
        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(IpField.getText()), null);
    }

    private Thread serverThread() {
        return new Thread(
                () ->
                        Game.play(
                                players,
                                playersNames,
                                SortedBag.of(ChMap.tickets()),
                                new Random()));
    }

    private void scaleButton(Button button) {
        GuiConstants.scaleButton(button);
    }

    private String[] configureNames() {
        String[] names = new String[PlayerId.COUNT];
        names[0] = firstPlayerName.getText().isEmpty() ? "Joueur 1" : firstPlayerName.getText();
        names[1] = secondPlayerName.getText().isEmpty() ? "Joueur 2" : secondPlayerName.getText();
        return names;
    }

    @FXML
    public void setPicture() throws MalformedURLException {
        File f = fileChooser.showOpenDialog(currentWindow);
        if (f != null) chosenPictureURL = f.toURI().toURL();
    }
}
