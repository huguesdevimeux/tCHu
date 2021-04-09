package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Representation of a route that links two nearby stations.
 *
 * @author Luca Mouchel (324748)
 * @author Hugues Devimeux (327282)
 */
public final class Route {
    private final String id;
    private final Station station1;
    private final Station station2;
    private final int length;
    private final Level level;
    private final Color color;

    /**
     * Route constructor.
     *
     * @param id Routes' identity
     * @param station1 First station of the route
     * @param station2 Second station of the route
     * @param length Length of the route
     * @param level Defines what type of route it is
     * @param color Can be any color
     * @throws IllegalArgumentException If stations 1 and 2 are the same or if length is out of
     *     bounds defined by Constants.java
     * @throws NullPointerException if either id, station1, station2 or level are null
     */
    public Route(
            String id, Station station1, Station station2, int length, Level level, Color color) {
        this.id = Objects.requireNonNull(id);
        this.station1 = Objects.requireNonNull(station1);
        this.station2 = Objects.requireNonNull(station2);
        this.level = Objects.requireNonNull(level);
        this.length = length;
        this.color = color;
        Preconditions.checkArgument(!(station1.equals(station2)));
        Preconditions.checkArgument(
                length >= Constants.MIN_ROUTE_LENGTH && length <= Constants.MAX_ROUTE_LENGTH);
    }

    /**
     * Returns routes' id.
     *
     * @return id
     */
    public String id() {
        return id;
    }

    /**
     * Returns station1.
     *
     * @return station1
     */
    public Station station1() {
        return station1;
    }

    /**
     * Returns station2.
     *
     * @return station2
     */
    public Station station2() {
        return station2;
    }

    /**
     * Returns routes' length.
     *
     * @return length
     */
    public int length() {
        return length;
    }

    /**
     * Returns the routes' level: underground or overground.
     *
     * @return level
     */
    public Level level() {
        return level;
    }

    /**
     * Returns the routes' color or null if the color is plain.
     *
     * @return color
     */
    public Color color() {
        return color;
    }

    /**
     * Returns List of the two stations in the order they appear in the constructor.
     *
     * @return list of the two stations
     */
    public List<Station> stations() {
        return List.of(station1, station2);
    }

    /**
     * Returns the opposite station from which this method is called.
     *
     * @param station
     * @return opposite station
     * @throws IllegalArgumentException if argument station is neither of the start/end stations
     */
    public Station stationOpposite(Station station) {
        Preconditions.checkArgument((station.equals(station1) || station.equals(station2)));
        return station.equals(station1) ? station2 : station1;
    }

    /**
     * Returns List of all the possible cards one can use to take over a route.
     *
     * @return possible claim cards (playable cards)
     */
    public List<SortedBag<Card>> possibleClaimCards() {
        List<Card> cardList = new ArrayList<>();
        List<SortedBag<Card>> cardBag = new ArrayList<>();

        if (level.equals(Level.OVERGROUND)) {
            // when route is overground locomotive cards cannot be used
            if (color == null) {
                for (Card card : Card.CARS) {
                    IntStream.range(0, length).forEach(y -> cardList.add(card));
                    /// adding all the cars added to cardBuilder into cardBag
                    cardBag.add(SortedBag.of(cardList));
                    // resetting cardBuilder to prevent from having subArrays of cardBag to have
                    // more than lengths' elements
                    cardList.clear();
                }
            } else {
                IntStream.range(0, length).forEach(y -> cardList.add(Card.of(this.color)));
                cardBag.add(SortedBag.of(cardList));
            }

        } else {
            // if level is UNDERGROUND, Locomotive cards come into play.
            if (color == null) {
                for (int i = this.length; i > 0; i--) {
                    for (Card card : Card.CARS) {
                        // same instructions as before
                        IntStream.range(0, i).forEach(y -> cardList.add(card));
                        // adding locomotive cards to complete all the possible claim cards
                        // when route is a tunnel
                        while (cardList.size() < length) cardList.add(Card.LOCOMOTIVE);
                        cardBag.add(SortedBag.of(cardList));
                        cardList.clear();
                    }
                }

            } else {
                for (int i = this.length; i > 0; i--) {
                    // same instructions but the color here does not matter
                    // we just assign the given color
                    IntStream.range(0, i).forEach(y -> cardList.add(Card.of(this.color)));
                    while (cardList.size() < length) cardList.add(Card.LOCOMOTIVE);
                    cardBag.add(SortedBag.of(cardList));
                    cardList.clear();
                }
            }
            // this single for loop allows to add the final subArray in the list with ONLY
            // locomotive cards
            IntStream.range(0, length).forEach(y -> cardList.add(Card.LOCOMOTIVE));
            cardBag.add(SortedBag.of(cardList));
        }
        return cardBag;
    }

    /**
     * Returns the additional amount of cards one must play to take over a route knowing that the
     * player has played with <code> claimCards </code> and the three cards taken from the stack of
     * cards are the <code> drawnCards </code>
     *
     * @param claimCards cards the player uses to play
     * @param drawnCards 3 cards drawn from the stack of cards
     * @return number of additional cards the player must play to take over route
     * @throws IllegalArgumentException if the route is not a tunnel
     * @throws IllegalArgumentException if <\code> drawnCards </\code> does not contain exactly
     *     three cards
     */
    public int additionalClaimCardsCount(SortedBag<Card> claimCards, SortedBag<Card> drawnCards) {
        Preconditions.checkArgument(level.equals(Level.UNDERGROUND));
        Preconditions.checkArgument(drawnCards.size() == 3);
        int additionalClaimCards = 0;
        // adding the number  of locomotive cards in the drawn cards
        additionalClaimCards += drawnCards.stream().filter(Card.LOCOMOTIVE::equals).count();
        for (Card drawn : drawnCards)
            additionalClaimCards +=
                    claimCards.stream()
                            .distinct()
                            .filter(claim -> !claim.equals(Card.LOCOMOTIVE) && claim.equals(drawn))
                            .count();
        return additionalClaimCards;
    }

    /**
     * Returns the number of claimPoints (construction points) that a player gets when taking over a
     * route. Depends on the routes' length.
     *
     * @return claimPoints
     */
    public int claimPoints() {
        return Constants.ROUTE_CLAIM_POINTS.get(length);
    }

    /** Enum defines the type of route. */
    public enum Level {
        OVERGROUND,
        UNDERGROUND
    }
}
