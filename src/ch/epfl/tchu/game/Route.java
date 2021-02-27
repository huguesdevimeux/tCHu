package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.List;

public final class Route {
    /**
     * Representation of a route that links two nearby stations.
     *
     * @author Luca Mouchel (324748)
     * @author Hugues Devimeux (327282)
     */
    private final String id;

    private final Station station1;
    private final Station station2;
    private final int length;
    private final Level level;
    private final Color color;

    /** Enum defines the type of route. */
    public enum Level {
        OVERGROUND,
        UNDERGROUND;
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
     * @throws IllegalArgumentException If stations have same name or if length is out of bounds
     *     defined by Constants.java
     * @throws NullPointerException if either id, station1, station2 or level are null
     */
    public Route(
            String id, Station station1, Station station2, int length, Level level, Color color) {
        this.id = id;
        this.station1 = station1;
        this.station2 = station2;
        this.length = length;
        this.level = level;
        this.color = color;

        Preconditions.checkArgument(
                !(station1.name().equals(station2.name())
                        && (length < Constants.MIN_ROUTE_LENGTH
                                && length > Constants.MAX_ROUTE_LENGTH)));
        if ((id == null) || (station1 == null) || (station2 == null) || (level == null)) {
            throw new NullPointerException();
        }
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
     * @param station station
     * @throws IllegalArgumentException if argument station is neither of the stations called in the
     *     constructor
     * @return opposite station
     */
    public Station stationOpposite(Station station) {
        Preconditions.checkArgument(
                (station.name().equals(station1.name()) || station.name().equals(station2.name())));
        if (station.name().equals(station1.name())) {
            return station2;
        } else {
            return station1;
        }
    }
}
