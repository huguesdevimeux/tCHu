package ch.epfl.tchu.net;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;

import java.io.*;
import java.net.Socket;
import java.util.*;

import static ch.epfl.tchu.net.Serdes.*;

/**
 * Represents a proxy player. Meant to be used by {@link ch.epfl.tchu.game.Game} as a normal Player.
 *
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public final class RemotePlayerProxy implements Player {

    // NOTE : these are never closed, and this is intended since this class will in theory be run
    // during the WHOLE programme.
    private final BufferedWriter writer;
    private final BufferedReader reader;

    /**
     * Constructor for {@link RemotePlayerProxy}.
     *
     * @param socket The sockets that will be used to communicate online.
     * @throws NullPointerException if socket is null.
     */
    public RemotePlayerProxy(Socket socket) {
        Objects.requireNonNull(socket);
        try {
            this.writer =
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    socket.getOutputStream(), NetConstants.ENCODING));
            this.reader =
                    new BufferedReader(
                            new InputStreamReader(socket.getInputStream(), NetConstants.ENCODING));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void initPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        Map<PlayerId, String> orderedMap = new EnumMap<>(playerNames);
        sendInNetwork(
                MessageId.INIT_PLAYERS,
                List.of(
                        playerIdSerde.serialize(ownId),
                        stringListSerde.serialize(new ArrayList<>(orderedMap.values()))));
    }

    @Override
    public void receiveInfo(String info) {
        sendInNetwork(MessageId.RECEIVE_INFO, List.of(stringSerde.serialize(info)));
    }

    @Override
    public void updateState(PublicGameState newState, PlayerState ownState) {
        sendInNetwork(
                MessageId.UPDATE_STATE,
                List.of(
                        publicGameStateSerde.serialize(newState),
                        playerStateSerde.serialize(ownState)));
    }

    @Override
    public void setInitialTicketChoice(SortedBag<Ticket> tickets) {
        sendInNetwork(MessageId.SET_INITIAL_TICKETS, List.of(ticketBagSerde.serialize(tickets)));
    }

    @Override
    public SortedBag<Ticket> chooseInitialTickets() {
        sendInNetwork(MessageId.CHOOSE_INITIAL_TICKETS);
        return ticketBagSerde.deserialize(readFromNetwork());
    }

    @Override
    public TurnKind nextTurn() {
        sendInNetwork(MessageId.NEXT_TURN);
        return turnKindSerde.deserialize(readFromNetwork());
    }

    @Override
    public SortedBag<Ticket> chooseTickets(SortedBag<Ticket> options) {
        sendInNetwork(MessageId.CHOOSE_TICKETS, List.of(ticketBagSerde.serialize(options)));
        return ticketBagSerde.deserialize(readFromNetwork());
    }

    @Override
    public int drawSlot() {
        sendInNetwork(MessageId.DRAW_SLOT);
        return intSerde.deserialize(readFromNetwork());
    }

    @Override
    public Route claimedRoute() {
        sendInNetwork(MessageId.ROUTE);
        return routeSerde.deserialize(readFromNetwork());
    }

    @Override
    public SortedBag<Card> initialClaimCards() {
        sendInNetwork(MessageId.CARDS);
        return cardBagSerde.deserialize(readFromNetwork());
    }

    @Override
    public SortedBag<Card> chooseAdditionalCards(List<SortedBag<Card>> options) {
        sendInNetwork(
                MessageId.CHOOSE_ADDITIONAL_CARDS, List.of(listOfCardBagSerde.serialize(options)));
        return cardBagSerde.deserialize(readFromNetwork());
    }

	/**
	 * Reads the network and return a string corresponding to the response.
	 *
	 * @return The response.
	 * @throws IllegalStateException if the end of the stream has been reached
	 */
	private String readFromNetwork() {
        String returnValue;
        try {
            returnValue = reader.readLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (returnValue == null) {
            throw new IllegalStateException("Expected response from network but gets nothing.");
        }
        return returnValue;
    }

    /**
     * Sends in the network the passed messageId and eventual args in the right format. Blocking.
     *
     * @param messageId The message Id. Can't be null.
     */
    private void sendInNetwork(MessageId messageId) {
        sendInNetwork(messageId, Collections.emptyList());
    }

    /**
     * Sends in the network the passed messageId and eventual args in the right format. Blocking.
     *
     * @param messageId The message Id. Can't be null.
     * @param serializedArgs The args serialized. Can't be null but can be empty.
     */
    private void sendInNetwork(MessageId messageId, List<String> serializedArgs) {
        try {
            this.writer.write(Objects.requireNonNull(messageId).name() + NetConstants.SPACE);
            if (serializedArgs.size() > 0) {
                this.writer.write(
                        String.join(NetConstants.SPACE, Objects.requireNonNull(serializedArgs)));
            }
            this.writer.write(NetConstants.END_LINE);
            this.writer.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
