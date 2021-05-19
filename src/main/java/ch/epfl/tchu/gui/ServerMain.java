package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.ChMap;
import ch.epfl.tchu.game.Game;
import ch.epfl.tchu.game.Player;
import ch.epfl.tchu.game.PlayerId;
import ch.epfl.tchu.net.RemotePlayerProxy;
import javafx.application.Application;
import javafx.stage.Stage;

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

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        List<String> names = GuiConstants.DEFAULT_NAMES;
        List<String> params = getParameters().getRaw();
        if (params.size() == names.size()) names = params;
        else if (params.size() != 0)
            throw new Exception("Invalid number of parameters given to the programme. Exiting.");

        Map<PlayerId, String> playersNames = new HashMap<>();

        for (int i = 0; i < PlayerId.COUNT; i++) {
            playersNames.put(PlayerId.ALL.get(i), names.get(i));
        }

        Map<PlayerId, Player> players = new HashMap<>();
        try (ServerSocket serverSocket = new ServerSocket(GuiConstants.DEFAULT_PORT)) {

            players.put(PlayerId.PLAYER_1, new GraphicalPlayerAdapter());
            for (int i = 1; i < PlayerId.COUNT; i++) {
                players.put(PlayerId.ALL.get(i), new RemotePlayerProxy(serverSocket.accept()));
            }
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