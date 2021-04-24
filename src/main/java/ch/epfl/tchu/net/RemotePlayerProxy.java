package ch.epfl.tchu.net;

import static ch.epfl.tchu.net.Serdes.*;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;

import java.io.*;
import java.net.Socket;
import java.util.*;

/**
 * Represents a proxy player. Meant to be used by {@link ch.epfl.tchu.game.Game} as a normal Player.
 *
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public class RemotePlayerProxy implements Player {

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
                            new OutputStreamWriter(socket.getOutputStream(), NetConst.ENCODING));
            this.inRedirect =
                    new BufferedReader(
                            new InputStreamReader(socket.getInputStream(), NetConst.ENCODING));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void initPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        // map serialized (handmade with love)
        String playersNamesSerialized =
                String.join(
                        COMMA_SEPARATOR,
                        stringSerde.serialize(playerNames.get(PlayerId.PLAYER_1)),
                        stringSerde.serialize(playerNames.get(PlayerId.PLAYER_2)));
        networkInteractionHandler(
                MessageId.INIT_PLAYERS,
                List.of(playerIdSerde.serialize(ownId), playersNamesSerialized));
    }

    @Override
    public void receiveInfo(String info) {
        networkInteractionHandler(MessageId.RECEIVE_INFO, List.of(stringSerde.serialize(info)));
    }

    @Override
    public void updateState(PublicGameState newState, PlayerState ownState) {
        networkInteractionHandler(
                MessageId.UPDATE_STATE,
                List.of(
                        publicGameStateSerde.serialize(newState),
                        playerStateSerde.serialize(ownState)));
    }

    @Override
    public void setInitialTicketChoice(SortedBag<Ticket> tickets) {
        networkInteractionHandler(
                MessageId.SET_INITIAL_TICKETS, List.of(ticketBagSerde.serialize(tickets)));
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
        return networkInteractionHandler(MessageId.CHOOSE_TICKETS)
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
                        List.of(listOfCardBagSerde.serialize(options)))
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
        return networkInteractionHandler(messageId, Collections.emptyList());
    }

    /**
     * Handles the sending of method's corresponding message over the network.
     *
     * @param messageId The messageId of the message that will be sent.
     * @param serializedArgs The arguments of the methods to be communicate.
     * @return An eventual response of the network. Not deserialized.
     * @throws UncheckedIOException in case of {@link IOException}.
     */
    private Optional<String> networkInteractionHandler(
            MessageId messageId, List<String> serializedArgs) {
        Objects.requireNonNull(serializedArgs);
        try {
            this.outRedirect.write(messageId.name() + NetConst.SPACE);
            if (serializedArgs.size() > 0)
                this.outRedirect.write(String.join(NetConst.SPACE, serializedArgs));
            this.outRedirect.write(NetConst.SPACE + NetConst.ENDLINE);
            this.outRedirect.flush();
            // TODO difference between NO RESPONSE and EMPTY RESPONSE
            return Optional.ofNullable(this.inRedirect.readLine());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

// TODO : test with empty sorted bag
// TODO : document messageId class attributes.
// TODO update doc with @throws
// TODO replace IllegalStateException by IO expections
