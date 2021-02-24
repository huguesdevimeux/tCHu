package ch.epfl.tchu.game;

/**
 * Interface representing connectivity between two stations.
 */
public interface StationConnectivity {
    /**
     * Check the connectivity between two stations, s1 and s2.
     * @param s1    First Station.
     * @param s2    Second station.
     * @return      Wether the two stations are connected.
     */
    boolean connected(Station s1, Station s2);
}
