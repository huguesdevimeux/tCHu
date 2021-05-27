package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.epfl.tchu.game.GameConstants.*;

/**
 * Representation of the observable state/part of the game.
 *
 * @author Luca Mouchel (324748)
 * @author Hugues Devimeux (327282)
 */
public final class ObservableGameState {
    // 1st group of properties
    private final IntegerProperty percentageOfTicketsRemaining = new SimpleIntegerProperty();
    private final IntegerProperty percentageOfCardsRemaining = new SimpleIntegerProperty();
    private final List<ObjectProperty<Card>> faceUpCards = createFaceUpCards();
    private final Map<Route, ObjectProperty<PlayerId>> allRoutesOwners = createMapForRoutesOwners();
    // 2nd group of properties
    // to stock the numbers of each players tickets, cards, etc, we use a map to
    // relate each player to the attribute
    private final Map<PlayerId, IntegerProperty> eachPlayersTicketsCount = createMapWithSingleIntProperty();
    private final Map<PlayerId, IntegerProperty> eachPlayersCardsCount = createMapWithSingleIntProperty();
    private final Map<PlayerId, IntegerProperty> eachPlayersCarsCount = createMapWithSingleIntProperty();
    private final Map<PlayerId, IntegerProperty> eachPlayersClaimPoints = createMapWithSingleIntProperty();
    // 3rd group of properties
    private final ObservableList<Ticket> playersTickets = FXCollections.observableArrayList();
    // we stock the number of each type of card in a list such that the numbers in the list
    // represent the number of cards of the card at given index in Card.ALL. f.ex.
    // if a player has one black card, the list will be [1,0,0,0,0,0,0,0,0]
    private final List<IntegerProperty> currentPlayersNumberOfEachCards =
            createPlayersCardsOfEachColor();
    // in order to verify if a player can claim a route, we use a list of BOOLEANS the size of the
    // total number of routes in the game and for each route, if the player can claim it,
    // we assign true, false otherwise (false is the default value).
    private final List<BooleanProperty> playerCanClaimRoute = createBooleanPropertyList();
    private final PlayerId correspondingPlayer;
    private PublicGameState gameState;
    private PlayerState playerState;

    /**
     * Instantiable constructor.
     *
     * @param correspondingPlayer identity of the player
     */
    public ObservableGameState(PlayerId correspondingPlayer) {
        this.correspondingPlayer = correspondingPlayer;
    }

    /**
     * Updates all of the attributes.
     *
     * @param newGameState the new gameState
     * @param playerState  the player state
     * @throws NullPointerException if the public game state is null
     * @throws NullPointerException if the player state is null
     */
    public void setState(PublicGameState newGameState, PlayerState playerState) {
        this.gameState = Objects.requireNonNull(newGameState);
        this.playerState = Objects.requireNonNull(playerState);
        // counting the number of each players' tickets and calculates the percentage remaining
        int numOfTicketsUsed =
                PlayerId.ALL.stream()
                        .mapToInt(i -> newGameState.playerState(i).ticketCount())
                        .sum();
        percentageOfTicketsRemaining.set(
                (int) ((1 - (double) numOfTicketsUsed / ChMap.tickets().size()) * 100));

        percentageOfCardsRemaining.set(
                (int) ((newGameState.cardState().deckSize() / (double) TOTAL_CARDS_COUNT) * 100));

        // setting the face up cards
        for (int slot : FACE_UP_CARD_SLOTS) {
            Card newCard = newGameState.cardState().faceUpCard(slot);
            faceUpCards.get(slot).set(newCard);
        }

        // for each player, we need to know the tickets, cards, cars count as well as their claim
        // points we put these in lists of object properties of size 2
        for (PlayerId playerId : PlayerId.ALL) {
            eachPlayersTicketsCount.get(playerId).set(newGameState.playerState(playerId).ticketCount());
            eachPlayersCardsCount.get(playerId).set(newGameState.playerState(playerId).cardCount());
            eachPlayersCarsCount.get(playerId).set((newGameState.playerState(playerId).carCount()));
            eachPlayersClaimPoints.get(playerId).set((newGameState.playerState(playerId).claimPoints()));
        }

        // simply setting the object property as the tickets of the player
        playersTickets.setAll(playerState.tickets().toList());

        // counting the number of cards equal to each card individually (from Card.ALL)
        for (Card card : Card.ALL) {
            currentPlayersNumberOfEachCards
                    .get(Card.ALL.indexOf(card))
                    .set((int) playerState.cards().stream().filter(c -> c.equals(card)).count());
        }

        Set<List<Station>> stationsOfDoubleClaimedRoutes = newGameState.claimedRoutes().stream()
                .filter(Route::isPartOfDouble)
                .map(Route::stations).collect(Collectors.toSet());
        for (Route route : ChMap.routes()) {

            // Setting the last property
            // We create 3 booleans which are conditions to be met in order to claim a route.
            boolean playerIsCurrentPlayer = newGameState.currentPlayerId().equals(correspondingPlayer);
            boolean routeIsNotClaimed =
                    !newGameState.claimedRoutes().contains(route)
							// The player can't take a a part of a double route which one part has been claimed.
                            && !stationsOfDoubleClaimedRoutes.contains(route.stations());
            boolean canClaimRoute = playerState.canClaimRoute(route);
			playerCanClaimRoute.get(ChMap.routes().indexOf(route)).set(playerIsCurrentPlayer && routeIsNotClaimed && canClaimRoute);

            for (PlayerId playerId : PlayerId.ALL) {
                if (newGameState.playerState(playerId).routes().contains(route))
                    allRoutesOwners.get(route).set(playerId);
            }
		}
	}

