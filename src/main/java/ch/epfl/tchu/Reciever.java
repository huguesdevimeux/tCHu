package ch.epfl.tchu;

import ch.epfl.tchu.net.NetConstants;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public class Reciever {
	public static void main(String[] args) {
		try (ServerSocket s0 = new ServerSocket(5108);
			 ServerSocket s01 = new ServerSocket(5109) )  {
            System.out.println(NetConstants.Image.URLof("PLAYER_1.png"));
			Path f = Files.createTempDirectory("test");
            System.out.println(f);
			Socket s1= s0.accept();
            System.out.println("Second accepted");

			Socket s = s01.accept();
            System.out.println("Image Sockets done");

			InputStream inputStream = s.getInputStream();
            System.out.println("Git the input");
			ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
			BufferedImage img = ImageIO.read(imageInputStream);
            System.out.println(img);
			BufferedImage read = ImageIO.read(ImageIO.createImageInputStream(inputStream));
            System.out.println(read);
//			ImageIO.write(img, "png", new File("//home/hugues/OneDrive/Downloads/mathisbg-min5"));
//			ImageIO.write(read, "png", new File("//home/hugues/OneDrive/Downloads/mathisbg-min6"));

//			BufferedReader r =
//				new BufferedReader(
//					new InputStreamReader(s1.getInputStream(),
//						US_ASCII));
//			BufferedWriter w =
//				new BufferedWriter(
//					new OutputStreamWriter(s1.getOutputStream(),
//						US_ASCII));
//				int i = Integer.parseInt(r.readLine());
//				int i1 = i + 1;
//				w.write(String.valueOf(i1));
//				w.write('\n');
//				w.flush();
		}
	catch ( IOException e) {
			throw new UncheckedIOException( e);
		}
	}
}
