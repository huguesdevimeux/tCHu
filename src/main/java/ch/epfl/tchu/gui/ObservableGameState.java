package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.*;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static ch.epfl.tchu.game.Constants.FACE_UP_CARDS_COUNT;
import static ch.epfl.tchu.game.Constants.FACE_UP_CARD_SLOTS;
import static javafx.beans.property.ReadOnlyIntegerProperty.readOnlyIntegerProperty;

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
    private final IntegerProperty percentageOfTicketsRemaining =
            percentageOfTicketsRemaining();
    private final IntegerProperty percentageOfCardsRemaining = percentageOfCardsRemaining();
    private final List<ObjectProperty<Card>> faceUpCards = createFaceUpCards();
    private final List<ObjectProperty<Route>> allRoutes = createRoutes();
    // 2nd group of properties
    // to stock the numbers of each players tickets, cards, etc, we use a 2-sized list
    private final List<IntegerProperty> eachPlayersTickets = createPlayersTickets();
    private final List<IntegerProperty> eachPlayersCards = createPlayersCards();
    private final List<IntegerProperty> eachPlayersCars = createPlayersCars();
    private final List<IntegerProperty> eachPlayersClaimPoints = createPlayersClaimPoints();
    // 3rd group of properties
    private final ObservableList<Ticket> playersTickets = createPlayersListOfTickets();
    // we stock the number of each type of card in a list such that the numbers in the list
    // represent the number of cards of the card at given index in Card.ALL. f.ex.
    // if a player has one black card, the list will be [1,0,0,0,0,0,0,0,0]
    private final List<IntegerProperty> playersNumberOfEachCards =
            createPlayersCardsOfEachColor();
    // in order to verify if a player can claim a route, we use a list of BOOLEANS the size of the
    // total number of routes in the game and for each route, if the player can claim it,
    // we assign true, false otherwise (false is the default value).
    private final List<BooleanProperty> playerCanClaimRoute = playerCanClaimRoute();

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
        playersTickets.setAll(List.of());
        playersNumberOfEachCards.forEach(i -> i.set(0));
        playerCanClaimRoute.forEach(i -> i.set(false));
    }

    /**
     * Updates all of the attributes.
     *
     * @param newGameState the new gameState
     * @param playerState  the player state
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
        playersTickets.setAll(playerState.tickets().toList());

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
    private static IntegerProperty percentageOfTicketsRemaining() {
        return new SimpleIntegerProperty(0);
    }

    private static IntegerProperty percentageOfCardsRemaining() {
        return new SimpleIntegerProperty(0);
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

    private static List<IntegerProperty> createPlayersTickets() {
        List<IntegerProperty> eachPlayersTickets = new ArrayList<>();
        IntStream.range(0, PlayerId.COUNT)
                .forEach(i -> eachPlayersTickets.add(new SimpleIntegerProperty(0)));
        return eachPlayersTickets;
    }

    private static List<IntegerProperty> createPlayersCards() {
        List<IntegerProperty> eachPlayersCards = new ArrayList<>();
        IntStream.range(0, PlayerId.COUNT)
                .forEach(i -> eachPlayersCards.add(new SimpleIntegerProperty(0)));
        return eachPlayersCards;
    }

    private static List<IntegerProperty> createPlayersCars() {
        List<IntegerProperty> eachPlayersCars = new ArrayList<>();
        IntStream.range(0, PlayerId.COUNT)
                .forEach(i -> eachPlayersCars.add(new SimpleIntegerProperty(0)));
        return eachPlayersCars;
    }

    private static List<IntegerProperty> createPlayersClaimPoints() {
        List<IntegerProperty> eachPlayersClaimPoints = new ArrayList<>();
        IntStream.range(0, PlayerId.COUNT)
                .forEach(i -> eachPlayersClaimPoints.add(new SimpleIntegerProperty(0)));
        return eachPlayersClaimPoints;
    }

    private static ObservableList<Ticket> createPlayersListOfTickets() {
        return FXCollections.observableArrayList();
    }

    private static List<IntegerProperty> createPlayersCardsOfEachColor() {
        List<IntegerProperty> playersNumberOfEachCards = new ArrayList<>();
        IntStream.range(0, Card.COUNT)
                .forEach(i -> playersNumberOfEachCards.add(new SimpleIntegerProperty(0)));
        return playersNumberOfEachCards;
    }

    private static List<BooleanProperty> playerCanClaimRoute() {
        List<BooleanProperty> playerCanClaimRoute = new ArrayList<>();
        IntStream.range(0, ChMap.routes().size())
                .forEach(i -> playerCanClaimRoute.add(new SimpleBooleanProperty(false)));
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

    public ReadOnlyIntegerProperty playerTicketCount(PlayerState playerState) {
        return new SimpleIntegerProperty(playerState.ticketCount());
    }

    public ReadOnlyIntegerProperty playerCardCount(PlayerState playerState) {
        return new SimpleIntegerProperty(playerState.cardCount());
    }

    public ReadOnlyIntegerProperty playerCarCount(PlayerState playerState) {
        return new SimpleIntegerProperty(playerState.carCount());
    }

    public ReadOnlyIntegerProperty playerClaimPoints(PlayerState playerState) {
        return new SimpleIntegerProperty(playerState.claimPoints());
    }

    public ReadOnlyObjectProperty<List<Ticket>> playersTicketsList(PlayerState playerState) {
        return new SimpleObjectProperty<>(playerState.tickets().toList());
    }

    public ReadOnlyIntegerProperty playersNumberOfCards(Card card) {
        return new SimpleIntegerProperty(
                playersNumberOfEachCards.get(Card.ALL.indexOf(card)).get());
    }

    public ReadOnlyBooleanProperty playerCanClaimRoute(Route route) {
        return playerCanClaimRoute.get(ChMap.routes().indexOf(route));
    }

    public ReadOnlyObjectProperty<PublicGameState> getCurrentGameState() {
        return new SimpleObjectProperty<>(newGameState);
    }

    public ReadOnlyObjectProperty<PlayerState> getCurrentPlayerState() {
        return new SimpleObjectProperty<>(playerState);
    }
}
