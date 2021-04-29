package ch.epfl.tchu.gui;

import static ch.epfl.tchu.game.Constants.FACE_UP_CARDS_COUNT;
import static ch.epfl.tchu.game.Constants.FACE_UP_CARD_SLOTS;

import static javafx.beans.property.ReadOnlyIntegerProperty.readOnlyIntegerProperty;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Representation of the observable state /part of the game.
 *
 * @author Luca Mouchel (324748)
 * @author Hugues Devimeux (327282)
 */
public class ObservableGameState {
    private PublicGameState newGameState;
    private PlayerState playerState;
    private PlayerId correspondingPlayer;
    // 1st group of properties
    private final ObjectProperty<Integer> percentageOfTicketsRemaining = percentageOfTicketsRemaining();
    private final ObjectProperty<Integer> percentageOfCardsRemaining = percentageOfCardsRemaining();
    private final List<ObjectProperty<Card>> faceUpCards = createFaceUpCards();
    private final List<ObjectProperty<Route>> allRoutes = createRoutes();
    // 2nd group of properties
    // to stock the numbers of each players tickets, cards, etc, we use a 2-sized list
    private final List<ObjectProperty<Integer>> eachPlayersTickets = createPlayersTickets();
    private final List<ObjectProperty<Integer>> eachPlayersCards = createPlayersCards();
    private final List<ObjectProperty<Integer>> eachPlayersCars = createPlayersCars();
    private final List<ObjectProperty<Integer>> eachPlayersClaimPoints = createPlayersClaimPoints();
    // 3rd group of properties
    private final ObjectProperty<List<Ticket>> playersTickets = createPlayersListOfTickets();
    // we stock the number of each type of card in a list such that the numbers in the list
    // represent the number of cards of the card at given index in Card.ALL. f.ex.
    // if a player has one black card, the list will be [1,0,0,0,0,0,0,0,0]
    private final List<ObjectProperty<Integer>> playersNumberOfEachCards = createPlayersCardsOfEachColor();
    // in order to verify if a player can claim a route, we use a list of BOOLEANS the size of the
    // total number of routes in the game and for each route, if the player can claim it,
    // we assign true, false otherwise (false is the default value).
    private final List<ObjectProperty<Boolean>> playerCanClaimRoute = playerCanClaimRoute();

    /**
     * Instantiable constructor. Sets by default all attributes to null, 0, or false, depending on
     * their types.
     *
     * @param correspondingPlayer identity of the player
     */
    public ObservableGameState(PlayerId correspondingPlayer) {
        this.correspondingPlayer = correspondingPlayer;
        this.playerState = null;
        this.newGameState = null;
        faceUpCards.forEach(i -> i.set(null));
        percentageOfTicketsRemaining.set(0);
        percentageOfCardsRemaining.set(0);
        allRoutes.forEach(i -> i.set(null));
        eachPlayersTickets.forEach(i -> i.set(0));
        eachPlayersCards.forEach(i -> i.set(0));
        eachPlayersCars.forEach(i -> i.set(0));
        eachPlayersClaimPoints.forEach(i -> i.set(0));
        playersTickets.set(null);
        playersNumberOfEachCards.forEach(i -> i.set(0));
        playerCanClaimRoute.forEach(i -> i.set(false));
    }

