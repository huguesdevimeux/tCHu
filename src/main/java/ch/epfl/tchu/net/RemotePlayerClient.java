package ch.epfl.tchu.net;

import ch.epfl.tchu.game.Player;

import java.io.*;
import java.net.Socket;

/**
 * Represents a remote player.
 *
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public class RemotePlayerClient {

	private final Player player;
	private final String host;
	private final int port;
	BufferedReader inReader;
	BufferedWriter outWriter;

	public RemotePlayerClient(Player player, String host, int port) {
		this.player = player;
		this.host = host;
		this.port = port;
	}

	/**
	 * Run the client. Handles the game process through the network.
	 */
	public void run() {
		try (Socket s = new Socket(host, port);
			 BufferedReader inReader =
				 new BufferedReader(
					 new InputStreamReader(s.getInputStream(),
						 NetConst.ENCODING));
			 BufferedWriter outWriter =
				 new BufferedWriter(
					 new OutputStreamWriter(s.getOutputStream(),
						 NetConst.ENCODING))) {

			// TODO end of the game ?
			// run shit

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
