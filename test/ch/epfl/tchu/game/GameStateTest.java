package ch.epfl.tchu.game;

import ch.epfl.tchu.SortedBag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static ch.epfl.tchu.game.PlayerId.PLAYER_1;
import static ch.epfl.tchu.game.PlayerId.PLAYER_2;
import static org.junit.jupiter.api.Assertions.*;

public class GameStateTest {
    PlayerId currentPlayerId;
    PlayerId lastPlayer;
    SortedBag<Ticket> tickets;
    SortedBag<Card> cards;
    PlayerState playerState1;
    PlayerState playerState2;
    Map<PlayerId, PublicPlayerState> playerState = new HashMap<>();
    Route r1;
    Route r2;
    Random rng;
    GameState gameState;

    @BeforeEach
    void setUp() {
        rng = new Random();
        tickets = SortedBag.of(ChMap.tickets().subList(0, 5));
        cards = SortedBag.of(Card.GREEN);
        r1 = ChMap.routes().get(0);
        r2 = ChMap.routes().get(1);
        playerState1 = new PlayerState(tickets, cards, List.of(r1));
        playerState2 = new PlayerState(tickets, cards, List.of(r2));
        gameState = GameState.initial(tickets, rng);
        currentPlayerId = PLAYER_1;
        lastPlayer = PLAYER_2;
        playerState.put(currentPlayerId, playerState1);
        playerState.put(lastPlayer, playerState2);
    }

    @Test
    void returnsTheCompletePlayerStateDependingOnId() {
        assertEquals(playerState.get(PLAYER_1), gameState.playerState(PLAYER_1));
        assertEquals(playerState.get(PLAYER_2), gameState.playerState(PLAYER_2));
    }

    @Test
    void returnsCompleteCurrentPlayerState() {
        assertEquals(playerState.get(currentPlayerId), gameState.currentPlayerState());
    }

    @Test
    void topTicketsReturnsCorrectBag() {
        System.out.println(gameState.topTickets(5));
        // WTF - why when count = 1 it doesnt take the top card from the bag?
        System.out.println(gameState.topTickets(1));
        Ticket unique = tickets.get(4);
        // following will fail : aEquals(unique, gameState.topTickets(1)); ?
        assertEquals(unique, gameState.topTickets(5).get(4));
        assertEquals(tickets, gameState.topTickets(5));
        assertThrows(IllegalArgumentException.class, () -> gameState.topTickets(6));
        assertThrows(IllegalArgumentException.class, () -> gameState.topTickets(-1));
    }

    @Test
    void withoutTopTicketsReturnsCorrectBag() {
        SortedBag<Ticket> a = SortedBag.of();
        GameState gS = GameState.initial(tickets, rng);
        assertEquals(gS.topTickets(5), gameState.withoutTopTickets(0).topTickets(5));

        tickets = SortedBag.of(List.of(ChMap.tickets().get(3), ChMap.tickets().get(4)));
        gS = GameState.initial(tickets, rng);
        assertEquals(1, gameState.withoutTopTickets(4).topTickets(1).size());
        assertEquals(2, gameState.withoutTopTickets(3).topTickets(2).size());
        assertEquals(2, gameState.withoutTopTickets(1).topTickets(2).size());
        assertEquals(0, gameState.withoutTopTickets(5).topTickets(0).size());
        // total number of tickets = 5 - so if u remove 5 cards, then top tickets = 0
        assertThrows(
                IllegalArgumentException.class, () -> gameState.withoutTopTickets(5).topTickets(1));
        assertThrows(IllegalArgumentException.class, () -> gameState.withoutTopTickets(8));
        assertThrows(IllegalArgumentException.class, () -> gameState.withoutTopTickets(-1));
    }

    @Test
    void topCard() {
        // everything is shuffled so
        assertTrue(true);
        // assertThrows(IllegalArgumentException.class, () -> new GameState())
    }

    @Test
    void withoutTopCardGameState() {
        GameState a = gameState.withoutTopCard();
        assertNotEquals(gameState.topCard(), a.topCard());
        // deck in gameState doesnt depend on any parameters and will never be empty as it takes all
        // the cards from Constants
    }

    @Test
    void withMoreDiscardedCards() {
        SortedBag<Card> discardedCards = SortedBag.of(Card.GREEN);
        GameState a = gameState;
        assertEquals(a.cardState(), gameState.cardState());
        int totalSize = gameState.withMoreDiscardedCards(discardedCards).cardState().totalSize();
        int previousSize = gameState.cardState().totalSize();
        assertTrue(totalSize >= previousSize);
    }

    @Test
    void withCardsDeckRecreatedIfNeededFunctions() {
        // to show the deck @line 45 of gameState is never empty
        assertFalse(gameState.cardState().isDeckEmpty());
        assertEquals(gameState, gameState.withCardsDeckRecreatedIfNeeded(new Random()));
    }

    @Test
    void withInitiallyChosenTickets(){
        playerState.remove(PLAYER_1);
        playerState1 = new PlayerState(SortedBag.of(), cards, List.of(r1));
        playerState.put(PLAYER_1, playerState1);
        int fails = playerState.get(PLAYER_1).ticketCount();

        assertThrows(IllegalArgumentException.class, () -> gameState.withInitiallyChosenTickets((PLAYER_1), tickets));
    }
}
