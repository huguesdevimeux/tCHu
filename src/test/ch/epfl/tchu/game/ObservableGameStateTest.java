package ch.epfl.tchu.game;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.gui.ObservableGameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ch.epfl.tchu.game.PlayerId.PLAYER_1;
import static ch.epfl.tchu.game.PlayerId.PLAYER_2;
import static org.junit.jupiter.api.Assertions.*;

public class ObservableGameStateTest {
    ObservableGameState gameState;
    PublicGameState publicGameState;

    @BeforeEach
    void setUp() {
        gameState = new ObservableGameState(PLAYER_1);
        PlayerState p1State =
                new PlayerState(
                        SortedBag.of(ChMap.tickets().subList(0, 10)),
                        SortedBag.of(1, Card.WHITE, 3, Card.RED),
                        ChMap.routes().subList(0, 3));

        PublicPlayerState p2State = new PublicPlayerState(0, 4, ChMap.routes().subList(3, 6));

        Map<PlayerId, PublicPlayerState> pubPlayerStates =
                Map.of(PLAYER_1, p1State, PLAYER_2, p2State);
        PublicCardState cardState = new PublicCardState(Card.ALL.subList(0, 5), 110 - 2 * 4 - 5, 0);
        publicGameState =
                new PublicGameState(36, cardState, PLAYER_1, pubPlayerStates, null);
        gameState.setState(publicGameState, p1State);
    }

    @Test
    void percentageOfTicketsIsCorrect() {
        int totalTicketsUsed = gameState.playerTicketCount(PLAYER_1).get() + gameState.playerTicketCount(PLAYER_2).get();
        assertEquals(10, totalTicketsUsed);
        assertEquals(((int) ((1 - (double) 10 / 46) * 100)), gameState.percentageTickets().get());
    }

    @Test
    void cardPercentageIsCorrect() {
        int cardCount = gameState.playerCardCount(PLAYER_1).get() + gameState.playerCardCount(PLAYER_2).get();
        assertEquals(4 + 4, cardCount);
        assertEquals(((int) ((1 - (double) (110 - 2 * 4 - 5) / 110) * 100)), gameState.percentageCards().get());
    }

    @Test
    void faceUpCardsAreTheSame() {
        for (Card card : Card.ALL.subList(0, 5)) {
            assertEquals(card, gameState.faceUpCard(Card.ALL.indexOf(card)).get());
        }
    }

    @Test
    void routesCorrespondToCorrectPlayer() {
        IntStream.range(6, ChMap.routes().size()).forEach(i -> assertNull(gameState.getRoutesOwner(ChMap.routes().get(i)).get()));
        IntStream.range(0, 3).forEach(i -> assertEquals(PLAYER_1, gameState.getRoutesOwner(ChMap.routes().get(i)).get()));
        IntStream.range(3, 6).forEach(i -> assertEquals(PLAYER_2, gameState.getRoutesOwner(ChMap.routes().get(i)).get()));
    }

    @Test
    void playersCountPropertiesMatch() {
        assertEquals(10, gameState.playerTicketCount(PLAYER_1).get());
        assertEquals(0, gameState.playerTicketCount(PLAYER_2).get());

        assertEquals(4, gameState.playerCardCount(PLAYER_1).get());
        assertEquals(4, gameState.playerCardCount(PLAYER_2).get());

        assertEquals(32, gameState.playerCarCount(PLAYER_1).get());
        assertEquals(36, gameState.playerCarCount(PLAYER_2).get());

        assertEquals(7 + 1 + 4, gameState.playerClaimPoints(PLAYER_1).get());
        assertEquals(2 + 1 + 1, gameState.playerClaimPoints(PLAYER_2).get());
    }

    @Test
    void playersTicketsMatch() {
        //got to put a f***** sorted bag to sort the mfers
        assertEquals(SortedBag.of(ChMap.tickets().subList(0, 10)).toList(), gameState.playersTicketsList());
    }

    @Test
    void currentPlayersNumberOfCards() {
        assertEquals(1, gameState.playerNumberOfCards(Card.WHITE).get());
        assertEquals(3, gameState.playerNumberOfCards(Card.RED).get());
        PlayerState ps =
                new PlayerState(
                        SortedBag.of(ChMap.tickets().subList(0, 10)),
                        SortedBag.of(1, Card.WHITE, 1, Card.RED)
                                .union(SortedBag.of(1, Card.BLACK, 1, Card.BLUE)
                                        .union(SortedBag.of(1, Card.VIOLET, 1, Card.GREEN)
                                                .union(SortedBag.of(1, Card.ORANGE, 1, Card.LOCOMOTIVE)
                                                        .union(SortedBag.of(1, Card.YELLOW))))),
                        ChMap.routes().subList(0, 3));
        gameState.setState(publicGameState, ps);
        for (int i = 0; i < Card.COUNT; i++) {
            assertEquals(1, gameState.playerNumberOfCards(Card.ALL.get(i)).get());
        }
    }

    @Test
    void canClaimRoute() {
        PlayerState p1State =
                new PlayerState(
                        SortedBag.of(ChMap.tickets().subList(0, 10)),
                        SortedBag.of(4, Card.RED),
                        List.of());
        gameState.setState(publicGameState, p1State);
        List<Route> claimableRoutesForPlayer1 =
                ChMap.routes().stream().filter(r -> r.color() != null)
                        .filter(route -> route.length() <= 3 && route.color().equals(Color.RED) && route.level().equals(Route.Level.OVERGROUND))
                        .collect(Collectors.toList());

        for (Route r : claimableRoutesForPlayer1) {
            assertTrue(gameState.playerCanClaimRoute(r).get());
        }
    }
}