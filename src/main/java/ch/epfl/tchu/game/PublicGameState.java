package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the public part of the state of a part of tCHu. Immutable.
 *
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public class PublicGameState {
    private final int ticketsCount;
    private final PublicCardState cardState;
    private final PlayerId currentPlayerId;
    private final Map<PlayerId, PublicPlayerState> playerState;
    private final PlayerId lastPlayer;

    /**
     * Constructs the public part of the state of the game.
     *
     * @param ticketsCount    Number of tickets.
     * @param cardState       The PUBLIC state of the cards. Must be non null.
     * @param currentPlayerId The ID of the current player. Must be non null.
     * @param playerState     Public player State. Must be non null.
     * @param lastPlayer      The last player who has played.
     * @throws IllegalArgumentException if the deckSize is strictly negative.
     * @throws IllegalArgumentException if playerState does not have exactly two entries.
     * @throws NullPointerException     if either cardState, currentPlayerId, playerState is null.
     */
    public PublicGameState(
            int ticketsCount,
            PublicCardState cardState,
            PlayerId currentPlayerId,
            Map<PlayerId, PublicPlayerState> playerState,
            PlayerId lastPlayer) {
        Preconditions.checkArgument(cardState.deckSize() >= 0);
        Preconditions.checkArgument(playerState.keySet().size() == 2);
        Preconditions.checkArgument(ticketsCount >= 0);

        this.ticketsCount = ticketsCount;
        this.cardState = Objects.requireNonNull(cardState);
        this.currentPlayerId = Objects.requireNonNull(currentPlayerId);
        this.playerState = Objects.requireNonNull(playerState);
        // Last player can be null.
        this.lastPlayer = lastPlayer;
    }

    /**
     * Return the size of the ticket deck.
     *
     * @return the size of the ticket deck.
     */
    public int ticketsCount() {
        return this.ticketsCount;
    }

    /**
     * Returns true if it is possible to draw tickets, i.e. if the deck is not empty.
     *
     * @return true if it is possible to draw tickets, i.e. if the deck is not empty.
     */
    public boolean canDrawTickets() {
        return this.ticketsCount > 0;
    }

    /**
     * Returns the public part of the car/locomotive card status.
     *
     * @return the public part of the car/locomotive card status.
     */
    public PublicCardState cardState() {
        return this.cardState;
    }

    /**
     * Returns true if it is possible to draw cards, i.e. if the deck and the discard pile each
     * contain five cards.
     *
     * @return Whether it's possible to draw cards.
     */
    public boolean canDrawCards() {
        return this.cardState.deckSize() + this.cardState.discardsSize() >= 5;
    }

    /**
     * Returns the identity of the current player.
     *
     * @return the identity of the current player.
     */
    public PlayerId currentPlayerId() {
        return this.currentPlayerId;
    }

    /**
     * Returns the public part of the player state of the given identity.
     *
     * @param playerId The player id.
     * @return The public part of the player state of the given identity.
     */
    public PublicPlayerState playerState(PlayerId playerId) {
        return this.playerState.get(playerId);
    }

    /**
     * Returns the public part of the current player's state.
     *
     * @return The public part of the current player's state,
     */
    public PublicPlayerState currentPlayerState() {
        return this.playerState.get(this.currentPlayerId);
    }

    /**
     * Returns all the routes that either one of the players has taken.
     *
     * @return The roads.
     */
    public List<Route> claimedRoutes() {
        return this.playerState.values().stream()
                .map(PublicPlayerState::routes) // Collect the routes of the publicPlayerStates.
                .flatMap(Collection::stream) // flat the bigboi
                .distinct() // remove duplicates
                .collect(Collectors.toList());
    }

    /**
     * Returns the identity of the last player, or null if it is not yet known because the last
     * round has not started.
     *
     * @return The identity of the last player.
     */
    public PlayerId lastPlayer() {
        return this.lastPlayer;
    }
}
