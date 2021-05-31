package ch.epfl.tchu.net;

import ch.epfl.tchu.game.PlayerId;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public class ProfileImagesUtils {

    private static final Path imagesLocation = generateTempPath();

    private static Path generateTempPath() {
        try {
            return Files.createTempDirectory("test");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private ProfileImagesUtils() {}

    public static void sendImage(OutputStream outputStream, BufferedImage image)
            throws IOException {
        ImageIO.write(
                Objects.requireNonNull(image), NetConstants.Image.EXTENSION_IMAGE, outputStream);
    }

	public static EnumMap<PlayerId, BufferedImage> retrieveImages(InputStream inputStream)
            throws IOException {
        var ret = new EnumMap<PlayerId, BufferedImage>(PlayerId.class);
        for (PlayerId playerId : PlayerId.ALL) {
            ret.put(
                    playerId,
                    Objects.requireNonNull(
                            ImageIO.read(ImageIO.createImageInputStream(inputStream))));
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
			new File(pathOfImageOf(playerId).toUri()));
    }

    /**
     * Loads the profile image associated with the given player.
     *
     * @param playerId The player.
     * @return The image.
     */
    public static BufferedImage loadImageFor(PlayerId playerId) throws IOException {
        return ImageIO.read(pathOfImageOf(playerId).toFile());
    }

	/**
	 * Retrieves the path pointing at the image of the givent playerid.
	 *
 	 * @param playerId The payer from which the image belongs
	 * @return The path.
	 */
	public static Path pathOfImageOf(PlayerId playerId) {
    	return imagesLocation.resolve(playerId.name());
	}

	/**
	 * Validates the image size and weight. Resize the image if not square.
	 *
	 * @param image The image.
	 * @return The image.
	 */
	public static BufferedImage validateImage(BufferedImage image) {
		BufferedImage tempImage = image;
		if (image.getHeight() != image.getWidth()) {
			int resizeSize = Math.min(image.getHeight(), image.getWidth());
			tempImage = tempImage.getSubimage(0, 0, resizeSize, resizeSize);
		}
        return tempImage;
	}

}
