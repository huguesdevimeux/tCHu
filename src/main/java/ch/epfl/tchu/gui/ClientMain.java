package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.PlayerId;
import ch.epfl.tchu.net.NetConstants;
import ch.epfl.tchu.net.ProfileImagesUtils;
import ch.epfl.tchu.net.RemotePlayerClient;
import javafx.application.Application;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.net.URL;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;

/**
 * Implements a client for tCHu, to play a game as a Client.
 *
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public final class ClientMain extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        List<String> params = getParameters().getRaw();
        // Default values.
        String ipTarget = NetConstants.Network.DEFAULT_IP;
        int port = NetConstants.Network.DEFAULT_PORT;
        // TODO Change this. The server should send itseld the default profile image.
        String profileImageURL =
                String.format(
                        NetConstants.Image.FILENAME_DEFAULT_PROFILE_IMAGE,
                        PlayerId.PLAYER_1.name());
        if (params.size() == NetConstants.Network.NUMBER_PARAMETERS_REQUIRED) {
            ipTarget = params.get(0);
            port = Integer.parseInt(params.get(1));
            profileImageURL = params.get(2);
        } else if (params.size() != 0)
            throw new Exception("Wrong number of parameters given to the programme. Exiting.");

        try (Socket imageSocket = new Socket(ipTarget, port + 1)) {
            System.out.println(profileImageURL);
			BufferedImage read = ImageIO.read(new URL(profileImageURL));
			ProfileImagesUtils.sendImage(
                    imageSocket.getOutputStream(), read);
            System.out.println("Sent image");
            // Get images from network.
            EnumMap<PlayerId, BufferedImage> images =
                    ProfileImagesUtils.retrieveImages(imageSocket.getInputStream());
            // Save locally the images.
            Objects.requireNonNull(images)
                    .forEach(
                            (playerId, image) -> {
                                try {
                                    ProfileImagesUtils.saveImageFor(playerId, image);
                                } catch (IOException e) {
                                    throw new UncheckedIOException(e);
                                }
                            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        String finalIpTarget = ipTarget;
        int finalPort = port;

        new Thread(
                        () ->
                                new RemotePlayerClient(
                                                new GraphicalPlayerAdapter(),
                                                finalIpTarget,
                                                finalPort)
                                        .run())
                .start();
    }
}