    //Private methods to create lists or maps comprised of n elements of
    //either false if property requires a boolean, 0 or null.
    private static List<ObjectProperty<Card>> createFaceUpCards() {
        return Stream.generate(() -> new SimpleObjectProperty<Card>())
                .limit(FACE_UP_CARDS_COUNT)
                .collect(Collectors.toList());
    }

    private static Map<Route, ObjectProperty<PlayerId>> createMapForRoutesOwners() {
        Map<Route, ObjectProperty<PlayerId>> mapRouteToOwner = new HashMap<>();
        for (Route route : ChMap.routes())
            mapRouteToOwner.put(route, new SimpleObjectProperty<>());
        return mapRouteToOwner;
    }

    private static Map<PlayerId, IntegerProperty> createMapWithSingleIntProperty() {
        Map<PlayerId, IntegerProperty> map = new EnumMap<>(PlayerId.class);
        PlayerId.ALL.forEach(playerId -> map.put(playerId, new SimpleIntegerProperty()));
        return map;
    }

    private static List<IntegerProperty> createPlayersCardsOfEachColor() {
        return Stream.generate(SimpleIntegerProperty::new)
                .limit(Card.COUNT)
                .collect(Collectors.toList());
    }

    private static List<BooleanProperty> createBooleanPropertyList() {
        return Stream.generate(SimpleBooleanProperty::new)
                .limit(ChMap.routes().size())
                .collect(Collectors.toList());
    }

    // simple getters as Read Only properties

    /**
     * Returns the percentage of tickets remaining.
     *
     * @return percentage of tickets remaining
     */
    public ReadOnlyIntegerProperty percentageTickets() {
        return percentageOfTicketsRemaining;
    }

    /**
     * Returns the percentage of cards remaining.
     *
     * @return percentage of cards remaining
     */
    public ReadOnlyIntegerProperty percentageCards() {
        return percentageOfCardsRemaining;
    }

    /**
     * Returns the face up card at index slot.
     *
     * @param slot index of the face up card
     * @return face up card at index slot
     */
    public ReadOnlyObjectProperty<Card> faceUpCard(int slot) {
        return faceUpCards.get(slot);
    }

    /**
     * Returns the owner of the route. (either PLAYER_1 or 2 or null if nobody "owns" it)
     *
     * @param route route's owner
     * @return the route's owner, or null if it does not have any
     */
    public ReadOnlyObjectProperty<PlayerId> getRoutesOwner(Route route) {
        return allRoutesOwners.get(route);
    }

    /**
     * Returns the player's ticket count.
     *
     * @param playerId specified player
     * @return the specified player's ticket count
     */
    public ReadOnlyIntegerProperty playerTicketCount(PlayerId playerId) {
        return eachPlayersTicketsCount.get(playerId);
    }

    /**
     * Returns the player's card count.
     *
     * @param playerId specified player
     * @return the specified player's card count
     */
    public ReadOnlyIntegerProperty playerCardCount(PlayerId playerId) {
        return eachPlayersCardsCount.get(playerId);
    }

    /**
     * Returns the player's car count.
     *
     * @param playerId specified player
     * @return the specified player's car count
     */
    public ReadOnlyIntegerProperty playerCarCount(PlayerId playerId) {
        return eachPlayersCarsCount.get(playerId);
    }

    /**
     * Returns the player's claim points.
     *
     * @param playerId specified player
     * @return the specified player's claim points
     */
    public ReadOnlyIntegerProperty playerClaimPoints(PlayerId playerId) {
        return eachPlayersClaimPoints.get(playerId);
    }

    /**
     * Returns the current player's list of tickets.
     *
     * @return the current player's list of tickets
     */
    public ObservableList<Ticket> playersTicketsList() {
        return FXCollections.unmodifiableObservableList(playersTickets);
    }

    /**
     * Returns the current player's amount of cards of type {@code card}.
     *
     * @param card the card to evaluate it's total amount in the player's cards
     * @return the amount of cards of type {@code card}
     */
    public ReadOnlyIntegerProperty playerNumberOfCards(Card card) {
        return currentPlayersNumberOfEachCards.get(Card.ALL.indexOf(card));
    }

    /**
     * Returns whether the player can claim the route.
     *
     * @param route to evaluate whether the player can claim it
     * @return true if the player can claim the route, else false
     */
    public ReadOnlyBooleanProperty playerCanClaimRoute(Route route) {
        return playerCanClaimRoute.get(ChMap.routes().indexOf(route));
    }


    /**
     * Returns a boolean from a method in {@code PublicGameState}.
     *
     * @return true if the player can draw tickets, else false
     */
    public boolean canDrawTickets() {
        return gameState.canDrawTickets();
    }

    /**
     * Returns a boolean from a method in {@code PublicGameState}.
     *
     * @return true whether the player can draw cards, else false
     */
    public boolean canDrawCards() {
        return gameState.canDrawCards();
    }

    /**
     * Returns the player's possible claim cards for the given route.
     *
     * @param route to extract the possible claim cards from
     * @return the possible claim cards to claim the route
     */
    public List<SortedBag<Card>> possibleClaimCards(Route route) {
        return playerState.possibleClaimCards(route);
    }
}
