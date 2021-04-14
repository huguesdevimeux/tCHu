package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.stream.IntStream;

/**
 * Represents a flat partition.
 * Immutable.
 *
 * @author Luca Mouchel (324748)
 * @author Hugues Devimeux (327282)
 */
public final class StationPartition implements StationConnectivity {
    private final int[] flatPartition;

    /**
     * Private constructor, not instantiable.
     *
     * @param flatPartition instantiates array representing flat partition
     */
    private StationPartition(int[] flatPartition) {
        this.flatPartition = flatPartition;
    }

    /**
     * Returns true if both stations belong in the same partition. However, stations can be out of
     * bounds of the array. If one of them is, returns true if both ids are the same.
     *
     * @param s1 First Station.
     * @param s2 Second station.
     * @return true if s1 and s2 are considered connected.
     */
    @Override
    public boolean connected(Station s1, Station s2) {
        // Check if  one of the two station is is out of bound of the partition.
        // Following the paper, we return true iff the two ids are equals (they are the same
        // station).
        if (Math.max(s1.id(), s2.id()) >= flatPartition.length) return s1.id() == s2.id();
        // Returns whether the two stations have the same representative.
        return flatPartition[s1.id()] == flatPartition[s2.id()];
    }

    /** Represents the deep partition. */
    public static final class Builder {
        private final int stationCount;
        private final int[] partition;

        /**
         * Instantiates a new Builder as well as an array with the number of stations.
         *
         * @param stationCount the number of stations.
         * @throws IndexOutOfBoundsException if id isn't between 0 and stationCount(excluded).
         * @throws IllegalArgumentException if stationCount is negative.
         */
        public Builder(int stationCount) {
            Preconditions.checkArgument(stationCount >= 0);
            this.stationCount = stationCount;
            // Creates the partition by setting each station representative to itself.
            // The array will be [0,1,2,3,4....stationCount]. Hence, the n-th index correspond to n.
            // So, station with id n is linked to station with id n. (to itself).
            this.partition = IntStream.range(0, stationCount).toArray();
        }

        /**
         * Connect two stations in the partition.
         *
         * @param s1 first station.
         * @param s2 second station.
         * @return the builder.
         */
        public Builder connect(Station s1, Station s2) {
            // Sets the representative of s1 to s2. that's all.
            // The partition is still "deep": a station can have as a representative a station that
            // also have a representative, etc.
            // The partition is flatted in build method.
            int representativeOfS1 = representative(s1.id());
            int representativeOfS2 = representative(s2.id());
            // We arbitrarily choose that the representative will be ALWAYS the biggest number,
            // To avoid two stations being linked to each other (i.e, a station with id 1 has for
            // representative a station B with id 2, and vice versa.
            // Hence, we get an acyclic directed graph.
            partition[Math.min(representativeOfS1, representativeOfS2)] =
                    Math.max(representativeOfS1, representativeOfS2);
            return this;
        }

        private int representative(int stationId) {
            int tempRepresentative = stationId;
            // Go "up" in the parent chain until a representative in found.
            // A representative is defined by being connected to itself.
            while (tempRepresentative != partition[tempRepresentative]) {
                tempRepresentative = partition[tempRepresentative];
            }
            return tempRepresentative;
        }

        /**
         * Returns flat partition corresponding to the deep partition of the class.
         *
         * @return station partition of the deep partition
         */
        public StationPartition build() {
            // We iterates through every station of the partition, and compute its representative.
            // Hence, we will get an array where the station at n-th index is the representative of
            // the station with id n.
            return new StationPartition(
                    IntStream.range(0, stationCount).map(this::representative).toArray());
        }
    }
}
