package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Ticket from a city to a city, country to country, country to city or city to country.
 *
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public final class Ticket implements Comparable<Ticket> {
    private Station from;
    private Station to;
    private final String textRepresentation;
    private final List<Trip> trips;

    /**
     * Given a list of trips from the same Station, constructs corresponding tickets. Main
     * constructor.
     *
     * @param trips trip to add.
     */
    public Ticket(List<Trip> trips) {
        Preconditions.checkArgument(!trips.isEmpty());
        String stationName = trips.get(0).from().name();
        // Check that all the stations have the same name. stationName is the name of the first
        // station.
        Preconditions.checkArgument(
                trips.stream().allMatch(trip -> trip.from().name().equals(stationName)));
        this.trips = List.copyOf(trips);
        this.textRepresentation = computeTextRepresentation();
    }

    /**
     * Create a ticket from a stations to another, counting a given number of points.
     *
     * @param from starting station.
     * @param to ending station.
     * @param points value of the ticket.
     */
    public Ticket(Station from, Station to, int points) {
        this(Collections.singletonList(new Trip(from, to, points)));
        this.from = from;
        this.to = to;
    }

    private String computeTextRepresentation() {
        TreeSet<String> endingStationsNames =
                trips.stream()
                        .map(p -> String.format("%s (%s)", p.to().name(), p.points()))
                        .collect(Collectors.toCollection(TreeSet::new));
        // All the stations have the same starting point, therefore we can take the first one.
        String startStationName = trips.get(0).from().name();
        // Add brackets in the representation depending on the number of destinations to display
        String template = (trips.size() > 1) ? "%s - {%s}" : "%s - %s";
        return String.format(template, startStationName, String.join(", ", endingStationsNames));
    }

    /**
     * Get the amount of points the ticket is worth.
     *
     * @param connectivity Connectivity of the player whose belongs the ticket.
     * @return amount of points.
     */
    public int points(StationConnectivity connectivity) {
        // If some is connected, we get the max (positive value). Otherwise, we return the negation
        // of the minimum number of points of the trip.
        // Because Trip.points already returns negative values depending on the connectivity, we can
        // simply take the maximum of all the points of all the trips.

        // On the technical side: This maps the trips List to a List of int corresponding to the
        // points of each trip. Then it computes the max.
        return trips.stream().mapToInt(trip -> trip.points(connectivity)).max().orElse(0);
    }

    /**
     * Gets the string representation of the ticket.
     *
     * @return the string representation of the ticket.
     */
    public String text() {
        return textRepresentation;
    }

    @Override
    public String toString() {
        return text();
    }

    public List<Trip> getTrips() {
        return trips;
    }

    /**
     * Lexicographically (on ticket's name) compare with the given ticket.
     *
     * @param that To compare.
     * @return 0 if equal, negative if given ticket is AFTER, positive if given ticket is BEFORE.
     *     (in the alphabet).
     */
    @Override
    public int compareTo(Ticket that) {
        return this.text().compareTo(that.text());
    }

    public Station getFrom() {
        return from;
    }

    public Station getTo() {
        return to;
    }
}
