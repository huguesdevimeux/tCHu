package ch.epfl.tchu.net;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.ChMap;
import ch.epfl.tchu.game.Game;
import ch.epfl.tchu.game.Player;
import ch.epfl.tchu.game.PlayerId;
import ch.epfl.tchu.gui.GraphicalPlayerAdapter;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainMenuServerController {
    @FXML private Button hostGame, configNgrok;
    @FXML private TextField playerName, IpField;
    @FXML private Button play;
    @FXML private Button getIP;
    @FXML private Label awaitingConnectionLabel;

    public void hostGameAction() {
        Map<PlayerId, String> playersNames = new HashMap<>();
        Map<PlayerId, Player> players = new HashMap<>();

        hostGame.setOnMouseClicked(
                e -> {
                    play.setDisable(true);
                    hostGame.setText(hostGame.getText() + "...");
                    awaitingConnectionLabel.setText("En attente d'une connection");
                    awaitingConnectionLabel.setTextFill(Color.RED);
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
                                    ServerSocket serverSocket = new ServerSocket(5108);
                                    Socket socket = serverSocket.accept();
                                    players.put(PlayerId.PLAYER_1, new GraphicalPlayerAdapter());
                                    players.put(PlayerId.PLAYER_2, new RemotePlayerProxy(socket));
                                    System.out.println(socket.getRemoteSocketAddress());
                                } catch (IOException exception) {
                                    exception.printStackTrace();
                                }
                                awaitingConnectionLabel.setText("Connection établie!");
                                awaitingConnectionLabel.setTextFill(Color.GREEN);
                                hostGame.setText(hostGame.getText().replace("...", ""));
                                play.setDisable(false);

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

    public void getIPAction() throws UnknownHostException {
        String playersIp = PlayerIPAddress.getIPAddress();

        IpField.setText(playersIp);
        scaleButton(getIP);
    }

    public void ngrokConfigAction() {
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
        scaleButton(configNgrok);
    }

    private void scaleButton(Button button) {

        button.setScaleX(1.1);
        button.setScaleY(1.1);
        PauseTransition pt = new PauseTransition(Duration.millis(300));
        pt.setOnFinished(
                ev -> {
                    getIP.setScaleX(1);
                    getIP.setScaleY(1);
                });
        pt.playFromStart();
    }

    private void disableButtons() {
        hostGame.setDisable(true);
    }
}
