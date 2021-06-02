package ch.epfl.tchu.net;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.ChMap;
import ch.epfl.tchu.game.Game;
import ch.epfl.tchu.game.Player;
import ch.epfl.tchu.game.PlayerId;
import ch.epfl.tchu.gui.GraphicalPlayerAdapter;
import ch.epfl.tchu.gui.GuiConstants;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainMenuServerController {
    private static final String WAITING_FOR_CONNECTION = "En attente d'une connexion";
    private static final String CONNECTION_ESTABLISHED = "Un joueur est connecté!";
    public static boolean checkBoxSelected;
    private final ServerSocket serverSocket = new ServerSocket(NetConstants.Network.DEFAULT_PORT);
    Map<PlayerId, String> playersNames = new HashMap<>();
    Map<PlayerId, Player> players = new HashMap<>();
    @FXML private Button hostGame, configNgrok, play, getIP;
    @FXML
    private TextField firstPlayerName,
            secondPlayerName,
            thirdPlayerName,
            IpField,
            awaitingConnectionText;
    @FXML private CheckBox checkBox, otherServicesUsed;

    public MainMenuServerController() throws IOException {}

    public void hostGameAction() {
        awaitingConnectionText.setText(WAITING_FOR_CONNECTION);
        scaleButton(hostGame);
        hostGame.setDisable(true);
        players.put(PlayerId.PLAYER_1, new GraphicalPlayerAdapter());
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
    }

    public void playAction() {
        checkBoxSelected = otherServicesUsed.isSelected();
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
                () -> Game.play(
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
        if (checkBox.isSelected())
            names[2] = thirdPlayerName.getText().isEmpty() ? "Joueur 3" : thirdPlayerName.getText();
        return names;
    }

    public void checkBoxAction() {
        thirdPlayerName.setVisible(checkBox.isSelected());
    }
}
