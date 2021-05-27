package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.List;

/**
 * Represents the public part of a player's state, i.e. the number of tickets, cards and cars he
 * owns, the roads he has seized, and the number of building points he has thus obtained. Immutable.
 *
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public class PublicPlayerState {
    private final int ticketCount;
    private final List<Route> routes;
    private final int cardCount;
    private final int claimPoints;
    private final int carCount;

    /**
     * Constructs the public state of a player who has the given number of tickets and cards, and
     * has seized the given routes
     *
     * @param ticketCount Amount of tickets the player has.
     * @param cardCount Amount of Cards the player has.
     * @param routes Routes the player has.
     * @throws IllegalArgumentException If ticketCount or cardCount is negative?
     */
    public PublicPlayerState(int ticketCount, int cardCount, List<Route> routes) {
        // Check if the ticketCount and cardCount are both >= 0.
        Preconditions.checkArgument(Math.min(ticketCount, cardCount) >= 0);
        this.cardCount = cardCount;
        this.ticketCount = ticketCount;
        this.routes = List.copyOf(routes);
        this.claimPoints = this.routes.stream().mapToInt(Route::claimPoints).sum();
        this.carCount =
                GameConstants.INITIAL_CAR_COUNT - this.routes.stream().mapToInt(Route::length).sum();
    }

    /**
     * Returns the number of tickets the player has.
     *
     * @return the number of tickets the player has.
     */
    public int ticketCount() {
        return this.ticketCount;
    }

    /**
     * Returns the number of tickets that the player has.
     *
     * @return the number of tickets the player has.
     */
    public int cardCount() {
        return this.cardCount;
    }

    /**
     * Returns the routes which the player has.
     *
     * @return the routes which the player has.
     */
    public List<Route> routes() {
        return this.routes;
    }

    /**
     * Returns the number of construction points the player obtained.
     *
     * @return the number of construction points the player obtained.
     */
    public int claimPoints() {
        return this.claimPoints;
    }

    /**
     * Returns the number of wagons the player has.
     *
     * @return the number of wagons the player has.
     */
    public int carCount() {
        return this.carCount;
    }
}
