package ch.epfl.tchu.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class PublicPlayerStateTest {
    private int ticketCount;
    private List<Route> routes;
    private PublicPlayerState publicPlayerState;
    private Route route1;
    private Route route2;
    private final Station a = ChMap.stations().get(0);
    private final Station b = ChMap.stations().get(1);
    private final Station c = ChMap.stations().get(2);
    private final Station d = ChMap.stations().get(3);

    @BeforeEach
    void setUp() {
        route1 = new Route("Lausanne", a, b, 5, Route.Level.UNDERGROUND, Color.BLACK);
        route2 = new Route("Berne", c, d, 6, Route.Level.OVERGROUND, null);
        routes = List.of(route1, route2);
        publicPlayerState = new PublicPlayerState(5, 6, routes);
    }

    @Test
    void returnsCorrectAmountsOfTicketAndCardCounts() {
        assertEquals(5, publicPlayerState.ticketCount());
        assertEquals(6, publicPlayerState.cardCount());
    }

    @Test
    void assertThrowsExceptionIfOutOfBounds() {
        assertThrows(IllegalArgumentException.class, () -> new PublicPlayerState(-1, 4, routes));
        assertThrows(IllegalArgumentException.class, () -> new PublicPlayerState(1, -1, routes));
    }

    @Test
    void returnsCorrectRoutes() {
        assertEquals(routes, publicPlayerState.routes());
        //testing also with empty lists of routes
        routes = List.of();
        assertEquals(routes, new PublicPlayerState(1, 1, List.of()).routes());
    }

    @Test
    void returnsCorrectAmountOfClaimPoints() {
        // one route is length 5, the other 6 - so the total must be 10 + 15 = 25
        assertEquals(25, publicPlayerState.claimPoints());
        route1 = new Route("a", a, b, 1, Route.Level.OVERGROUND, null);
        routes = List.of(route1);
        assertEquals(1, new PublicPlayerState(1, 1, routes).claimPoints());
    }

    @Test
    void returnsCorrectCarCount() {
        // 40 - 5 - 6
        assertEquals(29, publicPlayerState.carCount());
    }
}
