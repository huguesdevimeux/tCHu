package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static ch.epfl.tchu.game.Constants.FACE_UP_CARDS_COUNT;
import static ch.epfl.tchu.game.Constants.FACE_UP_CARD_SLOTS;

/**
 * Representation of the observable state/part of the game.
 *
 * @author Luca Mouchel (324748)
 * @author Hugues Devimeux (327282)
 */
public class ObservableGameState {
    // 1st group of properties
    private final IntegerProperty percentageOfTicketsRemaining = new SimpleIntegerProperty();
    private final IntegerProperty percentageOfCardsRemaining = new SimpleIntegerProperty();
    private final List<ObjectProperty<Card>> faceUpCards = createFaceUpCards();
    private final Map<Route, ObjectProperty<PlayerId>> allRoutes = createMapForRoutesOwners();
    // 2nd group of properties
    // to stock the numbers of each players tickets, cards, etc, we use a map to
    //relate each player to the attribute
    private final Map<PlayerId, IntegerProperty> eachPlayersTicketsCount = new HashMap<>();
    private final Map<PlayerId, IntegerProperty> eachPlayersCardsCount = new HashMap<>();
    private final Map<PlayerId, IntegerProperty> eachPlayersCarsCount = new HashMap<>();
    private final Map<PlayerId, IntegerProperty> eachPlayersClaimPoints = new HashMap<>();
    // 3rd group of properties
    private final ObservableList<Ticket> playersTickets = FXCollections.observableArrayList();
    // we stock the number of each type of card in a list such that the numbers in the list
    // represent the number of cards of the card at given index in Card.ALL. f.ex.
    // if a player has one black card, the list will be [1,0,0,0,0,0,0,0,0]
    private final List<IntegerProperty> currentPlayersNumberOfEachCards = createPlayersCardsOfEachColor();
    // in order to verify if a player can claim a route, we use a list of BOOLEANS the size of the
    // total number of routes in the game and for each route, if the player can claim it,
    // we assign true, false otherwise (false is the default value).
    private final List<BooleanProperty> playerCanClaimRoute = createBooleanPropertyList();
    private final PlayerId correspondingPlayer;
    private PublicGameState newGameState;
    private PlayerState playerState;

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

        double cardQuotient = (double) newGameState.cardState().deckSize() / Constants.ALL_CARDS.size();
        percentageOfCardsRemaining.set((int) ((1 - cardQuotient) * 100));

        // setting the face up cards
        for (int slot : FACE_UP_CARD_SLOTS) {
            Card newCard = newGameState.cardState().faceUpCard(slot);
			faceUpCards.get(slot).set(newCard);
        }

        // for each player, we need to know the tickets, cards, cars count as well as their claim
        // points we put these in lists of object properties of size 2
        for (PlayerId playerId : PlayerId.ALL) {
            eachPlayersTicketsCount.put(playerId, new SimpleIntegerProperty(newGameState.playerState(playerId).ticketCount()));
            eachPlayersCardsCount.put(playerId, new SimpleIntegerProperty(newGameState.playerState(playerId).cardCount()));
            eachPlayersCarsCount.put(playerId, new SimpleIntegerProperty(newGameState.playerState(playerId).carCount()));
            eachPlayersClaimPoints.put(playerId, new SimpleIntegerProperty(newGameState.playerState(playerId).claimPoints()));
        }

        // simply setting the object property as the tickets of the player
        playersTickets.setAll(playerState.tickets().toList());

        // counting the number of cards equal to each card individually (from Card.ALL)
        for (Card card : Card.ALL) {
            currentPlayersNumberOfEachCards
                    .get(Card.ALL.indexOf(card))
                    .set((int) playerState.cards().stream().filter(c -> c.equals(card)).count());
        }

