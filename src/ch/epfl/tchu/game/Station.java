package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

/** Represents a standard station. Immutable. */
public final class Station {
    private final int id;
    private final String name;

    public Station(int id, String name) {
        Preconditions.checkArgument(id >= 0);
        this.id = id;
        this.name = name;
    }

    public String name() {
        return name;
    }

    public int id() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}
