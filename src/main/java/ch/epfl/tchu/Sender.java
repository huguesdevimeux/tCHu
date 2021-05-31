package ch.epfl.tchu;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;

import static java.nio.charset.StandardCharsets.US_ASCII;

/**
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public class Sender {
    public static void main(String[] args) {
        try (Socket s1 = new Socket("localhost", 5108);
			 Socket s = new Socket("localhost", 5109) ) {

			BufferedImage a = ImageIO.read(new File("//home/hugues/OneDrive/Downloads/mathisbg-min.png"));
			BufferedImage b = ImageIO.read(new File("//home/hugues/OneDrive/Downloads/836064319941640243.png"));
			OutputStream outputStream = s1.getOutputStream();
			ImageIO.write(a, "png", outputStream);
			outputStream.flush();
			ImageIO.write(b, "png", outputStream);
			outputStream.flush();
			BufferedReader r =
				new BufferedReader(
					new InputStreamReader(s.getInputStream(),
						US_ASCII));
			BufferedWriter w =
				new BufferedWriter(
					new OutputStreamWriter(s.getOutputStream(),
						US_ASCII));
				int i = 2021;
				w.write(String.valueOf(i));
				w.write('\n');
				w.flush();
				int succ = Integer.parseInt(r.readLine());
				System.out.println(succ);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
