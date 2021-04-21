package ch.epfl.tchu.net;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

import static ch.epfl.tchu.game.PlayerId.PLAYER_1;
import static ch.epfl.tchu.game.PlayerId.PLAYER_2;

/**
 * Class with all the useful Serdes.
 *
 * @author Luca Mouchel (324748)
 * @author Hugues Devimeux (327282)
 */
public final class Serdes {
    private static final String COMMA_SEPARATOR = ",";
    private static final String SEMI_COLON_SEPARATOR = ";";
    private static final String COLON_SEPARATOR = ":";

    /** Not instantiable.*/
    private Serdes() {}

    /** Serde for integers*/
    public static final Serde<Integer> intSerde =
            Serde.of(i -> Integer.toString(i), Integer::parseInt);
    /** Serde for Strings using Base64 class */
    public static final Serde<String> stringSerde =
            Serde.of(
                    object -> Base64.getEncoder().encodeToString(object.getBytes(StandardCharsets.UTF_8)),
                    str ->
                            Arrays.toString(
                                    Base64.getDecoder()
                                            .decode(
                                                    new String(
                                                            str.getBytes(StandardCharsets.UTF_8)))));
    /** Serde for PlayerId */
    public static final Serde<PlayerId> playerIdSerde = Serde.oneOf(PlayerId.ALL);
    /** Serde for TurnKind */
    public static final Serde<Player.TurnKind> turnKindSerde = Serde.oneOf(Player.TurnKind.ALL);
    /** Serde for Cards*/
    public static final Serde<Card> cardSerde = Serde.oneOf(Card.ALL);
    /** Serde for routes*/
    public static final Serde<Route> routeSerde = Serde.oneOf(ChMap.routes());
    /** Serde for Tickets*/
    public static final Serde<Ticket> ticketSerde = Serde.oneOf(ChMap.tickets());
    /** Serde for Lists of strings*/
    public static final Serde<List<String>> stringListSerde =
            Serde.listOf(stringSerde, COMMA_SEPARATOR);

    /** Serde for Lists of cards*/
    public static final Serde<List<Card>> cardListSerde = Serde.listOf(cardSerde, COMMA_SEPARATOR);
    /** Serde for Lists of routes*/
    public static final Serde<List<Route>> routeListSerde =
            Serde.listOf(routeSerde, COMMA_SEPARATOR);

    /** Serde for sorted bags of cards*/
    public static final Serde<SortedBag<Card>> cardBagSerde =
            Serde.bagOf(cardSerde, COMMA_SEPARATOR);
    /** Serde for sorted bags of tickets*/
    public static final Serde<SortedBag<Ticket>> ticketBagSerde =
            Serde.bagOf(ticketSerde, COMMA_SEPARATOR);
    /** Serde for Lists of SortedBags of cards*/
    public static final Serde<List<SortedBag<Card>>> listOfCardBagSerde =
            Serde.listOf(cardBagSerde, SEMI_COLON_SEPARATOR);

    /** Serde for public card states*/
    public static final Serde<PublicCardState> publicCardStateSerde =
            Serde.of(
                    (object) -> // join each element from the constructor of Public
                            //cardState with ";"
                            String.join(
                                    SEMI_COLON_SEPARATOR,
                                    cardListSerde.serialize(object.faceUpCards()),
                                    intSerde.serialize(object.deckSize()),
                                    intSerde.serialize(object.discardsSize())),
                    (str) -> {
                        //to deserialize, split the string with ";" and deserialize each element
                        //the numbers 0,1,2 are indexes to correspond to the index of the attributes of
                        // the constructor of the class PublicCardState -
                        // ie index 0 -> attribute List<Cards>faceUpCards -> use of cardListSerde
                        String[] elements = str.split(Pattern.quote(SEMI_COLON_SEPARATOR), -1);
                        return new PublicCardState(
                                cardListSerde.deserialize(elements[0]),
                                intSerde.deserialize(elements[1]),
                                intSerde.deserialize(elements[2]));
                    });

