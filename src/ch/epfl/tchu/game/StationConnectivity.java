package ch.epfl.tchu.game;

/** Interface representing connectivity between two stations. */
public interface StationConnectivity {
    boolean connected(Station s1, Station s2);
}
