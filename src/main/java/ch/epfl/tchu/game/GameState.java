package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

/**
 * Represents the state of a game of Tchu. Immutable.
 *
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public final class GameState extends PublicGameState {

    private final CardState cardState;
    private final Deck<Ticket> deckTickets;
    private final Map<PlayerId, PlayerState> playerStates;

    private GameState(
            Deck<Ticket> deckTickets,
            CardState cardState,
            PlayerId currentPlayerId,
            Map<PlayerId, PlayerState> playerStates,
            PlayerId lastPlayer) {
        super(deckTickets.size(), cardState, currentPlayerId, Map.copyOf(playerStates), lastPlayer);
        this.deckTickets = deckTickets;
        this.playerStates = Map.copyOf(playerStates);
        this.cardState = cardState;
    }

    /**
     * Returns the initial state of a game of tCHu in which the ticket deck contains the given
     * tickets and the card deck contains the GuiConstants.ALL_CARDS cards, without the top 8 (2×4),
     * dealt to the players; these decks are shuffled with the given random generator, which is also
     * used to randomly choose the identity of the first player.
     *
     * @param tickets The tickets for the new game.
     * @param rng The random generator used.
     * @return An instance of {@link GameState} with the specified attributes.
     */
    public static GameState initial(SortedBag<Ticket> tickets, Random rng) {
        Deck<Card> deckCards = Deck.of(GameConstants.ALL_CARDS, rng);
        Map<PlayerId, PlayerState> playerStates = new EnumMap<>(PlayerId.class);
        for (PlayerId player : PlayerId.ALL) {
            playerStates.put(
                    player, PlayerState.initial(deckCards.topCards(GameConstants.INITIAL_CARDS_COUNT)));
            deckCards = deckCards.withoutTopCards(GameConstants.INITIAL_CARDS_COUNT);
        }

        return new GameState(
                Deck.of(tickets, rng),
                CardState.of(deckCards),
                PlayerId.ALL.get(rng.nextInt(PlayerId.COUNT)),
                playerStates,
                null // The last player is null at the initial state
                );
    }

    /**
     * Factory that returns a GameState with the same players (same currentPlayer and same
     * lastPlayer), to avoid redundancies in the codebase.
     */
    private GameState GameStateWithSamePlayers(
            Deck<Ticket> deckTickets,
            CardState cardState,
            Map<PlayerId, PlayerState> playerStates) {
        return new GameState(
                deckTickets, cardState, this.currentPlayerId(), playerStates, this.lastPlayer());
    }

    /**
     * Returns the complete state of the player.
     *
     * @param playerId The player id.
     * @return the complete state of the player.
     */
    public PlayerState playerState(PlayerId playerId) {
        return this.playerStates.get(playerId);
    }

    /**
     * Returns the complete state of the game.
     *
     * @return the complete state of the game.
     */
    public PlayerState currentPlayerState() {
        return this.playerStates.get(this.currentPlayerId());
    }

    // region Cards and Tickets Methods

    /**
     * Returns the tickets from the top of the deck, or raises IllegalArgumentException if count is
     * not between 0 and the deck size (inclusive).
     *
     * @param count Number of tickets to get.
     * @return The count top card(s).
     * @throws IllegalArgumentException if count is not within 0 and the size of the deck
     *     (included).
     */
    public SortedBag<Ticket> topTickets(int count) {
        return this.deckTickets.topCards(count);
    }

    /**
     * Returns a state identical to the receiver, but without count ticket(s) from the top of the
     * deck.
     *
     * @param count The number of tickets to remove.
     * @return The state.
     * @throws IllegalArgumentException if count is not within 0 and the size of the deck
     *     (inclusive).
     */
    public GameState withoutTopTickets(int count) {
        return GameStateWithSamePlayers(
                this.deckTickets.withoutTopCards(count), this.cardState, this.playerStates);
    }

    /**
     * Returns the card to the top of the deck.
     *
     * @return The card.
     * @throws IllegalArgumentException if the deck is empty.
     */
    public Card topCard() {
        return this.cardState.topDeckCard();
    }

    /**
     * Returns a state identical to the receiver but without the card on top of the deck.
     *
     * @return The state.
     * @throws IllegalArgumentException if the deck is empty.
     */
    public GameState withoutTopCard() {
        return GameStateWithSamePlayers(
                this.deckTickets, this.cardState.withoutTopDeckCard(), this.playerStates);
    }

    /**
     * Returns a state identical to the receiver but with the given cards added to the discard pile.
     *
     * @param discardedCards the cards that have been discarded.
     * @return The new state.
     */
    public GameState withMoreDiscardedCards(SortedBag<Card> discardedCards) {
        return GameStateWithSamePlayers(
                this.deckTickets,
                cardState.withMoreDiscardedCards(discardedCards),
                this.playerStates);
    }

    /**
     * Returns a state identical to the receiver unless the deck of cards is empty, in which case it
     * is recreated from the discard pile, shuffled using the given random generator.
     *
     * @param rng The random Generator.
     * @return The new state.
     */
    public GameState withCardsDeckRecreatedIfNeeded(Random rng) {
        if (this.cardState.isDeckEmpty())
            return GameStateWithSamePlayers(
                    this.deckTickets,
                    this.cardState.withDeckRecreatedFromDiscards(rng),
                    this.playerStates);
        return this;
    }

    // endregion

    // region Methods triggered by the Player (OnAction methods).

    /**
     * Returns a state identical to the receiver but in which the given tickets have been added to
     * the given player's hand.
     *
     * @param playerId the player Id.
     * @param chosenTickets The tickets chosen by the player.
     * @return The new {@link GameState} instance.
     * @throws IllegalArgumentException if the given player already has at least one ticket.
     */
    public GameState withInitiallyChosenTickets(
            PlayerId playerId, SortedBag<Ticket> chosenTickets) {
        Preconditions.checkArgument(this.playerStates.get(playerId).ticketCount() == 0);
        // NOTE : this method does not modify the deck of tickets, and it's intended. cf paper.
        EnumMap<PlayerId, PlayerState> tempPlayerState = new EnumMap<>(this.playerStates);
        // Update the playerState for the given PlayerId.
        tempPlayerState.computeIfPresent(
                playerId, (id, playerState) -> playerState.withAddedTickets(chosenTickets));
        return GameStateWithSamePlayers(this.deckTickets, this.cardState, tempPlayerState);
    }

    /**
     * Returns a state identical to the receiver, but in which the current player has drawn the
     * drawnTickets from the top of the deck, and chosen to keep the ones contained in chosenTicket.
     *
     * @param drawnTickets The drawn tickets.
     * @param chosenTickets The chosen tickets.
     * @return The new State.
     * @throws IllegalArgumentException if the set of chosen tickets is not included in the set of
     *     drawn tickets.
     */
    public GameState withChosenAdditionalTickets(
            SortedBag<Ticket> drawnTickets, SortedBag<Ticket> chosenTickets) {
        Preconditions.checkArgument(drawnTickets.contains(chosenTickets));

        EnumMap<PlayerId, PlayerState> tempPlayerState = new EnumMap<>(this.playerStates);
        // Change the player state corresponding to the current player to a state with added
        // tickets.
        tempPlayerState.computeIfPresent(
                this.currentPlayerId(), (id, state) -> state.withAddedTickets(chosenTickets));
        return GameStateWithSamePlayers(
                this.deckTickets.withoutTopCards(drawnTickets.size()),
                this.cardState,
                tempPlayerState);
    }

    /**
     * Returns a state identical to the receiver except that the face-up card at the given location
     * has been placed in the current player's hand, and replaced by the one at the top of the deck
     *
     * @param slot The slot of the card drawn.
     * @return The new state.
     * @throws IllegalArgumentException if it is not possible to draw cards, i.e. if canDrawCards
     *     returns false.
     */
    public GameState withDrawnFaceUpCard(int slot) {
        EnumMap<PlayerId, PlayerState> tempPlayerState = new EnumMap<>(this.playerStates);
        // Add a card to the playerState corresponding to the current player.
        tempPlayerState.computeIfPresent(
                this.currentPlayerId(),
                (id, playerState) -> playerState.withAddedCard(cardState.faceUpCard(slot)));
        return GameStateWithSamePlayers(
                this.deckTickets, this.cardState.withDrawnFaceUpCard(slot), tempPlayerState);
    }

    /**
     * Returns a state identical to the receiver except that the top card of the deck has been
     * placed in the current player's hand.
     *
     * @return The state.
     * @throws IllegalArgumentException if it is not possible to draw cards, i.e. if canDrawCards
     *     returns false.
     */
    public GameState withBlindlyDrawnCard() {
        EnumMap<PlayerId, PlayerState> tempPlayerState = new EnumMap<>(this.playerStates);
        // Add a card to the playerState corresponding to the current player.
        tempPlayerState.computeIfPresent(
                this.currentPlayerId(),
                (id, playerState) -> playerState.withAddedCard(cardState.topDeckCard()));
        return GameStateWithSamePlayers(
                this.deckTickets, this.cardState.withoutTopDeckCard(), tempPlayerState);
    }

    /**
     * Returns a state identical to the receiver but in which the current player has seized the
     * given route using the given cards.
     *
     * @param route The route the player has taken.
     * @param cards The cards the player used to take the route.
     * @return The new State.
     */
    public GameState withClaimedRoute(Route route, SortedBag<Card> cards) {
        EnumMap<PlayerId, PlayerState> tempPlayerState = new EnumMap<>(this.playerStates);
        // Add a card to the playerState corresponding to the current player.
        tempPlayerState.computeIfPresent(
                this.currentPlayerId(),
                (id, playerState) -> playerState.withClaimedRoute(route, cards));
        return GameStateWithSamePlayers(
                this.deckTickets, this.cardState.withMoreDiscardedCards(cards), tempPlayerState);
    }

    // endregion

    // region Methods turn related.

    /**
     * Returns true if the last turn is starting, i.e. if the identity of the last player is
     * currently unknown but the current player has only two or fewer cars left; this method should
     * only be called at the end of a player's turn.
     *
     * @return Whether the last turn begins.
     */
    public boolean lastTurnBegins() {
        return this.currentPlayerState().carCount() <= 2 && lastPlayer() == null;
    }

    /**
     * Ends the turn of the current player, i.e. returns a state identical to the receiver except
     * that the current player is the one following the current player; moreover, if lastTurnBegins
     * returns true, the current player becomes the last player.
     *
     * @return The new sate of the next turn.
     */
    public GameState forNextTurn() {
        PlayerId lastPlayer = this.lastPlayer();
        if (lastTurnBegins()) {
            lastPlayer = this.currentPlayerId();
        }
        return new GameState(
                this.deckTickets,
                this.cardState,
                this.currentPlayerId().next(),
                this.playerStates,
                lastPlayer);
    }

    // endregion
}