    /** Serde for public player states*/
    public static final Serde<PublicPlayerState> publicPlayerStateSerde =
            //same principle as the previous Serde, join each serialized item
            //from the constructor of PublicPlayerState by ";"
            Serde.of(
                    (i) ->
                            String.join(
                                    SEMI_COLON_SEPARATOR,
                                    intSerde.serialize(i.ticketCount()),
                                    intSerde.serialize(i.cardCount()),
                                    routeListSerde.serialize(i.routes())),
                    (s) -> {
                        //same principle as the previous Serde, split the string with ";"
                        // and deserialize each element. the numbers 0,1,2 are indexes
                        // that correspond to the index of the attributes of the constructor
                        // of the class PublicCardState
                        String[] elements = s.split(Pattern.quote(SEMI_COLON_SEPARATOR), -1);
                        return new PublicPlayerState(
                                intSerde.deserialize(elements[0]),
                                intSerde.deserialize(elements[1]),
                                //however now we have to verify if the third attribute
                                //ie the list of routes is empty or not
                                //if it is, we must use an emptyList, otherwise we deserialize using
                                //routeListSerde
                                verifyIfListIsEmpty(elements, routeListSerde, 2));
                    });

    /** Serde for PlayerStates*/
    public static final Serde<PlayerState> playerStateSerde =
            //functions in the same way as the previous Serdes.
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
                                //we also have to verify if we should use an empty list or deserialize
                                //using routeListSerde
                                verifyIfListIsEmpty(elements, routeListSerde, 2));
                    });

    /** Serde for public game states*/
    public static final Serde<PublicGameState> publicGameStateSerde =
            Serde.of( //same principle, join each serialized element with ":"
                    //there is a certain order to serialize elements in so we can use hard
                    // coded elements such as PLAYER_1: Cf paper
                    (i) ->
                            String.join(
                                    COLON_SEPARATOR,
                                    intSerde.serialize(i.ticketsCount()),
                                    publicCardStateSerde.serialize(i.cardState()),
                                    playerIdSerde.serialize(i.currentPlayerId()),
                                    publicPlayerStateSerde.serialize(i.playerState(PLAYER_1)),
                                    publicPlayerStateSerde.serialize(i.playerState(PLAYER_2)),
                                    //if the last player is null, we serialize an empty string
                                    //otherwise we serialize the lastPlayer using playerIdSerde
                                    i.lastPlayer() == null
                                            ? stringSerde.serialize("")
                                            : playerIdSerde.serialize(i.lastPlayer())),
                    (s) -> {
                        //we split the string but now we have an array of 5 elements
                        //in order, these are the attributes of the constructor of
                        //PublicGameState.java
                        //index 0 -> int ticketsCount, index 1 -> PublicCardState
                        // index 2 -> currentPlayerId, index 3,4 -> publicPlayerStates to
                        //put into a map. index 5 -> lastPlayerId
                        String[] elements = s.split(Pattern.quote(COLON_SEPARATOR), -1);
                        PublicPlayerState firstPS = publicPlayerStateSerde.deserialize(elements[3]);
                        PublicPlayerState secondPS = publicPlayerStateSerde.deserialize(elements[4]);
                        //we verify if the last player is null or not
                        PlayerId lastPlayer =
                                elements[5].isEmpty()
                                        ? null
                                        : playerIdSerde.deserialize(elements[5]);
                        Map<PlayerId, PublicPlayerState> map =
                                Map.of(PLAYER_1, firstPS, PLAYER_2, secondPS);
                        return new PublicGameState(
                                intSerde.deserialize(elements[0]),
                                publicCardStateSerde.deserialize(elements[1]),
                                playerIdSerde.deserialize(elements[2]),
                                map,
                                lastPlayer);
                    });

    //private method that evaluates if the string in elements at the given index is empty or not
    //returns an EmptyList if it is and the deserialized element if not.
    private static <T> List<T> verifyIfListIsEmpty(String[] elements, Serde<List<T>> serde, int index) {
        return elements[index].isEmpty() ? Collections.emptyList() : serde.deserialize(elements[index]);
    }
}
