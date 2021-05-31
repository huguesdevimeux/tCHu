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

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainMenuServerController {
    Map<PlayerId, String> playersNames = new HashMap<>();
    Map<PlayerId, Player> players = new HashMap<>();
    @FXML private Button hostGame, configNgrok, play, getIP;
    @FXML private TextField firstPlayerName, secondPlayerName, thirdPlayerName, IpField;
    @FXML private TextField awaitingConnectionText;
    @FXML private CheckBox checkBox;
    private final ServerSocket serverSocket = new ServerSocket(5108);
    private final Socket socket = new Socket();

    public MainMenuServerController() throws IOException {}

    public void hostGameAction() throws IOException {
        hostGameOnPressed();
        players.put(PlayerId.PLAYER_1, new GraphicalPlayerAdapter());
        String[] names = configureNames();
        PlayerId.ALL.forEach(playerId -> playersNames.put(playerId, names[playerId.ordinal()]));
        new Thread(
                        () -> {
                            try {
                                players.put(PlayerId.PLAYER_2, new RemotePlayerProxy(serverSocket.accept()));
                                play.setDisable(false);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            awaitingConnectionText.setText("Un joueur est connecté!");
                        })
                .start();
        hostGameOnConnectionEstablished();
    }

    public void playAction() {
        scaleButton(play);
        serverThread().start();
        play.setDisable(true);
    }

    public void getIPAction() throws UnknownHostException {
        String playersIp = PlayerIPAddress.getIPAddress();
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

    private void hostGameOnPressed() {
        awaitingConnectionText.setText("En attente d'une connexion");
        hostGame.setDisable(true);
    }

    private void hostGameOnConnectionEstablished() {}

    private String[] configureNames() {
        String[] names = new String[PlayerId.COUNT];
        names[0] = firstPlayerName.getText().isEmpty() ? "Joueur 1" : firstPlayerName.getText();
        names[1] = secondPlayerName.getText().isEmpty() ? "Joueur 2" : secondPlayerName.getText();
        return names;
    }

    public void checkBoxAction() {
        thirdPlayerName.setVisible(checkBox.isSelected());
    }
}
