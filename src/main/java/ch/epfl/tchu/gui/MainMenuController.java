package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.ChMap;
import ch.epfl.tchu.game.Game;
import ch.epfl.tchu.game.Player;
import ch.epfl.tchu.game.PlayerId;
import ch.epfl.tchu.net.PlayersIPAddress;
import ch.epfl.tchu.net.RemotePlayerProxy;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainMenuController {
    @FXML private Button numericalIP, joinGame, hostGame, copyIP, configNgrok;
    @FXML private TextField playerName, playersNumericalIP;
    @FXML private Button play;

    public void setMenuActions() throws Exception {
        String numericalIP = PlayersIPAddress.getIPAddress();
        this.numericalIP.setOnMouseClicked(e -> playersNumericalIP.setText(numericalIP));

        configNgrok.setOnMouseClicked(
                e -> {
                    try {
                        FXMLLoader fxmlLoader = new FXMLLoader();
                        fxmlLoader.setLocation(MainMenu.class.getResource("/NgrokConfig.fxml"));
                        Scene scene = new Scene(fxmlLoader.load(), 390, 90);
                        Stage stage = new Stage();
                        stage.setScene(scene);
                        stage.show();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                });


        Map<PlayerId, String> playersNames = new HashMap<>();
        Map<PlayerId, Player> players = new HashMap<>();

        hostGame.setOnMouseClicked(
                e -> {
                    hostGame.setText(hostGame.getText() + "...");
                    disableButtons();
                    PauseTransition pauseTransition = new PauseTransition(Duration.seconds(1));
                    pauseTransition.setOnFinished(
                            x -> {
                                try {
                                    String[] names = new String[PlayerId.COUNT];
                                    if (!playerName.getText().isEmpty()) {
                                        names = playerName.getText().split(" ");
                                    } else {
                                        for (int i = 0; i < names.length; i++) {
                                            names[i] = String.format("Joueur %s", i + 1);
                                        }
                                    }
                                    for (int i = 0; i < PlayerId.COUNT; i++) {
                                        playersNames.put(PlayerId.ALL.get(i), names[i]);
                                    }
                                    ServerSocket serverSocket =
                                            new ServerSocket(GuiConstants.DEFAULT_PORT);
                                    players.put(PlayerId.PLAYER_1, new GraphicalPlayerAdapter());
                                    players.put(PlayerId.PLAYER_2, new RemotePlayerProxy(serverSocket.accept()));
                                } catch (IOException exception) {
                                    exception.printStackTrace();
                                }
                                play.setOnMouseClicked(
                                        event -> new Thread(
                                                        () -> Game.play(
                                                                        players,
                                                                        playersNames,
                                                                        SortedBag.of(ChMap.tickets()),
                                                                        new Random())).start());
                            });
                    pauseTransition.playFromStart();
                });

        joinGame.setOnMouseClicked(
                e -> {
                    //          disableButtons();
                    joinGame.setText(joinGame.getText() + "...");
                    //                    thread.
                });

        copyIP.setOnMouseClicked(
                e ->
                        Toolkit.getDefaultToolkit()
                                .getSystemClipboard()
                                .setContents(
                                        new StringSelection(playersNumericalIP.getText()), null));
    }

    private void disableButtons() {
        hostGame.setDisable(true);
        joinGame.setDisable(true);
    }
}
