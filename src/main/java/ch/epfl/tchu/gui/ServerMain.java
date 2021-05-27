package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.ChMap;
import ch.epfl.tchu.game.Game;
import ch.epfl.tchu.game.Player;
import ch.epfl.tchu.game.PlayerId;
import ch.epfl.tchu.net.RemotePlayerProxy;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public class ServerMain extends Application {
    public static boolean launched;

    public static void main(String[] args) {
        launch(args);
    }

    public static boolean setLaunched() {
        return true;
    }

    @Override
    public void start(Stage stage) throws IOException {

        List<String> names = GuiConstants.DEFAULT_NAMES;

        Map<PlayerId, String> playersNames = new HashMap<>();

        for (int i = 0; i < PlayerId.ALL.size(); i++) {
            playersNames.put(PlayerId.ALL.get(i), names.get(i));
        }

        Map<PlayerId, Player> players = new HashMap<>();
        try {
            ServerSocket serverSocket = new ServerSocket(GuiConstants.DEFAULT_PORT);
            players.put(PlayerId.PLAYER_1, new GraphicalPlayerAdapter());
            players.put(PlayerId.PLAYER_2, new RemotePlayerProxy(serverSocket.accept()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(
                        () ->
                                Game.play(
                                        players,
                                        playersNames,
                                        SortedBag.of(ChMap.tickets()),
                                        new Random()))
                .start();
    }
}
