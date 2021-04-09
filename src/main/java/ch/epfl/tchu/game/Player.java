package ch.epfl.tchu.game;

import ch.epfl.tchu.SortedBag;

import java.util.List;
import java.util.Map;

/**
 * Represents all the actions a player can take in the game.
 *
 * @author Luca Mouchel (324748)
 * @author Hugues Devimeux (327282)
 */
public interface Player {
    /**
     * Called upon when starting the game to communicate the ID of the players as well as the name
     * of all the players, including his or hers (in <code>playernames</code>).
     *
     * @param ownId the players' id
     * @param playerNames all the names of all the players
     */
    void initPlayers(PlayerId ownId, Map<PlayerId, String> playerNames);

    /**
     * Called when an information has to be transmitted to a player under the form of a String.
     *
     * @param info information to be transmitted
     */
    void receiveInfo(String info);

    /**
     * Called when the game state changes - to inform the player of the <code>newState</code> as
     * well as his/her own state.
     *
     * @param newState new state of the game
     * @param ownState the state of the player
     */
    void updateState(PublicGameState newState, PlayerState ownState);

    /**
     * Called at the BEGINNING of the game to communicate the 5 tickets that have been distributed.
     *
     * @param tickets 5 tickets distributed
     */
    void setInitialTicketChoice(SortedBag<Ticket> tickets);

    /**
     * Called at the beginning of the game to ask the player which tickets the player wishes to keep
     * (via <code>setInitialTicketChoice</code>).
     *
     * @return the bag of tickets the player wants to keep
     */
    SortedBag<Ticket> chooseInitialTickets();

    /**
     * Called at the beginning of each round, to know what action the player decides to take.
     *
     * @return the player's chosen course of action at the beginning of the round
     */
    TurnKind nextTurn();

    /**
     * Called when the player decides to draw additional tickets during the game, as to
     * communication the drawn tickets and to know which ones the player keeps.
     *
     * @param options bag of tickets the player can choose from
     * @return communication of drawn tickets to know which ones are kept
     */
    SortedBag<Ticket> chooseTickets(SortedBag<Ticket> options);

    /**
     * Called when the player has decided to draw cards (can be locomotives) to know where he wants
     * to to draw them from. It can be a faceUp card - to which the method returns int in [0,4]. It
     * can be a deck card - to which the method returns <code>Constants.DECK_SLOT</code> aka -1.
     *
     * @return an int that depends on what type of card is drawn
     */
    int drawSlot();

    /**
     * Called when the player has decided (attempted to) take over a route - to know which route it
     * is.
     *
     * @return the route the player has tried to take over
     */
    Route claimedRoute();

    /**
     * Called when the player has decided to (or attempted to) take over a route - to know which
     * card(s) they want to use initially to do so.
     *
     * @return the initial cards the player wants to use to take over a route
     */
    SortedBag<Card> initialClaimCards();

    /**
     * Called when the player has decided to attempt to take a tunnel (UNDERGROUND route) and that
     * additional cards have to be used - to ultimately know which cards the player wants to use.
     * The available cards he can use is the parameter options. If the returned SortedBag is empty,
     * it means the player doesn't want (or can't) pick any options.
     *
     * @param options list of cards with which the player can choose to take over a tunnel
     * @return the additional cards the player wants to use to take over a tunnel, or an empty
     *     sortedBag if he can't or does not want to
     */
    SortedBag<Card> chooseAdditionalCards(List<SortedBag<Card>> options);

    enum TurnKind {
        /** Enumeration of all the actions a player can take. */
        DRAW_TICKETS,
        DRAW_CARDS,
        CLAIM_ROUTE;

        /**
         * same function as ALL attribute in Color Card, stocks every element of the enum in a List
         */
        public static final List<TurnKind> ALL = List.of(TurnKind.values());
    }
}