    /**
     * Updates all of the attributes.
     *
     * @param newGameState the new gameState
     * @param playerState the player state
     */
    public void setState(PublicGameState newGameState, PlayerState playerState) {
        this.newGameState = newGameState;
        this.playerState = playerState;
        // counting the number of each players' tickets and calculates the percentage remaining
        int numOfTicketsUsed =
                PlayerId.ALL.stream()
                        .mapToInt(i -> newGameState.playerState(i).ticketCount())
                        .sum();
        percentageOfTicketsRemaining.set(
                (int) ((1 - (double) numOfTicketsUsed / ChMap.tickets().size()) * 100));

        // same calculating process but for cards
        int numOfCardsUsed =
                PlayerId.ALL.stream().mapToInt(i -> newGameState.playerState(i).cardCount()).sum();
        percentageOfCardsRemaining.set(
                (int) ((1 - (double) numOfCardsUsed / newGameState.cardState().deckSize()) * 100));

        // setting the face up cards
        for (int slot : FACE_UP_CARD_SLOTS) {
            Card newCard = newGameState.cardState().faceUpCard(slot);
            faceUpCards.get(slot).set(newCard);
        }

        for (int i = 0; i < playerState.routes().size(); i++) {
            // not so sure about this
            Route route = newGameState.claimedRoutes().get(i);
            allRoutes.get(i).set(route);
        }

        // for each player, we need to know the tickets, cards, cars count as well as their claim
        // points we put these in lists of object properties of size 2
        for (int i = 0; i < PlayerId.COUNT; i++) {
            eachPlayersTickets
                    .get(i)
                    .set(newGameState.playerState(PlayerId.ALL.get(i)).ticketCount());
            eachPlayersCards.get(i).set(newGameState.playerState(PlayerId.ALL.get(i)).cardCount());
            eachPlayersCars.get(i).set(newGameState.playerState(PlayerId.ALL.get(i)).carCount());
            eachPlayersClaimPoints
                    .get(i)
                    .set(newGameState.playerState(PlayerId.ALL.get(i)).claimPoints());
        }

        // simply setting the object property as the tickets of the player
        playersTickets.set(playerState.tickets().toList());

        // counting the number of cards equal to each card individually (from Card.ALL)
        for (Card card : Card.ALL) {
            playersNumberOfEachCards
                    .get(Card.ALL.indexOf(card))
                    .set((int) playerState.cards().stream().filter(c -> c.equals(card)).count());
        }

        Set<List<Station>> set = new HashSet<>();
        // we first create a set using a list of stations in order to deal with double routes.
        // if the routes are "neighbours", they have the same "from" and "to" stations, so we add
        // all the neighboured routes to the set using their stations.
        newGameState.claimedRoutes().stream()
                .filter(this::routeHasNeighbour)
                .forEach(r -> set.add(r.stations()));
        // we create 3 booleans which are conditions to be met in order to claim a route.
        for (Route route : ChMap.routes()) {
            boolean psIsCurrentPs = playerState.equals(newGameState.currentPlayerState());
            boolean routeIsNotClaimed =
                    !newGameState.claimedRoutes().contains(route)
                            && !set.contains(route.stations());
            boolean canClaimRoute = playerState.canClaimRoute(route);
            if (psIsCurrentPs && routeIsNotClaimed && canClaimRoute) {
                // set true if all conditions are met
                playerCanClaimRoute.get(ChMap.routes().indexOf(route)).set(true);
            }
        }
    }

    /**
     * Boolean method to evaluate if the {@code route} has a neighbour.
     *
     * @param route to evaluate
     * @return whether the {@code route} has a neighbour
     */
    private boolean routeHasNeighbour(Route route) {
        return ChMap.routes().stream()
                .anyMatch(i -> !i.equals(route) && i.stations().equals(route.stations()));
    }

    // private methods to "create"/initialize the attributes as either
    // empty lists, or being assigned to 0 or false
    private static ObjectProperty<Integer> percentageOfTicketsRemaining() {
        return new SimpleObjectProperty<>(0);
    }

    private static ObjectProperty<Integer> percentageOfCardsRemaining() {
        return new SimpleObjectProperty<>(0);
    }

    private static List<ObjectProperty<Card>> createFaceUpCards() {
        List<ObjectProperty<Card>> faceUpCards = new ArrayList<>();
        IntStream.range(0, FACE_UP_CARDS_COUNT)
                .forEach(i -> faceUpCards.add(new SimpleObjectProperty<>()));
        return faceUpCards;
    }

    private static List<ObjectProperty<Route>> createRoutes() {
        List<ObjectProperty<Route>> allRoutes = new ArrayList<>();
        IntStream.range(0, ChMap.routes().size())
                .forEach(i -> allRoutes.add(new SimpleObjectProperty<>()));
        return allRoutes;
    }

    private static List<ObjectProperty<Integer>> createPlayersTickets() {
        List<ObjectProperty<Integer>> eachPlayersTickets = new ArrayList<>();
        IntStream.range(0, PlayerId.COUNT)
                .forEach(i -> eachPlayersTickets.add(new SimpleObjectProperty<>()));
        return eachPlayersTickets;
    }

