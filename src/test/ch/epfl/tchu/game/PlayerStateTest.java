package ch.epfl.tchu.game;

import ch.epfl.tchu.SortedBag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerStateTest {
    private PlayerState playerState;
    private SortedBag<Ticket> tickets;
    private SortedBag<Card> cards;
    private List<Route> routes;
    private PlayerState staticPlayerState;

    @BeforeEach
    void setUp() {
        tickets = SortedBag.of(ChMap.tickets().subList(0, 4));
        cards = SortedBag.of(Card.ALL);
        routes = ChMap.routes().subList(0, 4);
        playerState = new PlayerState(tickets, cards, routes);
        staticPlayerState =
                PlayerState.initial(
                        SortedBag.of(List.of(Card.BLUE, Card.VIOLET, Card.ORANGE, Card.WHITE)));
    }

    @Test
    void testingInitialReturnsCorrectPlayerStateAndThrowsException() {
        assertEquals(0, staticPlayerState.tickets().size());
        assertEquals(0, staticPlayerState.routes().size());
        assertEquals(4, staticPlayerState.cards().size());
        // setting initial cards < 4 to see if exception is thrown
        assertThrows(
                IllegalArgumentException.class,
                () -> PlayerState.initial(SortedBag.of(Card.ALL.subList(0, 2))));
    }

    @Test
    void returnsCorrectListOfTickets() {
        assertEquals(tickets, playerState.tickets());
    }

    @Test
    void returnsCorrectListOfCards() {
        assertEquals(cards, playerState.cards());
    }

    @Test
    void addedTicketsTakesInAccountNewTickets() {
        int initialTicketsSize = playerState.ticketCount(); // 4
        int addedTickets =
                playerState.withAddedTickets(SortedBag.of()).ticketCount(); // adding 0 tickets
        assertEquals(addedTickets - initialTicketsSize, 0);
        addedTickets =
                playerState
                        .withAddedTickets(SortedBag.of(ChMap.tickets().get(4)))
                        .ticketCount(); // adding 1 ticket to the initial stack of tickets
        assertEquals(addedTickets - initialTicketsSize, 1);
        addedTickets =
                playerState
                        .withAddedTickets(SortedBag.of(ChMap.tickets().subList(4, 7)))
                        .ticketCount(); // added 3 new tickets
        assertEquals(addedTickets - initialTicketsSize, 3);
    }

    @Test
    void withAddedCardsTakesInAccountNewCards() {
        // adding 1 card and testing withAddedCard()
        int initialCards = playerState.cardCount();
        int addedCards = playerState.withAddedCard(Card.BLUE).cardCount();
        assertEquals(addedCards - initialCards, 1);
        // adding 3 cards
//        addedCards = playerState.withAddedCards(SortedBag.of(Card.ALL.subList(2, 5))).cardCount();
//        assertEquals(addedCards - initialCards, 3);
//        // adding 0 cards
//        addedCards = playerState.withAddedCards(SortedBag.of()).cardCount();
//        assertEquals(addedCards - initialCards, 0);
    }

    @Test
    void canCorrectlyClaimRoute() {
        List<Card> cardList = new ArrayList<>(cards.toList().subList(0, 3));
        Collections.fill(cardList, Card.BLUE);
        cardList.add(Card.LOCOMOTIVE);
        Route successful =
                new Route(
                        "ID",
                        ChMap.stations().get(0),
                        ChMap.stations().get(1),
                        4,
                        Route.Level.UNDERGROUND,
                        Color.BLUE);
        // cardlist is 3xBLUE 1xLOCO & length is 4 so both conditions are met as carcount is 36
        playerState = new PlayerState(tickets, SortedBag.of(cardList), List.of(successful));
        assertTrue(playerState.canClaimRoute(successful));
        Route failingRoute =
                new Route(
                        "ID",
                        ChMap.stations().get(0),
                        ChMap.stations().get(1),
                        6,
                        Route.Level.UNDERGROUND,
                        Color.BLUE);
        playerState = new PlayerState(tickets, SortedBag.of(cardList), List.of(failingRoute));
        assertFalse(playerState.canClaimRoute(failingRoute));
    }

    @Test
    void claimCardsExceptionIsThrown() {
        PlayerState a =
                new PlayerState(
                        tickets, cards, ChMap.routes().subList(0, 17)); // reduces car count to 4
        assertThrows(
                IllegalArgumentException.class,
                () -> a.possibleClaimCards(ChMap.routes().get(22))); // length of route 22 is 6
    }

    @Test
    void possibleClaimCards1() {
        Route plainRoute =
                new Route(
                        "ID",
                        ChMap.stations().get(0),
                        ChMap.stations().get(1),
                        1,
                        Route.Level.UNDERGROUND,
                        Color.BLUE);
        List<Card> cardList = new ArrayList<>(cards.toList());
        cardList.add(Card.BLUE);
        List<Route> allRoutes = new ArrayList<>(routes);
        allRoutes.add(plainRoute);
        playerState = new PlayerState(tickets, SortedBag.of(cardList), allRoutes);
        assertEquals(
                "[{BLUE}, {LOCOMOTIVE}]", playerState.possibleClaimCards(plainRoute).toString());
    }

    @Test
    void possibleClaimCards2() {
        Route plainRoute =
                new Route(
                        "ID",
                        ChMap.stations().get(0),
                        ChMap.stations().get(1),
                        2,
                        Route.Level.UNDERGROUND,
                        Color.BLUE);
        List<Card> cardList = new ArrayList<>(cards.toList());
        cardList.add(Card.BLUE);
        List<Route> allRoutes = new ArrayList<>(routes);
        allRoutes.add(plainRoute);
        playerState = new PlayerState(tickets, SortedBag.of(cardList), allRoutes);
        assertEquals(
                "[{2×BLUE}, {BLUE, LOCOMOTIVE}]",
                playerState.possibleClaimCards(plainRoute).toString());
    }

    @Test
    void possibleClaimCards3() {
        Route plainRoute =
                new Route(
                        "ID",
                        ChMap.stations().get(0),
                        ChMap.stations().get(1),
                        3,
                        Route.Level.UNDERGROUND,
                        Color.BLUE);
        List<Card> cardList = new ArrayList<>(cards.toList());
        cardList.add(Card.BLUE);
        List<Route> allRoutes = new ArrayList<>(routes);
        allRoutes.add(plainRoute);
        playerState = new PlayerState(tickets, SortedBag.of(cardList), allRoutes);
        assertEquals(
                "[{2×BLUE, LOCOMOTIVE}]", playerState.possibleClaimCards(plainRoute).toString());
    }

    @Test
    void PossibleAdditionalCardsThrowsExceptions() {
        SortedBag<Card> setOf3Cards = SortedBag.of(Card.ALL.subList(0, 3));
//        assertThrows(
//                IllegalArgumentException.class,
//                () ->
//                        playerState.possibleAdditionalCards(
//                                -1, SortedBag.of(Card.BLUE), setOf3Cards));
//        assertThrows(
//                IllegalArgumentException.class,
//                () -> playerState.possibleAdditionalCards(4, SortedBag.of(Card.BLUE), setOf3Cards));
//        assertThrows(
//                IllegalArgumentException.class,
//                () -> playerState.possibleAdditionalCards(1, SortedBag.of(), setOf3Cards));
//        assertThrows(
//                IllegalArgumentException.class,
//                () ->
//                        playerState.possibleAdditionalCards(
//                                1, SortedBag.of(Card.ALL.subList(0, 3)), setOf3Cards));
//        assertThrows(
//                IllegalArgumentException.class,
//                () ->
//                        playerState.possibleAdditionalCards(
//                                1, SortedBag.of(Card.BLUE), SortedBag.of()));
    }

    @Test
    void possibleAdditionalCards2() {
        int additionalCards = 2;
        List<Card> initialCards =
                List.of(
                        Card.GREEN,
                        Card.GREEN,
                        Card.GREEN,
                        Card.BLUE,
                        Card.BLUE,
                        Card.LOCOMOTIVE,
                        Card.LOCOMOTIVE);
        SortedBag<Card> drawnCards = SortedBag.of(2, Card.GREEN, 1, Card.RED);
        Route green =
                new Route(
                        "id",
                        ChMap.stations().get(0),
                        ChMap.stations().get(1),
                        1,
                        Route.Level.UNDERGROUND,
                        Color.GREEN);
        SortedBag<Card> a = SortedBag.of(2, Card.GREEN);
        SortedBag<Card> b = SortedBag.of(1, Card.GREEN, 1, Card.LOCOMOTIVE);
        SortedBag<Card> c = SortedBag.of(2, Card.LOCOMOTIVE);
        List<SortedBag<Card>> expectedList = List.of(a, b, c);
        PlayerState newPS = new PlayerState(tickets, SortedBag.of(initialCards), List.of(green));
//        assertEquals(
//                expectedList,
//                newPS.possibleAdditionalCards(
//                        additionalCards, SortedBag.of(Card.GREEN), drawnCards));
        // to Test if it should return an empty list when you play with all the initial cards?
        // assertEquals(Collections.emptyList(), newPS.possibleAdditionalCards(additionalCards,
        // SortedBag.of(initialCards), drawnCards) );
    }

    @Test
    void withClaimedRoute() {
        Route toClaim = ChMap.routes().get(14);
        SortedBag<Card> claimCards = SortedBag.of(Card.GREEN);
        assertEquals(5, playerState.withClaimedRoute(toClaim, claimCards).routes().size());
        assertEquals(8, playerState.withClaimedRoute(toClaim, claimCards).cards().size());
        assertTrue(playerState.withClaimedRoute(toClaim, claimCards).routes().contains(toClaim));
    }

    @Test
    void returnsCorrectTicketPoints() {
        Station s1 = new Station(3, "SUH DUDE");
        Station s2 = new Station(3, "42069");
        Station s3 = new Station(5, "ID");
        Station s4 = new Station(6, "hi");
        Route a = new Route("id1", s1, s2, 3, Route.Level.UNDERGROUND, Color.BLUE);
        Route b = new Route("id2", s1, s3, 4, Route.Level.UNDERGROUND, Color.BLUE);
        Route c = new Route("id3", s3, s4, 5, Route.Level.OVERGROUND, Color.BLACK);
        SortedBag<Ticket> tickets2 =
                SortedBag.of(List.of(new Ticket(s1, s2, 10), new Ticket(s3, s1, 8)));

        List<Route> routes = List.of(a, b);
        playerState = new PlayerState(tickets2, cards, routes);
        assertEquals(18, playerState.ticketPoints());

        routes = List.of(a, c);
        playerState = new PlayerState(tickets2, cards, routes);
        assertEquals(2, playerState.ticketPoints());
    }

    @Test
    void returnsCorrectFinalPoints() {
        int claimPoints = playerState.claimPoints();
        int ticketPoints = playerState.ticketPoints();
        assertEquals(playerState.finalPoints(), claimPoints + ticketPoints);
    }
}