        Set<List<Station>> neighbouringRoutes = new HashSet<>();
        // We first create a set using a list of stations in order to deal with double routes.
        // If the players' routes are "neighbours",
        // they have the same "from" and "to" stations, so we add
        // all the neighboured routes to the set using their stations.
        newGameState.claimedRoutes().stream()
                .filter(this::routeHasNeighbour)
                .forEach(r -> neighbouringRoutes.add(r.stations()));
        for (Route route : ChMap.routes()) {
            //setting the 4th property of the first group that sets the owner of the route
            for (PlayerId playerId : PlayerId.ALL) {
                if (newGameState.playerState(playerId).routes().contains(route))
                    allRoutes.get(route).set(playerId);
            }

            // Setting the last property
            // We create 3 booleans which are conditions to be met in order to claim a route.
            boolean pStateIsCurrentPState = playerState.equals(newGameState.currentPlayerState());
            boolean routeIsNotClaimed =
                    !newGameState.claimedRoutes().contains(route)
                            && !neighbouringRoutes.contains(route.stations());
            boolean canClaimRoute = playerState.canClaimRoute(route);
            if (pStateIsCurrentPState && routeIsNotClaimed && canClaimRoute) {
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
                .anyMatch(routeTemp -> !routeTemp.equals(route) && routeTemp.stations().equals(route.stations()));
    }

    private List<ObjectProperty<Card>> createFaceUpCards() {
        return Stream.generate(() ->
                new SimpleObjectProperty<Card>(null)).limit(FACE_UP_CARDS_COUNT).collect(Collectors.toList());
    }

    private Map<Route, ObjectProperty<PlayerId>> createMapForRoutesOwners() {
        Map<Route, ObjectProperty<PlayerId>> mapRouteToOwner = new HashMap<>();
        for (Route route : ChMap.routes())
            mapRouteToOwner.put(route, new SimpleObjectProperty<>(null));
        return mapRouteToOwner;
    }

    private List<IntegerProperty> createPlayersCardsOfEachColor() {
        return Stream.generate(() ->
                new SimpleIntegerProperty(0)).limit(Card.COUNT).collect(Collectors.toList());
    }

    private List<BooleanProperty> createBooleanPropertyList() {
        return Stream.generate(() ->
                new SimpleBooleanProperty(false)).limit(ChMap.routes().size()).collect(Collectors.toList());
    }

    // simple getters as Read Only properties
    public ReadOnlyIntegerProperty percentageTickets() {
        return percentageOfTicketsRemaining;
    }

    public ReadOnlyIntegerProperty percentageCards() {
        return percentageOfTicketsRemaining;
    }

    public ReadOnlyObjectProperty<Card> faceUpCard(int slot) {
        return faceUpCards.get(slot);
    }

    public ReadOnlyObjectProperty<PlayerId> getRoutesOwner(Route route) {
        return allRoutes.get(route);
    }

    public ReadOnlyIntegerProperty playerTicketCount(PlayerId playerId) {
        return eachPlayersTicketsCount.get(playerId);
    }

    public ReadOnlyIntegerProperty playerCardCount(PlayerId playerId) {
        return eachPlayersCardsCount.get(playerId);
    }

    public ReadOnlyIntegerProperty playerCarCount(PlayerId playerId) {
        return eachPlayersCarsCount.get(playerId);
    }

    public ReadOnlyIntegerProperty playerClaimPoints(PlayerId playerId) {
        return eachPlayersClaimPoints.get(playerId);
    }

    public ObservableList<Ticket> playersTicketsList() {
        return FXCollections.unmodifiableObservableList(playersTickets);
    }

    public ReadOnlyIntegerProperty playersNumberOfCards(Card card) {
        return currentPlayersNumberOfEachCards.get(Card.ALL.indexOf(card));
    }

    public ReadOnlyBooleanProperty playerCanClaimRoute(Route route) {
        return playerCanClaimRoute.get(ChMap.routes().indexOf(route));
    }

    public ReadOnlyBooleanProperty canDrawTickets(PublicGameState publicGameState) {
        return new SimpleBooleanProperty(publicGameState.canDrawTickets());
    }

    public ReadOnlyBooleanProperty canDrawCards(PublicGameState publicGameState) {
        return new SimpleBooleanProperty(publicGameState.canDrawCards());
    }

    public ReadOnlyObjectProperty<List<SortedBag<Card>>> possibleClaimCards(
            PlayerState playerState, Route route) {
        return new SimpleObjectProperty<>(playerState.possibleClaimCards(route));
    }

    // Simple getters for the gameState and the PlayerState.
    public ReadOnlyObjectProperty<PublicGameState> getGameState() {
        return new SimpleObjectProperty<>(newGameState);
    }

    public ReadOnlyObjectProperty<PlayerState> getPlayerState() {
        return new SimpleObjectProperty<>(playerState);
    }
}
