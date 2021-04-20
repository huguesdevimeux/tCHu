package ch.epfl.tchu.net;

import static ch.epfl.tchu.game.PlayerId.PLAYER_1;
import static ch.epfl.tchu.game.PlayerId.PLAYER_2;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Serdes {
    private static final String COMMA_SEPARATOR = ",";
    private static final String SEMI_COLON_SEPARATOR = ";";
    private static final String COLON_SEPARATOR = ":";

    private Serdes() {}

    public static final Serde<Integer> intSerde =
            Serde.of(i -> Integer.toString(i), Integer::parseInt);
    public static final Serde<String> stringSerde =
            Serde.of(
                    i -> Base64.getEncoder().encodeToString(i.getBytes(StandardCharsets.UTF_8)),
                    s ->
                            Arrays.toString(
                                    Base64.getDecoder()
                                            .decode(
                                                    new String(
                                                            s.getBytes(StandardCharsets.UTF_8)))));
    public static final Serde<PlayerId> playerIdSerde = Serde.oneOf(PlayerId.ALL);
    public static final Serde<Player.TurnKind> turnKindSerde = Serde.oneOf(Player.TurnKind.ALL);
    public static final Serde<Card> cardSerde = Serde.oneOf(Card.ALL);
    public static final Serde<Route> routeSerde = Serde.oneOf(ChMap.routes());
    public static final Serde<Ticket> ticketSerde = Serde.oneOf(ChMap.tickets());
    public static final Serde<List<String>> stringListSerde =
            Serde.listOf(stringSerde, COMMA_SEPARATOR);
    public static final Serde<List<Card>> cardListSerde = Serde.listOf(cardSerde, COMMA_SEPARATOR);
    public static final Serde<List<Route>> routeListSerde =
            Serde.listOf(routeSerde, COMMA_SEPARATOR);
    public static final Serde<SortedBag<Card>> cardBagSerde =
            Serde.bagOf(cardSerde, COMMA_SEPARATOR);
    public static final Serde<SortedBag<Ticket>> ticketBagSerde =
            Serde.bagOf(ticketSerde, COMMA_SEPARATOR);
    public static final Serde<List<SortedBag<Card>>> listOfCardBagSerde =
            Serde.listOf(cardBagSerde, SEMI_COLON_SEPARATOR);
    public static final Serde<PublicCardState> publicCardStateSerde =
            Serde.of(
                    i ->
                            String.join(
                                    SEMI_COLON_SEPARATOR,
                                    cardListSerde.serialize(i.faceUpCards()),
                                    intSerde.serialize(i.deckSize()),
                                    intSerde.serialize(i.discardsSize())),
                    (s) -> {
                        String[] elements = s.split(Pattern.quote(SEMI_COLON_SEPARATOR), -1);
                        return new PublicCardState(
                                cardListSerde.deserialize(elements[0]),
                                intSerde.deserialize(elements[1]),
                                intSerde.deserialize(elements[2]));
                    });

    public static final Serde<PublicPlayerState> publicPlayerStateSerde =
            Serde.of(
                    i ->
                            String.join(
                                    SEMI_COLON_SEPARATOR,
                                    intSerde.serialize(i.ticketCount()),
                                    intSerde.serialize(i.cardCount()),
                                    routeListSerde.serialize(i.routes())),
                    (s) -> {
                        String[] elements = s.split(Pattern.quote(SEMI_COLON_SEPARATOR), -1);
                        return new PublicPlayerState(
                                intSerde.deserialize(elements[0]),
                                intSerde.deserialize(elements[1]),
                                routeListSerde.deserialize(elements[2]));
                    });

    public static final Serde<PlayerState> playerStateSerde =
            Serde.of(
                    (i) ->
                            String.join(
                                    SEMI_COLON_SEPARATOR,
                                    ticketBagSerde.serialize(i.tickets()),
                                    cardBagSerde.serialize(i.cards()),
                                    routeListSerde.serialize(i.routes())),
                    (s) -> {
                        String[] elements = s.split(Pattern.quote(SEMI_COLON_SEPARATOR), -1);
                        return new PlayerState(
                                ticketBagSerde.deserialize(elements[0]),
                                cardBagSerde.deserialize(elements[1]),
                                routeListSerde.deserialize(elements[2]));
                    });

    public static Serde<PublicGameState> publicGameStateSerde =
            Serde.of(
                    i ->
                            String.join(
                                    COLON_SEPARATOR,
                                    intSerde.serialize(i.ticketsCount()),
                                    publicCardStateSerde.serialize(i.cardState()),
                                    playerIdSerde.serialize(i.currentPlayerId()),
                                    publicPlayerStateSerde.serialize(i.playerState(PLAYER_1)),
                                    publicPlayerStateSerde.serialize(i.playerState(PLAYER_2)),
                                    i.lastPlayer() == null
                                            ? stringSerde.serialize("")
                                            : playerIdSerde.serialize(i.lastPlayer())),
                    s -> {
                        String[] elements = s.split(Pattern.quote(COLON_SEPARATOR), -1);
                        PublicPlayerState firstPlayerState =
                                publicPlayerStateSerde.deserialize(elements[3] + "0");
                        PublicPlayerState secondPlayerState =
                                publicPlayerStateSerde.deserialize(elements[4] + "0");
                        PlayerId lastPlayer =
                                elements[5].isEmpty()
                                        ? null
                                        : playerIdSerde.deserialize(elements[5]);
                        Map<PlayerId, PublicPlayerState> map =
                                Map.of(PLAYER_1, firstPlayerState, PLAYER_2, secondPlayerState);
                        return new PublicGameState(
                                intSerde.deserialize(elements[0]),
                                publicCardStateSerde.deserialize(elements[1]),
                                playerIdSerde.deserialize(elements[2]),
                                map,
                                lastPlayer);
                    });
}
