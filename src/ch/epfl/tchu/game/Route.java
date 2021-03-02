package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.List;
import java.util.Objects;

/**
 * Representation of a route that links two nearby stations.
 * Immutable.
 *
 * @author Luca Mouchel (324748)
 * @author Hugues Devimeux (327282)
 */
public final class Route {
    private final String id;

    private final Station station1;
    private final Station station2;
    private final int length;
    private final Level level;
    private final Color color;

    /** Enum defines the type of route. */
    public enum Level {
        OVERGROUND,
        UNDERGROUND
    }

    /**
     * Route constructor.
     *
     * @param id Routes' identity
     * @param station1 First station of the route
     * @param station2 Second station of the route
     * @param length Length of the route
     * @param level Defines what type of route it is
     * @param color Can be any color
     * @throws IllegalArgumentException If stations 1 and 2 are the same or if length is out of
     *     bounds defined by Constants.java
     * @throws NullPointerException if either id, station1, station2 or level are null
     */
    public Route(
            String id, Station station1, Station station2, int length, Level level, Color color) {
        this.id = Objects.requireNonNull(id);
        this.station1 = Objects.requireNonNull(station1);
        this.station2 = Objects.requireNonNull(station2);
        this.level = Objects.requireNonNull(level);
        this.length = length;
        this.color = color;
        Preconditions.checkArgument(!(station1.equals(station2)));
        Preconditions.checkArgument(
                length >= Constants.MIN_ROUTE_LENGTH && length <= Constants.MAX_ROUTE_LENGTH);
    }

    /**
     * Returns routes' id.
     *
     * @return id
     */
    public String id() {
        return id;
    }

    /**
     * Returns station1.
     *
     * @return station1
     */
    public Station station1() {
        return station1;
    }

    /**
     * Returns station2.
     *
     * @return station2
     */
    public Station station2() {
        return station2;
    }

    /**
     * Returns routes' length.
     *
     * @return length
     */
    public int length() {
        return length;
    }

    /**
     * Returns the routes' level: underground or overground.
     *
     * @return level
     */
    public Level level() {
        return level;
    }

    /**
     * Returns the routes' color or null if the color is plain.
     *
     * @return color
     */
    public Color color() {
        return color;
    }

    /**
     * Returns List of the two stations in the order they appear in the constructor.
     *
     * @return list of the two stations
     */
    public List<Station> stations() {
        return List.of(station1, station2);
    }

    /**
     * Returns the opposite station from which this method is called.
     *
     * @param station The station to get the opposite of.
     * @throws IllegalArgumentException if argument station is neither of the start/end stations.
     * @return opposite station
     */
    public Station stationOpposite(Station station) {
        Preconditions.checkArgument((station.equals(station1) || station.equals(station2)));
        if (station.equals(station1)) {
            return station2;
        } else {
            return station1;
        }
    }
}
