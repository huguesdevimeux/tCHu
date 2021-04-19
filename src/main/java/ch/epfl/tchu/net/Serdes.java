package ch.epfl.tchu.net;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class Serdes {
    private static final String COMMA_SEPARATOR = ",";
    private static final String SEMI_COLON_SEPARATOR = ";";
    private static final String COLON_SEPARATOR = ":";

    private Serdes() {
    }

    public static final Serde<Integer> intSerde = Serde.of(i -> Integer.toString(i), Integer::parseInt);
    public static final Serde<String> stringSerde = Serde.of(
            i -> Base64.getEncoder().encodeToString(i.getBytes(StandardCharsets.UTF_8)),
            s -> Arrays.toString(Base64.getDecoder().decode(new String(s.getBytes(StandardCharsets.UTF_8)))));


    public static final Serde<PlayerId> playerIdSerde = Serde.oneOf(PlayerId.ALL);

    public static final Serde<Player.TurnKind> turnKindSerde = Serde.oneOf(Player.TurnKind.ALL);

    public static final Serde<Card> cardSerde = Serde.oneOf(Card.ALL);

    public static final Serde<Route> routeSerde = Serde.oneOf(ChMap.routes());

    public static final Serde<Ticket> ticketSerde = Serde.oneOf(ChMap.tickets());


    public static final Serde<List<String>> stringListSerde = Serde.listOf(stringSerde, COMMA_SEPARATOR);

    public static final Serde<List<Card>> cardListSerde = Serde.listOf(cardSerde, COMMA_SEPARATOR);


    public static final Serde<List<Route>> routeListSerde = Serde.listOf(routeSerde, COMMA_SEPARATOR);

    public static final Serde<SortedBag<Card>> cardBagSerde = Serde.bagOf(cardSerde, COMMA_SEPARATOR);

    public static final Serde<SortedBag<Ticket>> ticketBagSerde = Serde.bagOf(ticketSerde, COMMA_SEPARATOR);

    public static final Serde<List<SortedBag<Card>>> listOfCardBagSerde = Serde.listOf(cardBagSerde, COMMA_SEPARATOR), SEMI_COLON_SEPARATOR);


    public static final Serde<PublicCardState> publicCardStateSerde = Serde.listOf( , SEMI_COLON_SEPARATOR);

    public static final Serde<PublicPlayerState> publicPlayerStateSerde = Serde.of(, SEMI_COLON_SEPARATOR);


    public static final Serde<PlayerState> playerStateSerde = Serde.of(, SEMI_COLON_SEPARATOR);


    public static final Serde<PublicGameState> publicGameStateSerde = Serde.of(, COLON_SEPARATOR);
}