    private static List<ObjectProperty<Integer>> createPlayersCards() {
        List<ObjectProperty<Integer>> eachPlayersCards = new ArrayList<>();
        IntStream.range(0, PlayerId.COUNT)
                .forEach(i -> eachPlayersCards.add(new SimpleObjectProperty<>()));
        return eachPlayersCards;
    }

    private static List<ObjectProperty<Integer>> createPlayersCars() {
        List<ObjectProperty<Integer>> eachPlayersCars = new ArrayList<>();
        IntStream.range(0, PlayerId.COUNT)
                .forEach(i -> eachPlayersCars.add(new SimpleObjectProperty<>()));
        return eachPlayersCars;
    }

    private static List<ObjectProperty<Integer>> createPlayersClaimPoints() {
        List<ObjectProperty<Integer>> eachPlayersClaimPoints = new ArrayList<>();
        IntStream.range(0, PlayerId.COUNT)
                .forEach(i -> eachPlayersClaimPoints.add(new SimpleObjectProperty<>(0)));
        return eachPlayersClaimPoints;
    }

    private static ObjectProperty<List<Ticket>> createPlayersListOfTickets() {
        return new SimpleObjectProperty<>();
    }

    private static List<ObjectProperty<Integer>> createPlayersCardsOfEachColor() {
        List<ObjectProperty<Integer>> playersNumberOfEachCards = new ArrayList<>();
        IntStream.range(0, Card.COUNT)
                .forEach(i -> playersNumberOfEachCards.add(new SimpleObjectProperty<>(0)));
        return playersNumberOfEachCards;
    }

    private static List<ObjectProperty<Boolean>> playerCanClaimRoute() {
        List<ObjectProperty<Boolean>> playerCanClaimRoute = new ArrayList<>();
        IntStream.range(0, ChMap.routes().size())
                .forEach(i -> playerCanClaimRoute.add(new SimpleObjectProperty<>(false)));
        return playerCanClaimRoute;
    }

    // simple getters as Read Only properties
    public ReadOnlyIntegerProperty percentageTickets() {
        return readOnlyIntegerProperty(percentageOfTicketsRemaining);
    }

    public ReadOnlyIntegerProperty percentageCards() {
        return readOnlyIntegerProperty(percentageOfTicketsRemaining);
    }

    public ReadOnlyObjectProperty<Card> faceUpCard(int slot) {
        return faceUpCards.get(slot);
    }

    public ReadOnlyObjectProperty<Route> allroutes(int slot) {
        return allRoutes.get(slot);
    }

    public ReadOnlyObjectProperty<Integer> playerTicketCount(PlayerState playerState) {
        return new SimpleObjectProperty<>(playerState.ticketCount());
    }

    public ReadOnlyObjectProperty<Integer> playerCardCount(PlayerState playerState) {
        return new SimpleObjectProperty<>(playerState.cardCount());
    }

    public ReadOnlyObjectProperty<Integer> playerCarCount(PlayerState playerState) {
        return new SimpleObjectProperty<>(playerState.carCount());
    }

    public ReadOnlyObjectProperty<Integer> playerClaimPoints(PlayerState playerState) {
        return new SimpleObjectProperty<>(playerState.claimPoints());
    }

    public ReadOnlyObjectProperty<List<Ticket>> playersTicketsList(PlayerState playerState) {
        return new SimpleObjectProperty<>(playerState.tickets().toList());
    }

    public ReadOnlyObjectProperty<Integer> playersNumberOfCards(
            PlayerState playerState, Card card) {
        return new SimpleObjectProperty<>(
                (int) playerState.cards().stream().filter(c -> c.equals(card)).count());
    }

    public ReadOnlyObjectProperty<Boolean> playerCanClaimRoute(Route route) {
        return playerCanClaimRoute.get(ChMap.routes().indexOf(route));
    }

    public ReadOnlyObjectProperty<PublicGameState> getCurrentGameState() {
        return new SimpleObjectProperty<>(newGameState);
    }

    public ReadOnlyObjectProperty<PlayerState> getCurrentPlayerState() {
        return new SimpleObjectProperty<>(playerState);
    }
}
