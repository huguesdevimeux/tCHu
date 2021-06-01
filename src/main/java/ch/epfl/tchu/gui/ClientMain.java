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
        URL profileImageURL = NetConstants.Image.URLof("PLAYER_2.png");
        if (params.size() == NetConstants.Network.NUMBER_PARAMETERS_REQUIRED) {
            ipTarget = params.get(0);
            port = Integer.parseInt(params.get(1));
            profileImageURL = new URL(params.get(2));
        } else if (params.size() != 0)
            throw new Exception("Wrong number of parameters given to the programme. Exiting.");

        try (Socket imageSocket = new Socket(ipTarget, port)) {
            ProfileImagesUtils.sendImage(
                    imageSocket.getOutputStream(),
                    ProfileImagesUtils.validateImage(ImageIO.read(profileImageURL)));
            // Get images from network.
            var images = ProfileImagesUtils.retrieveImages(imageSocket.getInputStream());
            // Save locally the images.
            for (var playerAndPicture : Objects.requireNonNull(images).entrySet()) {
                ProfileImagesUtils.saveImageFor(
                        playerAndPicture.getKey(), playerAndPicture.getValue());
            }
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
