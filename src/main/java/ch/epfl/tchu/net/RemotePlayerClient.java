package ch.epfl.tchu.net;

import static ch.epfl.tchu.net.NetConstants.COMMA_SEPARATOR;
import static ch.epfl.tchu.net.Serdes.*;

import ch.epfl.tchu.game.Player;
import ch.epfl.tchu.game.PlayerId;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Represents a remote player.
 *
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public final class RemotePlayerClient {

    private final Player player;
    private final String host;
    private final int port;

    public RemotePlayerClient(Player player, String host, int port) {
        this.player = player;
        this.host = host;
        this.port = port;
    }

    /** Run the client. Handles the game process through the network. */
    public void run() {
        try (Socket s = new Socket(host, port);
                BufferedReader inReader =
                        new BufferedReader(
                                new InputStreamReader(s.getInputStream(), NetConstants.ENCODING));
                BufferedWriter outWriter =
                        new BufferedWriter(
                                new OutputStreamWriter(
                                        s.getOutputStream(), NetConstants.ENCODING))) {

            String respFromNetwork = inReader.readLine();

            while (respFromNetwork != null) {
                List<String> splitResp =
                        new ArrayList<>(
                                Arrays.asList(
                                        respFromNetwork.split(
                                                Pattern.quote(NetConstants.SPACE), -1)));
                MessageId messageId = MessageId.valueOf(splitResp.get(0));
                splitResp.remove(0);
                Optional<String> toSendBack = handleClientResponse(messageId, splitResp);
                toSendBack.ifPresent(
                        s1 -> {
                            try {
                                outWriter.write(s1);
                                outWriter.write(NetConstants.ENDLINE);
                                outWriter.flush();
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });
                // Response for the next iteration.
                respFromNetwork = inReader.readLine();
            }

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Handles the communication through the network and return an Optional that depicts whether the
     * client should send back something to the server.
     *
     * @param messageId The type of message.
     * @param args The arguments SERIALIZED.
     * @return An optional present if there is a value to return.
     * @throws IllegalStateException if messageId is not registered.
     */
    private Optional<String> handleClientResponse(MessageId messageId, List<String> args) {
        switch (messageId) {
            case INIT_PLAYERS:
                // Deserializer handmade. See RemotePlayerProxy l. 48 for serializer implementation.
                List<String> playersSerialized =
                        List.of(args.get(1).split(Pattern.quote(COMMA_SEPARATOR), -1)).stream()
                                .map(stringSerde::deserialize)
                                .collect(Collectors.toList());

                player.initPlayers(
                        playerIdSerde.deserialize(args.get(0)),
                        Map.of(
                                PlayerId.PLAYER_1,
                                playersSerialized.get(0),
                                PlayerId.PLAYER_2,
                                playersSerialized.get(1)));

                return Optional.empty();
            case RECEIVE_INFO:
                player.receiveInfo(stringSerde.deserialize(args.get(0)));
                return Optional.empty();
            case UPDATE_STATE:
                player.updateState(
                        publicGameStateSerde.deserialize(args.get(0)),
                        playerStateSerde.deserialize(args.get(1)));
                return Optional.empty();
            case SET_INITIAL_TICKETS:
                player.setInitialTicketChoice(ticketBagSerde.deserialize(args.get(0)));
                return Optional.empty();
            case CHOOSE_INITIAL_TICKETS:
                return Optional.of(ticketBagSerde.serialize(player.chooseInitialTickets()));
            case NEXT_TURN:
                return Optional.of(turnKindSerde.serialize(player.nextTurn()));
            case CHOOSE_TICKETS:
                return Optional.of(
                        ticketBagSerde.serialize(
                                player.chooseTickets(ticketBagSerde.deserialize(args.get(0)))));
            case DRAW_SLOT:
                return Optional.of(intSerde.serialize(player.drawSlot()));
            case ROUTE:
                return Optional.of(routeSerde.serialize(player.claimedRoute()));
            case CARDS:
                return Optional.of(cardBagSerde.serialize(player.initialClaimCards()));
            case CHOOSE_ADDITIONAL_CARDS:
                return Optional.of(
                        cardBagSerde.serialize(
                                player.chooseAdditionalCards(
                                        listOfCardBagSerde.deserialize(args.get(0)))));
            default:
                throw new IllegalStateException();
        }
    }
}
