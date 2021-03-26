package ch.epfl.tchu.game;

import ch.epfl.tchu.SortedBag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

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
    Map<PlayerId, PlayerState> playerStates = new HashMap<>();
    Route r1;
    Route r2;
    Random rng;
    GameState gameState;
    Deck<Ticket> deck;
    CardState cardState;

    public static final Random NON_RANDOM =
            new Random() {
                @Override
                public int nextInt(int i) {
                    return i - 1;
                }
            };

    @BeforeEach
    void setUp() {
        rng = new Random(0);
        tickets = SortedBag.of(ChMap.tickets().subList(0, 5));
        deck = Deck.of(tickets, rng);
        cards = SortedBag.of(2, Card.GREEN, 2, Card.LOCOMOTIVE);
        r1 = ChMap.routes().get(0);
        r2 = ChMap.routes().get(1);
        playerState1 = new PlayerState(tickets, cards, List.of(r1));
        playerState2 = new PlayerState(tickets, cards, List.of(r2));
        cardState =
                CardState.of(Deck.of(SortedBag.of(1, Card.GREEN, 8, Card.VIOLET), new Random()));
        currentPlayerId = PLAYER_1;
        lastPlayer = PLAYER_2;
        playerStates.put(currentPlayerId, playerState1);
        playerStates.put(lastPlayer, playerState2);
        gameState = GameState.initial(tickets, rng);
    }

    @Test
    void topTicketsReturnsCorrectBag() {
        Ticket unique = tickets.get(4);
        // following will fail : aEquals(unique, gameState.topTickets(1)); ?
        assertEquals(unique, gameState.topTickets(5).get(4));
        assertEquals(tickets, gameState.topTickets(5));
        assertThrows(IllegalArgumentException.class, () -> gameState.topTickets(6));
        assertThrows(IllegalArgumentException.class, () -> gameState.topTickets(-1));
    }

    @Test
    void withoutTopTicketsReturnsCorrectBag() {
        GameState gS = GameState.initial(tickets, rng);
        assertEquals(gS.topTickets(5), gameState.withoutTopTickets(0).topTickets(5));

        tickets = SortedBag.of(List.of(ChMap.tickets().get(3), ChMap.tickets().get(4)));
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
        while (!gameState.cardState().isDeckEmpty()) {
            gameState = gameState.withoutTopCard();
        }
        assertThrows(IllegalArgumentException.class, () -> gameState.topCard());
    }

    @Test
    void withoutTopCardGameState() {
        Card a = gameState.withoutTopCard().topCard();
        assertNotEquals(gameState.topCard(), a);
    }

    @Test
    void withMoreDiscardedCards() {
        SortedBag<Card> discardedCards = SortedBag.of();
        int totalSize = gameState.withMoreDiscardedCards(discardedCards).cardState().totalSize();
        int previousSize = gameState.cardState().totalSize();
        assertEquals(previousSize, totalSize);

        discardedCards = SortedBag.of(Card.GREEN);
        totalSize = gameState.withMoreDiscardedCards(discardedCards).cardState().totalSize();
        assertTrue(totalSize > previousSize);
    }

    @Test
    void withCardsDeckRecreatedIfNeededFunctions() {
        GameState a = gameState;
        assertEquals(gameState, gameState.withCardsDeckRecreatedIfNeeded(new Random()));
        assertFalse(gameState.cardState().isDeckEmpty());
        while (!gameState.cardState().isDeckEmpty()) {
            gameState = gameState.withoutTopCard();
        }
        assertEquals(0, gameState.cardState().deckSize());
        assertNotEquals(a, gameState.withCardsDeckRecreatedIfNeeded(rng));
    }

    @Test
    void withInitiallyChosenTicketsThrowsException() {
        GameState temp = gameState;
        temp =
                temp.withChosenAdditionalTickets(
                        SortedBag.of(ChMap.tickets().subList(0, 3)),
                        SortedBag.of(ChMap.tickets().subList(0, 2)));
        GameState finalGS = temp;
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    finalGS.withInitiallyChosenTickets(
                            finalGS.currentPlayerId(), SortedBag.of(ChMap.tickets().subList(0, 2)));
                });
    }

    @Test
    void withInitiallyChosenTicketsWorks() {
        GameState temp = gameState;
        temp =
                temp.withInitiallyChosenTickets(
                        temp.currentPlayerId(), SortedBag.of(ChMap.tickets().subList(0, 3)));
        assertEquals(
                SortedBag.of(ChMap.tickets().subList(0, 3)), temp.currentPlayerState().tickets());
    }

    @Test
    void withChosenAdditionalTicketFails() {
        GameState temp = gameState;
        GameState finalGS = temp;
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    finalGS.withChosenAdditionalTickets(
                            SortedBag.of(ChMap.tickets().subList(0, 3)),
                            SortedBag.of(ChMap.tickets().subList(3, 6)));
                });
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    finalGS.withChosenAdditionalTickets(
                            SortedBag.of(ChMap.tickets().subList(0, 1)),
                            SortedBag.of(ChMap.tickets().subList(0, 6)));
                });
    }

    @Test
    void chosenAdditionalTickets() {
        GameState temp = gameState;
        temp =
                temp.withChosenAdditionalTickets(
                        SortedBag.of(ChMap.tickets().subList(0, 3)),
                        SortedBag.of(ChMap.tickets().subList(0, 2)));
        assertTrue(
                SortedBag.of(ChMap.tickets().subList(0, 2))
                        .equals(temp.currentPlayerState().tickets()));

        SortedBag<Ticket> drawnTickets = SortedBag.of(ChMap.tickets().subList(0, 3));
        // stays at 2 -weird
        SortedBag<Ticket> chosenTickets = SortedBag.of(ChMap.tickets().subList(0, 3));
        assertEquals(
                2,
                gameState.withChosenAdditionalTickets(drawnTickets, chosenTickets).ticketsCount());

        chosenTickets = SortedBag.of(ChMap.tickets().subList(0, 1));
        assertEquals(
                4,
                gameState.withChosenAdditionalTickets(drawnTickets, chosenTickets).ticketsCount());
    }

    @Test
    void withDrawnFaceUpCard() {
        GameState temp = gameState;
        Card topcard = temp.topCard();
        Card faceup = temp.cardState().faceUpCard(3);
        temp = temp.withDrawnFaceUpCard(3);
        assertTrue(temp.currentPlayerState().cards().contains(faceup));
        assertTrue(temp.cardState().faceUpCard(3) == topcard);
    }

    @Test
    void withBlindlyDrawnCard() {
        GameState temp = gameState;
        gameState = gameState.withBlindlyDrawnCard();
        assertNotEquals(temp.topCard(), gameState.topCard());
        assertEquals(temp.cardState().deckSize(), gameState.cardState().deckSize() + 1);

        Card topcard = temp.topCard();
        temp = temp.withBlindlyDrawnCard();
        assertTrue(temp.currentPlayerState().cards().contains(topcard));
    }

    @Test
    void withClaimedRoute() {
        GameState GS = gameState;
        Route claimedRoute = ChMap.routes().get(3);
        SortedBag<Card> claimCards = SortedBag.of(2, Card.BLUE, 1, Card.LOCOMOTIVE);
        // length is 2 so the number of cars used is 2 so carcount decreeases to 40 - 2 = 38
        gameState = gameState.withClaimedRoute(claimedRoute, claimCards);
        assertEquals(38, gameState.currentPlayerState().carCount());
        List<Route> routes = new ArrayList<>(ChMap.routes().subList(0, 17));
        for (Route route : routes) {
            GS = GS.withClaimedRoute(route, SortedBag.of(Card.RED));
            assertTrue(GS.currentPlayerState().routes().contains(route));
        }
    }

    @Test
    void lastTurnBegins() {
        assertFalse(gameState.lastTurnBegins());
        Route claimedRoute = ChMap.routes().get(2);
        SortedBag<Card> claimCards = SortedBag.of(2, Card.BLUE, 1, Card.LOCOMOTIVE);
        assertFalse(gameState.lastTurnBegins());
        for (int i = 0; i < 13; i++) {
            gameState = gameState.withClaimedRoute(claimedRoute, claimCards);
        }
        assertTrue(gameState.lastTurnBegins());
    }

    @Test
    void ForNextTurnWorks() {
        GameState temp = gameState;
        List<Route> routes = new ArrayList<>(ChMap.routes().subList(0, 17));
        routes.add(ChMap.routes().get(18));
        for (Route route : routes) {
            temp = temp.withClaimedRoute(route, SortedBag.of(Card.RED));
        }
        assertEquals(temp.currentPlayerId(), temp.forNextTurn().lastPlayer());
    }
}
