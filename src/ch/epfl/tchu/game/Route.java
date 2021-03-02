package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    /** Enum defines the type of route. */
    public enum Level {
        OVERGROUND,
        UNDERGROUND
    }

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
     * @throws IllegalArgumentException if argument station is neither of the start/end stations
     * @return opposite station
     */
    public Station stationOpposite(Station station) {
        Preconditions.checkArgument((station.equals(station1) || station.equals(station2)));
        if (station.equals(station1)) {
            return station2;
        } else {
            return station1;
        }
    }

    /**
     * Returns List of all the possible cards one can use to take over a route.
     *
     * @return possible claim cards (playable cards)
     */
    public List<SortedBag<Card>> possibleClaimCards() {
        SortedBag.Builder<Card> cardBuilder = new SortedBag.Builder<>();
        List<SortedBag<Card>> cardList = new ArrayList<>();

        if (level.equals(Level.OVERGROUND)) {
            // when route is overground locomotive cards cannot be used
            if (color == null) {
                for (Card cards : Card.CARS) {
                    for (int i = 0; i < this.length; i++) {
                        cardBuilder.add(cards);
                    }
                    /// adding all the cars that were added to cardBuilder into cardList
                    cardList.add(cardBuilder.build());
                    // resetting cardBuilder to prevent from having subArrays of cardList to have
                    // more
                    // than lengths' elements
                    cardBuilder = new SortedBag.Builder<>();
                }
            } else {
                for (int j = 0; j < this.length; j++) {
                    // if color is assigned, we just add the number of length of the route with the
                    // given colors
                    cardBuilder.add(Card.of(this.color));
                }
                cardList.add(cardBuilder.build());
            }

        } else {
            // if <\code> level </\code> is <\code> OVERGROUND </\code>, Locomotive cards come into
            // play
            if (color == null) {
                for (int i = this.length; i > 0; i--) {
                    for (Card all : Card.CARS) {
                        // same instructions as before
                        for (int j = 0; j < i; j++) {
                            cardBuilder.add(all);
                        }
                        // adding locomotive cards to complete all the possible claim cards
                        // when route is a tunnel
                        while (cardBuilder.size() < length) {
                            cardBuilder.add(Card.LOCOMOTIVE);
                        }
                        cardList.add(cardBuilder.build());
                        cardBuilder = new SortedBag.Builder<>();
                    }
                }

            } else {
                for (int i = this.length; i > 0; i--) {
                    for (int j = 0; j < i; j++) {
                        // same instructions but the color here does not matter
                        // we just assign the color that is given
                        cardBuilder.add(Card.of(this.color));
                    }
                    while (cardBuilder.size() < length) {
                        cardBuilder.add(Card.LOCOMOTIVE);
                    }
                    cardList.add(cardBuilder.build());
                    cardBuilder = new SortedBag.Builder<>();
                }
            }
            // this single for loop allows to add the subArray in the list with 2 locomotive cards
            for (int i = 0; i < this.length; i++) {
                cardBuilder.add(Card.LOCOMOTIVE);
            }
            cardList.add(cardBuilder.build());
        }
        return cardList;
    }

    /**
     * Returns the additional amount of cards one must play to take over a route knowing that the
     * player has played with <code> claimCards </code> and the three cards taken from the stack of
     * cards are the <code> drawnCards </code>
     *
     * @param claimCards
     * @param drawnCards
     * @throws IllegalArgumentException if the route is not a tunnel
     * @throws IllegalArgumentException if <\code> drawnCards </\code> does not contain exactly
     *     three cards
     * @return number of additional cards the player must play to take over route
     */
    public int additionalClaimCardsCount(SortedBag<Card> claimCards, SortedBag<Card> drawnCards) {
        Preconditions.checkArgument(level.equals(Level.UNDERGROUND));
        Preconditions.checkArgument(drawnCards.size() == 3);
        int additionalClaimCards = 0;
        for (Card drawn : drawnCards) {
            for (Card claim : claimCards) {
                if (!drawn.equals(Card.LOCOMOTIVE)) {
                    if (drawn.equals(claim)) {
                        additionalClaimCards++;
                    }
                } else {
                    additionalClaimCards++;
                }
            }
        }
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
}
