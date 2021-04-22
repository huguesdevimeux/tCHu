package ch.epfl.tchu.net;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;

import java.io.*;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * Represents a proxy player. Meant to be used by {@link ch.epfl.tchu.game.Game} as a normal Player.
 *
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public class RemoteProxyPlayer implements Player {

	private final BufferedWriter outRedirect;
	private final BufferedReader inRedirect;
	private final Socket socket;

	/**
	 * Constructor for {@link RemoteProxyPlayer}.
	 *
	 * @param socket The sockets that will be used to communicate online.
	 * @throws NullPointerException if socket is null.
	 */
	public RemoteProxyPlayer(Socket socket) {
		this.socket = Objects.requireNonNull(socket);
		try {
			this.outRedirect = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), NetConst.ENCODING));
			this.inRedirect = new BufferedReader(new InputStreamReader(socket.getInputStream(), NetConst.ENCODING));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void initPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {

	}

	@Override
	public void receiveInfo(String info) {

	}

	@Override
	public void updateState(PublicGameState newState, PlayerState ownState) {

	}

	@Override
	public void setInitialTicketChoice(SortedBag<Ticket> tickets) {

	}

	@Override
	public SortedBag<Ticket> chooseInitialTickets() {
		return null;
	}

	@Override
	public TurnKind nextTurn() {
		return null;
	}

	@Override
	public SortedBag<Ticket> chooseTickets(SortedBag<Ticket> options) {
		return null;
	}

	@Override
	public int drawSlot() {
		return 0;
	}

	@Override
	public Route claimedRoute() {
		return null;
	}

	@Override
	public SortedBag<Card> initialClaimCards() {
		return null;
	}

	@Override
	public SortedBag<Card> chooseAdditionalCards(List<SortedBag<Card>> options) {
		return null;
	}

	/**
	 * Handles the sending of method's corresponding message over the network.
	 *
	 * @param messageId The messageId of the message that will be sent.
	 * @return An eventual response of the network, null if there isn't.
	 * @throws UncheckedIOException in case of {@link IOException}.
	 */
	private String networkInteractionHandler(MessageId messageId) {
		return networkInteractionHandler(messageId, Collections.emptyList());
	}

	/**
	 * Handles the sending of method's corresponding message over the network.
	 *
	 * @param messageId      The messageId of the message that will be sent.
	 * @param serializedArgs The arguments of the methods to be communicate.
	 * @return An eventual response of the network, null if there isn't.
	 * @throws UncheckedIOException in case of {@link IOException}.
	 */
	private String networkInteractionHandler(MessageId messageId, List<String> serializedArgs) {
		Objects.requireNonNull(serializedArgs);
		try {
			this.outRedirect.write(messageId + NetConst.SPACE);
			if (serializedArgs.size() > 0)
				this.outRedirect.write(String.join(NetConst.SPACE, serializedArgs));
			this.outRedirect.write(NetConst.ENDLINE);
			return this.inRedirect.readLine();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
