package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

/**
 *  Represents a standard station. Immutable.
 *
 * @author Hugues Devimeux (327282)
 * */
public final class Station {
    private final int id;
    private final String name;

    /**
     * Default Station constructor.
     *
     * @param id id of the Station. Warning : Must be unique to each station!
     * @param name Name of the station.
     */
    public Station(int id, String name) {
        Preconditions.checkArgument(id >= 0);
        this.id = id;
        this.name = name;
    }

    /**
     * Returns the name of the station
     * @return name of the station
     */
    public String name() {
        return name;
    }

    /**
     * Returns the station's id
     * @return Station's id
     */
    public int id() {
        return id;
    }

    /**
     * Returns the station's name
     * @return Station's name
     */
    @Override
    public String toString() {
        return name;
    }
}
