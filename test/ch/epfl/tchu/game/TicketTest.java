package ch.epfl.tchu.game;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TicketTest {
    @Test
    void constructorFailsWithNoTrips() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Ticket(List.of());
        });
    }

    @Test
    void textIsCorrectForSimpleTicket() {
        var s1 = new Station(0, "From");
        var s2 = new Station(1, "To");
        var t = new Ticket(s1, s2, 1);
        assertEquals("From - To (1)", t.text());
    }

    @Test
    void textIsCorrectForSingleTripTicket() {
        var s1 = new Station(0, "From");
        var s2 = new Station(1, "To");
        var t = new Ticket(List.of(new Trip(s1, s2, 15)));
        assertEquals("From - To (15)", t.text());
    }

    @Test
    void textIsCorrectForMultipleTripTicket() {
        var map = new TestMap();
        assertEquals(
                "Berne - {Allemagne (6), Autriche (11), France (5), Italie (8)}",
                map.BER_NEIGHBORS.text());
        assertEquals(
                "France - {Allemagne (5), Autriche (14), Italie (11)}",
                map.FR_NEIGHBORS.text());
    }

    @Test
    void pointsAreCorrectWithNoConnectivity() {
        var map = new TestMap();
        var connectivity = new TestConnectivity(List.of(), List.of());
        assertEquals(-13, map.LAU_STG.points(connectivity));
        assertEquals(-5, map.BER_NEIGHBORS.points(connectivity));
        assertEquals(-5, map.FR_NEIGHBORS.points(connectivity));
    }

    @Test
    void pointsAreCorrectWithFullConnectivity() {
        var map = new TestMap();
        var connectivity = new FullConnectivity();
        assertEquals(+13, map.LAU_STG.points(connectivity));
        assertEquals(+11, map.BER_NEIGHBORS.points(connectivity));
        assertEquals(+14, map.FR_NEIGHBORS.points(connectivity));
    }

    @Test
    void pointsAreCorrectWithPartialConnectivity() {
        var map = new TestMap();
        var connectivity = new TestConnectivity(
                List.of(map.LAU, map.BER, map.BER, map.FR1, map.FR2, map.FR2),
                List.of(map.BER, map.FR2, map.DE3, map.IT1, map.IT2, map.DE1));
        assertEquals(-13, map.LAU_STG.points(connectivity));
        assertEquals(+6, map.BER_NEIGHBORS.points(connectivity));
        assertEquals(+11, map.FR_NEIGHBORS.points(connectivity));
    }

    @Test
    void compareToWorksOnKnownTickets() {
        var map = new TestMap();
        assertTrue(map.LAU_BER.compareTo(map.LAU_STG) < 0);
        assertTrue(map.LAU_BER.compareTo(map.FR_NEIGHBORS) > 0);
        //noinspection EqualsWithItself
        assertEquals(0, map.LAU_BER.compareTo(map.LAU_BER));
    }

    private static final class FullConnectivity implements StationConnectivity {
        @Override
        public boolean connected(Station s1, Station s2) {
            return true;
        }
    }

    private static final class TestConnectivity implements StationConnectivity {
        private final List<Station> stations1;
        private final List<Station> stations2;

        public TestConnectivity(List<Station> stations1, List<Station> stations2) {
            assert stations1.size() == stations2.size();
            this.stations1 = List.copyOf(stations1);
            this.stations2 = List.copyOf(stations2);
        }

        @Override
        public boolean connected(Station s1, Station s2) {
            for (int i = 0; i < stations1.size(); i++) {
                var t1 = stations1.get(i);
                var t2 = stations2.get(i);

                if (t1.equals(s1) && t2.equals(s2) || t1.equals(s2) && t2.equals(s1))
                    return true;
            }
            return false;
        }
    }

    private static final class TestMap {
        // Stations - cities
        public final Station BER = new Station(0, "Berne");
        public final Station LAU = new Station(1, "Lausanne");
        public final Station STG = new Station(2, "Saint-Gall");

        // Stations - countries
        public final Station DE1 = new Station(3, "Allemagne");
        public final Station DE2 = new Station(4, "Allemagne");
        public final Station DE3 = new Station(5, "Allemagne");
        public final Station AT1 = new Station(6, "Autriche");
        public final Station AT2 = new Station(7, "Autriche");
        public final Station IT1 = new Station(8, "Italie");
        public final Station IT2 = new Station(9, "Italie");
        public final Station IT3 = new Station(10, "Italie");
        public final Station FR1 = new Station(11, "France");
        public final Station FR2 = new Station(12, "France");

        // Countries
        public final List<Station> DE = List.of(DE1, DE2, DE3);
        public final List<Station> AT = List.of(AT1, AT2);
        public final List<Station> IT = List.of(IT1, IT2, IT3);
        public final List<Station> FR = List.of(FR1, FR2);

        public final Ticket LAU_STG = new Ticket(LAU, STG, 13);
        public final Ticket LAU_BER = new Ticket(LAU, BER, 2);
        public final Ticket BER_NEIGHBORS = ticketToNeighbors(List.of(BER), 6, 11, 8, 5);
        public final Ticket FR_NEIGHBORS = ticketToNeighbors(FR, 5, 14, 11, 0);

        private Ticket ticketToNeighbors(List<Station> from, int de, int at, int it, int fr) {
            var trips = new ArrayList<Trip>();
            if (de != 0) trips.addAll(Trip.all(from, DE, de));
            if (at != 0) trips.addAll(Trip.all(from, AT, at));
            if (it != 0) trips.addAll(Trip.all(from, IT, it));
            if (fr != 0) trips.addAll(Trip.all(from, FR, fr));
            return new Ticket(trips);
        }
    }
}