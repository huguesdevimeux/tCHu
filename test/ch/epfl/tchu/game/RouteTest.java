package ch.epfl.tchu.game;

import ch.epfl.tchu.SortedBag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteTest {

    private Station s1;
    private Route overGroundRoute;
    private Route underGroundRoute;
    private Station s2;
    private List<Card> claimCards;
    private SortedBag<Card> c;
    private List<Card> drawnCards;
    private SortedBag<Card> d;

    @BeforeEach
    void setUp() {
        s1 = new Station(1, "from");
        s2 = new Station(2, "to");
        overGroundRoute = new Route("ID", s1, s2, 5, Route.Level.OVERGROUND, Color.RED);
        underGroundRoute = new Route("ID", s1, s2, 2, Route.Level.UNDERGROUND, null);
        claimCards = List.of(Card.RED);
        c = SortedBag.of(claimCards);
        drawnCards = List.of(Card.BLACK, Card.RED, Card.LOCOMOTIVE);
        d = SortedBag.of(drawnCards);
    }

    @Test
    void RaisesIllegalArgumentExceptionWHenTwoStationsAreEquals() {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    new Route("ID", s1, s1, 5, Route.Level.UNDERGROUND, Color.RED);
                });
        // Test when two stations have the same name - it shouldn't raise Anything.
        // new Route("ID", s1, new Station(1, "from"),  5, Route.Level.UNDERGROUND, Color.RED);
    }

    @Test
    void RaisesIllegalArgumentExceptionWhenLengthUnbound() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new Route(
                                "ID",
                                s1,
                                s2,
                                Constants.MAX_ROUTE_LENGTH + 1,
                                Route.Level.UNDERGROUND,
                                Color.RED));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new Route(
                                "ID",
                                s1,
                                s2,
                                Constants.MIN_ROUTE_LENGTH - 1,
                                Route.Level.UNDERGROUND,
                                Color.RED));
        new Route("ID", s1, s2, Constants.MIN_ROUTE_LENGTH, Route.Level.UNDERGROUND, Color.RED);
        new Route("ID", s1, s2, Constants.MAX_ROUTE_LENGTH, Route.Level.UNDERGROUND, Color.RED);
    }

    @Test
    void station1() {
        assertEquals(overGroundRoute.station1(), s1);
    }

    @Test
    void station2() {
        assertEquals(overGroundRoute.station2(), s2);
    }

    @Test
    void length() {
        assertEquals(5, overGroundRoute.length());
    }

    @Test
    void stations() {
        assertIterableEquals(List.of(s1, s2), overGroundRoute.stations());
    }

    @Test
    void stationOpposite() {
        assertThrows(
                IllegalArgumentException.class,
                () -> overGroundRoute.stationOpposite(new Station(3, "caca")));
        assertEquals(s1, overGroundRoute.stationOpposite(s2));
    }

    @Test
    void possibleClaimCardsForOverGroundAndNonNullColor() {
        SortedBag.Builder<Card> cardBuilder = new SortedBag.Builder<>();
        for (int i = 0; i < overGroundRoute.length(); i++) {
            cardBuilder.add(Card.RED);
        }
        assertEquals(overGroundRoute.possibleClaimCards(), List.of(cardBuilder.build()));
    }

    @Test
    void possibleClaimCardsForUnderGroundAndNullColor() {
        SortedBag.Builder<Card> cB1 = new SortedBag.Builder<>();
        SortedBag.Builder<Card> cB2 = new SortedBag.Builder<>();
        SortedBag.Builder<Card> cB3 = new SortedBag.Builder<>();
        List<SortedBag<Card>> cardList;

        // First index of the example given on {@link https://cs108.epfl.ch/p/02_routes-trails.html}
        cB1.add(Card.BLACK);
        cB1.add(Card.BLACK);
        // 9th index of the example given
        cB2.add(Card.VIOLET);
        cB2.add(Card.LOCOMOTIVE);
        // 16th index of the example given
        cB3.add(Card.LOCOMOTIVE);
        cB3.add(Card.LOCOMOTIVE);

        cardList = List.of(cB1.build(), cB2.build(), cB3.build());
        assertEquals(underGroundRoute.possibleClaimCards().get(0), cardList.get(0));
        assertEquals(underGroundRoute.possibleClaimCards().get(9), cardList.get(1));
        assertEquals(underGroundRoute.possibleClaimCards().get(16), cardList.get(2));
    }

    @Test
    void RaisesIllegalArgumentExceptionIfRouteIsOvergroundForAdditionalClaimCards() {
        assertThrows(
                IllegalArgumentException.class,
                () -> overGroundRoute.additionalClaimCardsCount(c, d));
    }

    @Test
    void RaisesIllegalArgumentExceptionWhenDrawnCardsIsOutOfBounds() {
        drawnCards = List.of(Card.BLACK, Card.LOCOMOTIVE);
        d = SortedBag.of(drawnCards);
        assertThrows(
                IllegalArgumentException.class,
                () -> overGroundRoute.additionalClaimCardsCount(c, d));
    }

    @Test
    void ReturnsCorrectAmountOfAdditionalCards() {
        assertEquals(2, underGroundRoute.additionalClaimCardsCount(c, d));
    }

    @Test
    void ReturnsCorrectAmountOfAdditionalCardsForLocomotiveCards() {
        claimCards = List.of(Card.LOCOMOTIVE);
        c = SortedBag.of(claimCards);
        assertEquals(1, underGroundRoute.additionalClaimCardsCount(c, d));
    }

    @Test
    void claimPoints() {
        assertEquals(10, overGroundRoute.claimPoints());
    }
}
