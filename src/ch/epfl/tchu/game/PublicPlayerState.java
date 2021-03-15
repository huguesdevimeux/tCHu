package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.List;

/** The type Public player state. */
public class PublicPlayerState {
    private final int ticketCount;
    private final int cardCount;
    private final List<Route> routes;
    private final int carCount;
    private final int claimPoints;

    /**
     * Instantiates a new Public player state.
     *
     * @param ticketCount the ticket count
     * @param cardCount the card count
     * @param routes the routes
     */
    public PublicPlayerState(int ticketCount, int cardCount, List<Route> routes) {
        Preconditions.checkArgument(ticketCount >= 0 && cardCount >= 0);
        this.ticketCount = ticketCount;
        this.cardCount = cardCount;
        this.routes = routes;
        this.claimPoints = Constants.ROUTE_CLAIM_POINTS.get(routes.size());
        this.carCount = 9;
    }

    /**
     * Ticket count int.
     *
     * @return the int
     */
    public int ticketCount() {
        return ticketCount;
    }

    /**
     * Card count int.
     *
     * @return the int
     */
    public int cardCount() {
        return cardCount;
    }

    /**
     * Routes list.
     *
     * @return the list
     */
    public List<Route> routes() {
        return routes;
    }

    /**
     * Car count int.
     *
     * @return the int
     */
    public int carCount() {
        return carCount;
    }

    /**
     * Claim points int.
     *
     * @return the int
     */
    public int claimPoints() {
        return claimPoints;
    }
}
