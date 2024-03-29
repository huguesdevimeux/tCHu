package ch.epfl.tchu.net;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;

import java.util.Base64;
import java.util.List;
import java.util.Map;
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

    /** Serde for integers */
    public static final Serde<Integer> intSerde =
            Serde.of(i -> Integer.toString(i), Integer::parseInt);

    /** Not instantiable. */
    private Serdes() {}
    /** Serde for Strings using Base64 class */
    public static final Serde<String> stringSerde =
            Serde.of(
                    obj -> Base64.getEncoder().encodeToString(obj.getBytes(NetConstants.Serdes.ENCODING)),
                    str ->
                            new String(
                                    Base64.getDecoder().decode(str.getBytes()),
                                    NetConstants.Serdes.ENCODING));
    /** Serde for PlayerId */
    public static final Serde<PlayerId> playerIdSerde = Serde.oneOf(PlayerId.ALL);
    /** Serde for TurnKind */
    public static final Serde<Player.TurnKind> turnKindSerde = Serde.oneOf(Player.TurnKind.ALL);
    /** Serde for Cards */
    public static final Serde<Card> cardSerde = Serde.oneOf(Card.ALL);
    /** Serde for routes */
    public static final Serde<Route> routeSerde = Serde.oneOf(ChMap.routes());
    /** Serde for Tickets */
    public static final Serde<Ticket> ticketSerde = Serde.oneOf(ChMap.tickets());
    /** Serde for Lists of strings to separate with "," */
    public static final Serde<List<String>> stringListSerde =
            Serde.listOf(stringSerde, NetConstants.Serdes.SEPARATOR_1);
    /** Serde for Lists of cards to separate with "," */
    public static final Serde<List<Card>> cardListSerde = Serde.listOf(cardSerde, NetConstants.Serdes.SEPARATOR_1);
    /** Serde for Lists of routes to separate with "," */
    public static final Serde<List<Route>> routeListSerde =
            Serde.listOf(routeSerde, NetConstants.Serdes.SEPARATOR_1);
    /** Serde for sorted bags of cards to separate with "," */
    public static final Serde<SortedBag<Card>> cardBagSerde =
            Serde.bagOf(cardSerde, NetConstants.Serdes.SEPARATOR_1);
    /** Serde for sorted bags of tickets to separate with "," */
    public static final Serde<SortedBag<Ticket>> ticketBagSerde =
            Serde.bagOf(ticketSerde, NetConstants.Serdes.SEPARATOR_1);
    /** Serde for Lists of SortedBags of cards to separate with ";" */
    public static final Serde<List<SortedBag<Card>>> listOfCardBagSerde =
            Serde.listOf(cardBagSerde, NetConstants.Serdes.SEPARATOR_2);
    /** Serde for public card states to separate with ";" */
    public static final Serde<PublicCardState> publicCardStateSerde =
            Serde.of(
                    (publicCardState) -> // join each element from the constructor of Public
                            // cardState with ";"
                            String.join(
								NetConstants.Serdes.SEPARATOR_2,
                                    cardListSerde.serialize(publicCardState.faceUpCards()),
                                    intSerde.serialize(publicCardState.deckSize()),
                                    intSerde.serialize(publicCardState.discardsSize())),
                    (str) -> {
                        // to deserialize, split the string with ";" and deserialize each element
                        // the numbers 0,1,2 are indexes to correspond to the index of the
                        // attributes of the constructor of the class PublicCardState -
                        // ie index 0 -> attribute List<Cards>faceUpCards -> use of cardListSerde
                        String[] elements = str.split(Pattern.quote(NetConstants.Serdes.SEPARATOR_2), -1);
                        return new PublicCardState(
                                cardListSerde.deserialize(elements[0]),
                                intSerde.deserialize(elements[1]),
                                intSerde.deserialize(elements[2]));
                    });
    /** Serde for public player states to separate with ";" */
    public static final Serde<PublicPlayerState> publicPlayerStateSerde =
            // same principle as the previous Serde, join each serialized item
            // from the constructor of PublicPlayerState by ";"
            Serde.of(
                    (publicPlayerState) ->
                            String.join(
								NetConstants.Serdes.SEPARATOR_2,
                                    intSerde.serialize(publicPlayerState.ticketCount()),
                                    intSerde.serialize(publicPlayerState.cardCount()),
                                    routeListSerde.serialize(publicPlayerState.routes())),
                    (str) -> {
                        // same principle as the previous Serde, split the string with ";"
                        // and deserialize each element. the numbers 0,1,2 are indexes
                        // that correspond to the index of the attributes of the constructor
                        // of the class PublicCardState
                        String[] elements = str.split(Pattern.quote(NetConstants.Serdes.SEPARATOR_2), -1);
                        return new PublicPlayerState(
                                intSerde.deserialize(elements[0]),
                                intSerde.deserialize(elements[1]),
                                routeListSerde.deserialize(elements[2]));
                    });
    /** Serde for PlayerStates to separate with ";" */
    public static final Serde<PlayerState> playerStateSerde =
            // functions in the same way as the previous Serdes.
            Serde.of(
                    (playerState) ->
                            String.join(
								NetConstants.Serdes.SEPARATOR_2,
                                    ticketBagSerde.serialize(playerState.tickets()),
                                    cardBagSerde.serialize(playerState.cards()),
                                    routeListSerde.serialize(playerState.routes())),
                    (str) -> {
                        String[] elements = str.split(Pattern.quote(NetConstants.Serdes.SEPARATOR_2), -1);
                        return new PlayerState(
                                ticketBagSerde.deserialize(elements[0]),
                                cardBagSerde.deserialize(elements[1]),
                                routeListSerde.deserialize(elements[2]));
                    });
    /** Serde for public game states to separate with ":" */
    public static final Serde<PublicGameState> publicGameStateSerde =
            Serde.of( // same principle, join each serialized element with ":"
                    // there is a certain order to serialize elements in so we can use hard
                    // coded elements such as PLAYER_1: Cf paper
                    (publicGameState) ->
                            String.join(
								NetConstants.Serdes.SEPARATOR_3,
                                    intSerde.serialize(publicGameState.ticketsCount()),
                                    publicCardStateSerde.serialize(publicGameState.cardState()),
                                    playerIdSerde.serialize(publicGameState.currentPlayerId()),
                                    publicPlayerStateSerde.serialize(
                                            publicGameState.playerState(PLAYER_1)),
                                    publicPlayerStateSerde.serialize(
                                            publicGameState.playerState(PLAYER_2)),
                                    playerIdSerde.serialize(publicGameState.lastPlayer())),
      
                    (str) -> {
                        // we split the string but now we have an array of 5 elements
                        // in order, these are the attributes of the constructor of
                        // PublicGameState.java
                        // index 0 -> int ticketsCount, index 1 -> PublicCardState
                        // index 2 -> currentPlayerId, index 3,4 -> publicPlayerStates to
                        // put into a map. index 5 -> lastPlayerId
                        String[] elements = str.split(Pattern.quote(NetConstants.Serdes.SEPARATOR_3), -1);
                        PublicPlayerState firstPS = publicPlayerStateSerde.deserialize(elements[3]);
                        PublicPlayerState secondPS =
                                publicPlayerStateSerde.deserialize(elements[4]);
                        // An empty string is deserialized as a null player.
                        Map<PlayerId, PublicPlayerState> map =
                                Map.of(PLAYER_1, firstPS, PLAYER_2, secondPS);
                        return new PublicGameState(
                                intSerde.deserialize(elements[0]),
                                publicCardStateSerde.deserialize(elements[1]),
                                playerIdSerde.deserialize(elements[2]),
                                map,
                                playerIdSerde.deserialize(elements[5]));
                    });
}
