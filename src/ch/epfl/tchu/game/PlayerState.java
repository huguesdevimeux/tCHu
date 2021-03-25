package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public final class PlayerState extends PublicPlayerState {
    private final SortedBag<Ticket> tickets;
    private final SortedBag<Card> cards;
    private final List<Route> routes;

    /**
     * Constructs the state of a player having tickets, cards and routes.
     *
     * @param tickets The tickets of the player.
     * @param cards   The cards of the player.
     * @param routes  The routes of the player.
     */
    public PlayerState(SortedBag<Ticket> tickets, SortedBag<Card> cards, List<Route> routes) {
        super(tickets.size(), cards.size(), routes);
        this.routes = List.copyOf(routes);
        this.tickets = tickets;
        this.cards = cards;
    }

    /**
     * Returns the initial state of a player to whom the initial cards have been dealt; in this
     * initial state, the player does not yet have any tickets, and has not taken any roads.
     *
     * @param initialCards The initial cards of the player.
     * @return The initial State of the player.
     * @throws IllegalArgumentException if there is not exactly four cards.
     */
    public static PlayerState initial(SortedBag<Card> initialCards) {
        Preconditions.checkArgument(initialCards.size() == 4);
        return new PlayerState(SortedBag.of(), initialCards, Collections.emptyList());
    }

    /**
     * Returns the tickets of the player.
     *
     * @return the tickets of the player.
     */
    public SortedBag<Ticket> tickets() {
        return this.tickets;
    }

    /**
     * Returns the cards (wagon/locomotive) of the player.
     *
     * @return the cards (wagon/locomotive) of the player.
     */
    public SortedBag<Card> cards() {
        return this.cards;
    }

    /**
     * Returns a state identical to the receiver, except that the player also has the given tickets,
     *
     * @param newTickets The new tickets to add.
     * @return The new state.
     */
    public PlayerState withAddedTickets(SortedBag<Ticket> newTickets) {
        return new PlayerState(this.tickets().union(newTickets), this.cards(), this.routes());
    }

    /**
     * Returns a state identical to the receiver, except that the player also has the given card.
     *
     * @param card The card to add.
     * @return The state.
     */
    public PlayerState withAddedCard(Card card) {
        return withAddedCards(SortedBag.of(card));
    }

    /**
     * Returns an identical state to the receiver, except that the player also has the given cards.
     *
     * @param additionalCards The cards to add.
     * @return the new state.
     */
    public PlayerState withAddedCards(SortedBag<Card> additionalCards) {
        return new PlayerState(this.tickets(), this.cards.union(additionalCards), this.routes());
    }

    /**
     * Returns true if the player can take the given road, i.e. if he has enough cars left and if he
     * has the necessary cards.
     *
     * @param route The route to analyze.
     * @return Whether the player can take the route.
     */
    public boolean canClaimRoute(Route route) {
        return this.carCount() >= route.length()
                && route.possibleClaimCards().stream().anyMatch(this.cards::contains);
    }

    /**
     * Returns a list of all the sets of cards the player could use to take possession of the given
     * road.
     *
     * @param route The route to be tested.
     * @return The list of all the tests of cards that could be played.
     * @throws IllegalArgumentException if the player does not have enough cars to take the road.
     */
    public List<SortedBag<Card>> possibleClaimCards(Route route) {
        Preconditions.checkArgument(this.carCount() >= route.length());
        return route.possibleClaimCards().stream()
                .filter(this.cards::contains)
                .collect(Collectors.toList());
    }

    /**
     * Returns a list of all the sets of cards the player could use to take over a tunnel, sorted in
     * ascending order of the number of locomotive cards, knowing that he initially laid down the
     * initialCards, that the 3 cards drawn from the top of the deck are drawnCards, and that these
     * force the player to lay down additionalCardsCount cards;
     *
     * @param additionalCardsCount The additional cards the player needs to play.
     * @param initialCards The initialCards the player has played for the tunnel.
     * @param drawnCards The three cards the have been drawn for the tunnel.
     * @return All the possibilities, sorted by the number of locomotives.
     * @throws IllegalArgumentException if the number of additional cards is not between 1 and 3
     *     (inclusive)
     * @throws IllegalArgumentException if the set of initial cards is empty or contains more than 2
     *     different card types
     * @throws IllegalArgumentException if the set of drawn cards does not contain exactly 3 cards.
     */
    public List<SortedBag<Card>> possibleAdditionalCards(
            int additionalCardsCount, SortedBag<Card> initialCards, SortedBag<Card> drawnCards) {
        // WARNING drawnCards is useless for the logic. See Piazza @613
        Preconditions.checkArgument(1 <= additionalCardsCount && additionalCardsCount <= 3);
        // The set does not allow duplicates, so it returns the different types of cards of the set.
        Preconditions.checkArgument(
                (!initialCards.isEmpty()) && (initialCards.toSet().size() <= 2));
        Preconditions.checkArgument(drawnCards.size() == 3);
        // Removes the initial cards from the cards to get the additional cards the player can add.
        SortedBag<Card> cardsToPlay = this.cards.difference(initialCards);
        // The set gets every different type of cards that has initialCards.
        Set<Card> cardsPlayedForTheTunnel = initialCards.toSet();
        // Filter the cards that the player can play (CardsToPlay) to keep only the ones that are in
        // initial cards (the one that needs to be played).
        SortedBag<Card> cardsCanBePlayedAdditionally =
                SortedBag.of(
                        cardsToPlay.stream()
                                .filter(
                                        p ->
                                                cardsPlayedForTheTunnel.contains(p)
                                                        || p.equals(Card.LOCOMOTIVE))
                                .collect(Collectors.toList()));
        if (cardsCanBePlayedAdditionally.size() < additionalCardsCount)
            return Collections.emptyList();
        List<SortedBag<Card>> subsetsOfCardsPossiblyPlayed =
                new ArrayList<>(cardsCanBePlayedAdditionally.subsetsOfSize(additionalCardsCount));
        subsetsOfCardsPossiblyPlayed.sort(
                Comparator.comparingInt(cs -> cs.countOf(Card.LOCOMOTIVE)));
        return subsetsOfCardsPossiblyPlayed;
    }

    /**
     * Returns an identical state to the receiver, except that the player has additionally seized
     * the given route with the given cards. It means that the player sees the route ADDED and the
     * claimCard subtracted from them.
     *
     * @param route The route the player is taking.
     * @param claimCards The cards used to seize the route.
     * @return The new PlayerState.
     */
    public PlayerState withClaimedRoute(Route route, SortedBag<Card> claimCards) {
        List<Route> newRoutes = new ArrayList<>(this.routes());
        newRoutes.add(route);
        return new PlayerState(this.tickets(), this.cards.difference(claimCards), newRoutes);
    }

    /**
     * Returns the number of points - possibly negative - obtained by the player thanks to their
     * tickets.
     *
     * @return The number of points.
     */
    public int ticketPoints() {
        // For each route, take the max id of the two stations of the route. Then, take the maximum
        // of these maxima.
        int maxID =
                this.routes.stream()
                        .mapToInt(route -> Math.max(route.station1().id(), route.station2().id()))
                        .max()
                        .orElse(0);
        StationPartition.Builder builder = new StationPartition.Builder(maxID + 1);
        this.routes.forEach((route) -> builder.connect(route.station1(), route.station2()));
        StationPartition playerPartition = builder.build();
        return this.tickets.stream().mapToInt(ticket -> ticket.points(playerPartition)).sum();
    }

    /**
     * Returns the total points obtained by the player at the end of the game.
     *
     * @return Total amount of points.
     */
    public int finalPoints() {
        return this.claimPoints() + this.ticketPoints();
    }
}
