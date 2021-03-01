package ch.epfl.tchu.game;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class RouteTest {

    private Station s1;
    private Route standardRoute;
    private Station s2;

    @BeforeEach
    void setUp() {
        s1 = new Station(1, "from");
        s2 = new Station(2, "to");
        standardRoute = new Route("ID", s1, s2, 5, Route.Level.OVERGROUND, Color.RED);
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
        assertEquals(standardRoute.station1(), s1);
    }

    @Test
    void station2() {
        assertEquals(standardRoute.station2(), s2);
    }

    @Test
    void length() {
        assertEquals(5, standardRoute.length());
    }

    @Test
    void stations() {
        assertIterableEquals(List.of(s1, s2), standardRoute.stations());
    }

    @Test
    void stationOpposite() {
        assertThrows(
                IllegalArgumentException.class,
                () -> standardRoute.stationOpposite(new Station(3, "caca")));
        assertEquals(s1, standardRoute.stationOpposite(s2));
    }

    @Test
    void claimPoints() {
        assertEquals(standardRoute.claimPoints(), 10);
    }
}
