package ch.epfl.tchu;

import javax.imageio.ImageIO;
import javax.net.SocketFactory;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;

import static java.nio.charset.StandardCharsets.US_ASCII;

/**
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public class Sender {
    public static void main(String[] args) {
//        try (Socket s1 = new Socket("0.tcp.eu.ngrok.io", 19370);
//			 Socket s = new Socket("0.tcp.eu.ngrok.io", 19370) ) {
			try (Socket s1 = new Socket("localhost", 5108);
				 Socket s = new Socket("localhost", 5109)) {

				BufferedImage a =
                    ImageIO.read(
                            new File(
                                    "C:\\Users\\devimeux\\IdeaProjects\\tCHu\\src\\main\\resources\\PLAYER_1.png"));
            BufferedImage b =
                    ImageIO.read(
                            new File(
                                    "C:\\Users\\devimeux\\IdeaProjects\\tCHu\\src\\main\\resources\\PLAYER_2.png"));
			OutputStream outputStream = s.getOutputStream();
            System.out.println("Got the output steam");
			ImageIO.write(a, "png", outputStream);
			outputStream.flush();
            System.out.println("Sent the first");
			ImageIO.write(b, "png", s.getOutputStream());
			outputStream.flush();
			System.out.println("Sent the second");

//			BufferedReader r =
//				new BufferedReader(
//					new InputStreamReader(s.getInputStream(),
//						US_ASCII));
//			BufferedWriter w =
//				new BufferedWriter(
//					new OutputStreamWriter(s.getOutputStream(),
//						US_ASCII));
//				int i = 2021;
//				w.write(String.valueOf(i));
//				w.write('\n');
//				w.flush();
//				int succ = Integer.parseInt(r.readLine());
//				System.out.println(succ);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
