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
    private final BufferedWriter outRedirect;
    private final BufferedReader inRedirect;

    /**
     * Constructor for {@link RemotePlayerProxy}.
     *
     * @param socket The sockets that will be used to communicate online.
     * @throws NullPointerException if socket is null.
     */
    public RemotePlayerProxy(Socket socket) {
        Objects.requireNonNull(socket);
        try {
            this.outRedirect =
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    socket.getOutputStream(), NetConstants.ENCODING));
            this.inRedirect =
                    new BufferedReader(
                            new InputStreamReader(socket.getInputStream(), NetConstants.ENCODING));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void initPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        Map<PlayerId, String> orderedMap = new EnumMap<>(playerNames);
        networkInteractionHandler(
                MessageId.INIT_PLAYERS,
                List.of(
                        playerIdSerde.serialize(ownId),
                        stringListSerde.serialize(List.copyOf(orderedMap.values()))),
                false);
    }

    @Override
    public void receiveInfo(String info) {
        networkInteractionHandler(
                MessageId.RECEIVE_INFO, List.of(stringSerde.serialize(info)), false);
    }

    @Override
    public void updateState(PublicGameState newState, PlayerState ownState) {
        networkInteractionHandler(
                MessageId.UPDATE_STATE,
                List.of(
                        publicGameStateSerde.serialize(newState),
                        playerStateSerde.serialize(ownState)),
                false);
    }

    @Override
    public void setInitialTicketChoice(SortedBag<Ticket> tickets) {
        networkInteractionHandler(
                MessageId.SET_INITIAL_TICKETS, List.of(ticketBagSerde.serialize(tickets)), false);
    }

    @Override
    public SortedBag<Ticket> chooseInitialTickets() {
        return networkInteractionHandler(MessageId.CHOOSE_INITIAL_TICKETS)
                .map(ticketBagSerde::deserialize)
                .orElseThrow(() -> new IllegalStateException("Expected response from network."));
    }

    @Override
    public TurnKind nextTurn() {
        return networkInteractionHandler(MessageId.NEXT_TURN)
                .map(turnKindSerde::deserialize)
                .orElseThrow(() -> new IllegalStateException("Expected response from network."));
    }

    @Override
    public SortedBag<Ticket> chooseTickets(SortedBag<Ticket> options) {
        return networkInteractionHandler(
                MessageId.CHOOSE_TICKETS, List.of(ticketBagSerde.serialize(options)), true)
                .map(ticketBagSerde::deserialize)
                .orElseThrow(() -> new IllegalStateException("Expected response from network."));
    }

    @Override
    public int drawSlot() {
        return networkInteractionHandler(MessageId.DRAW_SLOT)
                .map(intSerde::deserialize)
                .orElseThrow(() -> new IllegalStateException("Expected response from network."));
    }

    @Override
    public Route claimedRoute() {
        return networkInteractionHandler(MessageId.ROUTE)
                .map(routeSerde::deserialize)
                .orElseThrow(() -> new IllegalStateException("Expected response from network."));
    }

    @Override
    public SortedBag<Card> initialClaimCards() {
        return networkInteractionHandler(MessageId.CARDS)
                .map(cardBagSerde::deserialize)
                .orElseThrow(() -> new IllegalStateException("Expected response from network."));
    }

    @Override
    public SortedBag<Card> chooseAdditionalCards(List<SortedBag<Card>> options) {
        return networkInteractionHandler(
                MessageId.CHOOSE_ADDITIONAL_CARDS,
                List.of(listOfCardBagSerde.serialize(options)),
                        true)
                .map(cardBagSerde::deserialize)
                .orElseThrow(() -> new IllegalStateException("Expected response from network."));
    }

    /**
     * Handles the sending of method's corresponding message over the network.
     *
     * @param messageId The messageId of the message that will be sent.
     * @return An eventual response of the network. Not deserialized.
     * @throws UncheckedIOException in case of {@link IOException}.
     */
    private Optional<String> networkInteractionHandler(MessageId messageId) {
        return networkInteractionHandler(messageId, Collections.emptyList(), true);
    }

    /**
     * Handles the sending of method's corresponding message over the network.
     *
     * @param messageId      The messageId of the message that will be sent.
     * @param serializedArgs The arguments of the methods to be communicate.
     * @param awaitsResponse Wethet there is a need to wait a respsonse.
     * @return An eventual response of the network. Not deserialized.
     * @throws UncheckedIOException in case of {@link IOException}.
     */
    private Optional<String> networkInteractionHandler(
            MessageId messageId, List<String> serializedArgs, boolean awaitsResponse) {
        Objects.requireNonNull(serializedArgs);
        try {
            this.outRedirect.write(messageId.name() + NetConstants.SPACE);
            if (serializedArgs.size() > 0)
                this.outRedirect.write(String.join(NetConstants.SPACE, serializedArgs));
            this.outRedirect.write(NetConstants.END_LINE);
            this.outRedirect.flush();
            // The Optional will be empty if readLine returns null.
            String value = awaitsResponse ? this.inRedirect.readLine() : null;
            return Optional.ofNullable(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

// TODO update doc with @throws - update pls
// TODO replace IllegalStateException by IO expections
