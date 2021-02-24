package ch.epfl.tchu.game;

import ch.epfl.test.TestRandomizer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TripTest {
    @Test
    void allWorksOnKnownExample() {
        var from = List.of(
                new Station(0, "Lausanne"),
                new Station(1, "Neuchâtel"));
        var to = List.of(
                new Station(2, "Berne"),
                new Station(3, "Zürich"),
                new Station(4, "Coire"));
        var points = 17;

        var expectedFromToIds = new int[][]{
                new int[]{0, 2},
                new int[]{0, 3},
                new int[]{0, 4},
                new int[]{1, 2},
                new int[]{1, 3},
                new int[]{1, 4},
        };
        var all = Trip.all(from, to, points);
        assertEquals(from.size() * to.size(), all.size());
        outer: for (var expectedFromToId : expectedFromToIds) {
            var fromId = expectedFromToId[0];
            var toId = expectedFromToId[1];
            for (var trip : all) {
                if (trip.from().id() == fromId && trip.to().id() == toId)
                    continue outer;
            }
            fail(String.format("Missing trip from %s to %s", fromId, toId));
        }
    }

    @Test
    void constructorFailsWithNullStations() {
        assertThrows(NullPointerException.class, () -> {
            new Trip(null, new Station(1, "Lausanne"), 1);
        });
        assertThrows(NullPointerException.class, () -> {
            new Trip(new Station(1, "Lausanne"), null, 1);
        });
    }

    @Test
    void constructorFailsWithInvalidPoints() {
        var s1 = new Station(0, "Lausanne");
        var s2 = new Station(1, "EPFL");
        assertThrows(IllegalArgumentException.class, () -> {
            new Trip(s1, s2, 0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new Trip(s1, s2, -1);
        });
    }

    @Test
    void fromToAndPointsReturnWhatTheyShould() {
        var rng = TestRandomizer.newRandom();
        for (var i = 0; i < TestRandomizer.RANDOM_ITERATIONS; i++) {
            var fromId = rng.nextInt(100);
            var from = new Station(fromId, "Lausanne");
            var to = new Station(fromId + 1, "Neuchâtel");
            var points = 1 + rng.nextInt(10);
            var trip = new Trip(from, to, points);
            assertEquals(from, trip.from());
            assertEquals(to, trip.to());
            assertEquals(points, trip.points());
        }
    }

    @Test
    void pointsReturnsPositivePointsWhenConnectedAndNegativePointsOtherwise() {
        var connected = new FullConnectivity();
        var notConnected = new NoConnectivity();

        var rng = TestRandomizer.newRandom();
        for (int i = 0; i < TestRandomizer.RANDOM_ITERATIONS; i++) {
            var fromId = rng.nextInt(100);
            var from = new Station(fromId, "Lugano");
            var to = new Station(fromId + 1, "Wassen");
            var points = 1 + rng.nextInt(10);
            var trip = new Trip(from, to, points);
            assertEquals(+points, trip.points(connected));
            assertEquals(-points, trip.points(notConnected));
        }
    }

    private static final class FullConnectivity implements StationConnectivity {
        @Override
        public boolean connected(Station s1, Station s2) {
            return true;
        }
    }

    private static final class NoConnectivity implements StationConnectivity {
        @Override
        public boolean connected(Station s1, Station s2) {
            return false;
        }
    }
}