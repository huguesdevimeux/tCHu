package ch.epfl.tchu.game;

import static org.junit.jupiter.api.Assertions.*;

import ch.epfl.tchu.SortedBag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class RouteTest {

    private Station s1;
    private Route overGroundRoute;
    private Route underGroundRoute;
    private Station s2;
    private SortedBag.Builder<Card> cardBuilder;
    private List<SortedBag<Card>> cardList;
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
        cardBuilder = new SortedBag.Builder<>();
        cardList = new ArrayList<>();
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
        for (int i = 0; i < overGroundRoute.length(); i++) {
            cardBuilder.add(Card.RED);
        }
        assertEquals(List.of(cardBuilder.build()), overGroundRoute.possibleClaimCards());
    }

    @Test
    void possibleClaimCardsForUnderGroundAndNullColor() {
        for (int i = underGroundRoute.length(); i > 0; i--) {
            for (Card card : Card.CARS) {
                for (int j = 0; j < i; j++) {
                    cardBuilder.add(card);
                }
                while (cardBuilder.size() < underGroundRoute.length()) {
                    cardBuilder.add(Card.LOCOMOTIVE);
                }
                cardList.add(cardBuilder.build());
                cardBuilder = new SortedBag.Builder<>();
            }
        }
        for (int k = 0; k < underGroundRoute.length(); k++) {
            cardBuilder.add(Card.LOCOMOTIVE);
        }
        cardList.add(cardBuilder.build());
        assertEquals(cardList, underGroundRoute.possibleClaimCards());
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
