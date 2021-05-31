package ch.epfl.tchu.net;

import ch.epfl.tchu.game.PlayerId;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public class ProfileImagesUtils {
    private ProfileImagesUtils() {}

    public static void sendImage(OutputStream outputStream, BufferedImage image)
            throws IOException {
         ImageIO.write(Objects.requireNonNull(image), NetConstants.Image.EXTENSION_IMAGE, outputStream);
    }

    public static void sendImages(
            OutputStream outputStream, EnumMap<PlayerId, BufferedImage> images) throws IOException {
        // THe order is guaranteed by the EnumMap.
        for (Map.Entry<PlayerId, BufferedImage> entry : images.entrySet()) {
			sendImage(outputStream, entry.getValue());
            outputStream.flush();
            System.out.println("Sent one");
        }
    }

    public static EnumMap<PlayerId, BufferedImage> retrieveImages(InputStream inputStream) throws IOException {
    	var ret = new EnumMap<PlayerId, BufferedImage>(PlayerId.class);
    	for (PlayerId playerId : PlayerId.ALL) {
			ret.put(playerId, Objects.requireNonNull(ImageIO.read(ImageIO.createImageInputStream(inputStream))));
            System.out.println("Retrieved one");
		}
		return ret;
    }

    /**
     * Saves image to assigned to the designed player
     *
     * @param playerId The designed player.
     * @param image The image to be saved.
     */
    public static void saveImageFor(PlayerId playerId, BufferedImage image) throws IOException {
        ImageIO.write(
                Objects.requireNonNull(image),
                NetConstants.Image.EXTENSION_IMAGE,
                new File(
                        String.format(NetConstants.Image.FILENAME_PROFILE_IMAGE, playerId.name())));
	}

	public static Image profileImageOf(PlayerId playerId) {
        return null;
    }
}
