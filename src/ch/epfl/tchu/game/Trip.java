package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a Trip (an in-game move between two Station). Also known as "objectif" in the french
 * version of the game. Immutable class.
 *
 * @author Hugues Devimeux (327282)
 */
public final class Trip {

    private final Station from;
    private final Station to;
    private final int points;

    /**
     * Default constructor for Trip.
     *
     * @param from Starting {@link Station} of the trip. Must be not null.
     * @param to Ending {@link Station} of the trip. Must be not null.
     * @param points Amount of points of the Trip.
     */
    public Trip(Station from, Station to, int points) {
        Preconditions.checkArgument(points > 0);
        this.from = Objects.requireNonNull(from);
        this.to = Objects.requireNonNull(to);
        this.points = points;
    }

    /**
     * Returns all the possible trips between each Station of <code> from </code> list to <code>to
     * </code> list.
     *
     * @param from Starting stations
     * @param to Ending stations
     * @param points Amount of points of each trip.
     * @return All the possible trips.
     * @throws IllegalArgumentException if one of the list is empty or the amount of points is
     *     negative.
     */
    public static List<Trip> all(List<Station> from, List<Station> to, int points) {
        Preconditions.checkArgument(from.size() > 0 && to.size() > 0 && points >= 0);
        ArrayList<Trip> trips = new ArrayList<>();
        for (Station startingStation : from) {
            for (Station endingStation : to) {
                trips.add(new Trip(startingStation, endingStation, points));
            }
        }
        return trips;
    }

    public Station from() {
        return from;
    }

    public Station to() {
        return to;
    }

    public int points() {
        return points;
    }

    /**
     * Returns the number of points if the two <code>{@link Station}</code> are connected, the
     * negation of the points otherwise.
     *
     * @param connectivity The connectivity to check.
     * @return Number of points.
     */
    public int points(StationConnectivity connectivity) {
        if (connectivity.connected(from, to)) return points;
        return -points;
    }
}
