package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.ChMap;
import ch.epfl.tchu.game.Game;
import ch.epfl.tchu.game.Player;
import ch.epfl.tchu.game.PlayerId;
import ch.epfl.tchu.net.NetConstants;
import ch.epfl.tchu.net.ProfileImagesUtils;
import ch.epfl.tchu.net.RemotePlayerProxy;
import javafx.application.Application;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Server implementation of tCHu. Used to host and play a game of tchu. The whole game is launched
 * from here.
 *
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public final class ServerMain extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        List<String> names = NetConstants.Network.DEFAULT_NAMES;
        List<String> params = getParameters().getRaw();
        URL ownProfileImageURL = NetConstants.Image.URLof("PLAYER_1.png");
        if (params.size() == names.size() + 1) {
            names = params.subList(1, params.size() - 1);
            ownProfileImageURL = new URL(params.get(0));
        } else if (params.size() != 0)
            throw new Exception("Invalid number of parameters given to the programme. Exiting.");

        Map<PlayerId, String> playersNames = new EnumMap<>(PlayerId.class);

        for (int i = 0; i < PlayerId.COUNT; i++) {
            playersNames.put(PlayerId.ALL.get(i), names.get(i));
        }

        EnumMap<PlayerId, Player> players = new EnumMap<>(PlayerId.class);
        EnumMap<PlayerId, BufferedImage> images = new EnumMap<>(PlayerId.class);
        try (ServerSocket serverSocket = new ServerSocket(NetConstants.Network.DEFAULT_PORT)) {
            Socket imagesSocket = serverSocket.accept();

            // Store the images of the players. The first player is considered as the host.
            images.put(PlayerId.PLAYER_1, ProfileImagesUtils.validateImage(ImageIO.read(ownProfileImageURL)));
            System.out.println("Seeking for other's image");
            BufferedImage bufferedImage =
                    ImageIO.read(ImageIO.createImageInputStream(imagesSocket.getInputStream()));
            System.out.println("Got other's image !");
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

            // Handle image retrieving and send back
            players.put(PlayerId.PLAYER_1, new GraphicalPlayerAdapter());
            players.put(PlayerId.PLAYER_2, new RemotePlayerProxy(serverSocket.accept()));
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
