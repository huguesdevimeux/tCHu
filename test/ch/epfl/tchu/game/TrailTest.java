package ch.epfl.tchu.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static ch.epfl.tchu.game.Trail.longest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TrailTest {
    private Station s1;
    private Station s2;
    private Station s3;
    private Station s4;
    private Route route1;
    private Route route2;
    private Route route3;
    private List<Route> listOfRoutes;

    @BeforeEach
    void setUp() {
        s1 = new Station(1, "S1");
        s2 = new Station(2, "S2");
        s3 = new Station(3, "S3");
        s4 = new Station(4, "S4");
        route1 = new Route("ID1", s1, s2, 5, Route.Level.OVERGROUND, Color.RED);
        route2 = new Route("ID2", s3, s4, 3, Route.Level.UNDERGROUND, null);
        route3 = new Route("ID3", s1, s4, 4, Route.Level.OVERGROUND, Color.RED);
        listOfRoutes = List.of(route1, route2);
    }

    @Test
    void assertLongestReturnsCorrectTrailWithIndependentNonTrivialRoutes() {
        assertEquals(longest(listOfRoutes).length(), route1.length());
    }

    @Test
    void assertLongestTrailIsCorrect() {
        listOfRoutes = List.of(route1, route2, route3);
        System.out.println(longest(listOfRoutes).toString());
        assertEquals("S2 - S1 - S4 - S3 (12)", longest(listOfRoutes).toString());
    }

    @Test
    void assertStationsAndLengthAreNullWithEmptyRouteList() {
        listOfRoutes = List.of();
        assertNull(longest(listOfRoutes).station1());
        assertNull(longest(listOfRoutes).station2());
        assertEquals(0, longest(listOfRoutes).length());
    }

    @Test
    void station1() {
        // route is the longest route in listOfRoutes so
        // s1 must be station1
        assertEquals(s1, longest(listOfRoutes).station1());
    }

    @Test
    void station2() {
        assertEquals(s2, longest(listOfRoutes).station2());
    }

    @Test
    void length() {
        assertEquals(5, longest(listOfRoutes).length());
    }

    @Test
    void assertToStringRepresentsCorrectTrail() {
        assertEquals("S1 - S2 (5)", longest(listOfRoutes).toString());
    }
}
