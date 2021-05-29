package ch.epfl.tchu.net;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.ChMap;
import ch.epfl.tchu.game.Game;
import ch.epfl.tchu.game.Player;
import ch.epfl.tchu.game.PlayerId;
import ch.epfl.tchu.gui.GraphicalPlayerAdapter;
import ch.epfl.tchu.gui.GuiConstants;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainMenuServerController {
    Map<PlayerId, String> playersNames = new HashMap<>();
    Map<PlayerId, Player> players = new HashMap<>();
    @FXML private Button hostGame, configNgrok, play, getIP;
    @FXML private TextField firstPlayerName, secondPlayerName, IpField;
    @FXML private Label awaitingConnectionLabel;

    public void hostGameAction() {
        hostGameOnPressed();
        PauseTransition pauseTransition = new PauseTransition(Duration.seconds(1));
        pauseTransition.setOnFinished(
                actionEvent -> {
                    String[] names = configureNames();
                    PlayerId.ALL.forEach(playerId -> playersNames.put(playerId, names[playerId.ordinal()]));
                    try {
                        ServerSocket serverSocket =
                                new ServerSocket(NetConstants.Network.DEFAULT_PORT);
                        players.put(PlayerId.PLAYER_1, new GraphicalPlayerAdapter());
                        players.put(
                                PlayerId.PLAYER_2, new RemotePlayerProxy(serverSocket.accept()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    hostGameOnConnectionEstablished();
                    play.setOnMouseClicked(
                            e -> {
                                scaleButton(play);
                                launchGame();
                            });
                });
        pauseTransition.playFromStart();
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

    private void launchGame() {
        new Thread(
                () ->
                        Game.play(
                                players,
                                playersNames,
                                SortedBag.of(ChMap.tickets()),
                                new Random()))
                .start();
    }

    private void scaleButton(Button button) {
        GuiConstants.scaleButton(button);
    }

    private void hostGameOnPressed() {
        hostGame.setText(hostGame.getText() + "...");
        awaitingConnectionLabel.setText("En attente d'une connection");
        awaitingConnectionLabel.setTextFill(Color.RED);
        hostGame.setDisable(true);
    }

    private void hostGameOnConnectionEstablished() {
        awaitingConnectionLabel.setText("Connection établie!");
        awaitingConnectionLabel.setTextFill(Color.GREEN);
        hostGame.setText(hostGame.getText().replace("...", ""));
        play.setDisable(false);
    }

    private String[] configureNames() {
        String[] names = new String[PlayerId.COUNT];
        names[0] = firstPlayerName.getText().isEmpty() ? "Joueur 1" : firstPlayerName.getText();
        names[1] = secondPlayerName.getText().isEmpty() ? "Joueur 2" : secondPlayerName.getText();
        return names;
    }
}