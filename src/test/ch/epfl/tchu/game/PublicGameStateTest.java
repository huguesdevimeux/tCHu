package ch.epfl.tchu.game;

import static ch.epfl.tchu.game.PlayerId.PLAYER_1;
import static ch.epfl.tchu.game.PlayerId.PLAYER_2;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublicGameStateTest {
    int ticketsCount;
    List<Card> faceUpCards;
    PublicCardState cardState;
    PlayerId currentPlayerId;
    Map<PlayerId, PublicPlayerState> playerState = new HashMap<>();
    PlayerId lastPlayer;
    PublicGameState publicGameState;
    Route r1 =
            new Route(
                    "id",
                    ChMap.stations().get(0),
                    ChMap.stations().get(1),
                    3,
                    Route.Level.UNDERGROUND,
                    Color.BLACK);
    Route r2 =
            new Route(
                    "id2",
                    ChMap.stations().get(10),
                    ChMap.stations().get(5),
                    2,
                    Route.Level.UNDERGROUND,
                    Color.GREEN);
    PublicPlayerState publicPlayerState1 =
            new PublicPlayerState(ticketsCount, Constants.TOTAL_CARDS_COUNT, List.of(r1));
    PublicPlayerState publicPlayerState2 =
            new PublicPlayerState(ticketsCount, Constants.TOTAL_CARDS_COUNT, List.of(r2));

    @BeforeEach
    void setup() {
        ticketsCount = 2;
        faceUpCards = List.of(Card.GREEN, Card.BLUE, Card.BLACK, Card.VIOLET, Card.RED);
        cardState = new PublicCardState(faceUpCards, 10, 0);
        currentPlayerId = PLAYER_1;
        lastPlayer = PLAYER_2;
        playerState.put(currentPlayerId, publicPlayerState1);
        playerState.put(lastPlayer, publicPlayerState2);

        publicGameState =
                new PublicGameState(
                        ticketsCount, cardState, currentPlayerId, playerState, lastPlayer);
    }

    @Test
    void constructorThrowsIllegalArgumentExceptions() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new PublicGameState(
                                ticketsCount,
                                new PublicCardState(faceUpCards, -1, 0),
                                currentPlayerId,
                                playerState,
                                lastPlayer));

        playerState.keySet().remove(PLAYER_1);
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new PublicGameState(
                                ticketsCount, cardState, currentPlayerId, playerState, lastPlayer));
    }

    @Test
    void throwsNullPointerExceptions() {
        assertThrows(
                NullPointerException.class,
                () ->
                        new PublicGameState(
                                ticketsCount, null, currentPlayerId, playerState, lastPlayer));
        assertThrows(
                NullPointerException.class,
                () -> new PublicGameState(ticketsCount, cardState, null, playerState, lastPlayer));
        assertThrows(
                NullPointerException.class,
                () ->
                        new PublicGameState(
                                ticketsCount, cardState, currentPlayerId, null, lastPlayer));
    }

    @Test
    void ticketCount() {
        assertEquals(2, publicGameState.ticketsCount());
    }

    @Test
    void canDrawTickets() {
        assertTrue(publicGameState.canDrawTickets());
        cardState = new PublicCardState(faceUpCards, 0, 2);
        publicGameState =
                new PublicGameState(0, cardState, currentPlayerId, playerState, lastPlayer);
        assertFalse(publicGameState.canDrawTickets());
    }

    @Test
    void cardState() {
        assertEquals(cardState, publicGameState.cardState());
    }

    @Test
    void canDrawCards() {
        assertTrue(publicGameState.canDrawCards());
        cardState = new PublicCardState(faceUpCards, 1, 6);
        publicGameState =
                new PublicGameState(
                        ticketsCount, cardState, currentPlayerId, playerState, lastPlayer);
        assertTrue(publicGameState.canDrawCards());
        cardState = new PublicCardState(faceUpCards, 1, 1);
        publicGameState =
                new PublicGameState(
                        ticketsCount, cardState, currentPlayerId, playerState, lastPlayer);
        assertFalse(publicGameState.canDrawCards());
    }

    @Test
    void currentPlayerId() {
        assertEquals(currentPlayerId, publicGameState.currentPlayerId());
    }

    @Test
    void lastPlayerId() {
        assertEquals(lastPlayer, publicGameState.lastPlayer());
    }

    @Test
    void returnsThePlayerStateDependingOnId() {
        assertEquals(playerState.get(PLAYER_1), publicGameState.playerState(PLAYER_1));
        assertEquals(playerState.get(PLAYER_2), publicGameState.playerState(PLAYER_2));
    }

    @Test
    void returnsCurrentPlayerState() {
        assertEquals(playerState.get(currentPlayerId), publicGameState.currentPlayerState());
    }

    @Test
    void returnCorrectListOfRoute() {
        assertEquals(List.of(r2, r1), publicGameState.claimedRoutes());
        playerState.remove(PLAYER_1, publicPlayerState1);
        playerState.remove(lastPlayer, publicPlayerState2);
        playerState.put(PLAYER_1, publicPlayerState2);
        playerState.put(PLAYER_2, publicPlayerState2);
        publicGameState =
                new PublicGameState(
                        ticketsCount, cardState, currentPlayerId, playerState, lastPlayer);
        assertEquals(List.of(r2), publicGameState.claimedRoutes());
    }
}
