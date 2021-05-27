package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.ChMap;
import ch.epfl.tchu.game.*;
import ch.epfl.tchu.game.Player;
import ch.epfl.tchu.game.PlayerId;
import ch.epfl.tchu.net.RemotePlayerProxy;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainMenuServerController {
    @FXML private Button hostGame, configNgrok;
    @FXML private TextField playerName;
    @FXML private Button play;

    public void setMenuActions() throws Exception {

        configNgrok.setOnMouseClicked(
                e -> {
                    try {
                        FXMLLoader fxmlLoader = new FXMLLoader();
                        fxmlLoader.setLocation(MainMenuServer.class.getResource("/NgrokConfig.fxml"));
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
                                            new ServerSocket(5108);
                                    players.put(PlayerId.PLAYER_1, new GraphicalPlayerAdapter());
                                    players.put(
                                            PlayerId.PLAYER_2,
                                            new RemotePlayerProxy(serverSocket.accept()));
                                } catch (IOException exception) {
                                    exception.printStackTrace();
                                }
                                play.setOnMouseClicked(
                                        event ->
                                                new Thread(
                                                                () ->
                                                                        Game.play(
                                                                                players,
                                                                                playersNames,
                                                                                SortedBag.of(
                                                                                        ChMap
                                                                                                .tickets()),
                                                                                new Random()))
                                                        .start());
                            });
                    pauseTransition.playFromStart();
                });
    }

    private void disableButtons() {
        hostGame.setDisable(true);
    }
}
