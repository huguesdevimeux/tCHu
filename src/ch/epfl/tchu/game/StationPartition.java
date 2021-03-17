package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.stream.IntStream;

/**
 * Represents a flat partition.
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
        boolean notInPartition =
                IntStream.of(flatPartition).noneMatch(x -> x == s1.id())
                        || IntStream.of(flatPartition).noneMatch(y -> y == s2.id());
        if (notInPartition) return s1.id() == s2.id();
        return true;
    }

    /** Represents the deep partition. */
    public static final class Builder {
        private int stationCount;
        private int[] deepPartition;

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
            this.deepPartition = new int[stationCount];
        }

        /*elects a representative*/
        private int representative(int stationId) {
            int temp = deepPartition[stationId];
            while (stationId != temp) {
                stationId = temp;
                temp = deepPartition[stationId];
            }
            return deepPartition[stationId];
        }

        /**
         * Joins sets containing the stations and elects one as the representative of the partition.
         *
         * @param s1 first station.
         * @param s2 second station.
         * @return the builder.
         */
        public Builder connect(Station s1, Station s2) {
            deepPartition[s2.id()] = representative(s1.id());
            return this;
        }

        /**
         * Returns flat partition corresponding to the deep partition of the class.
         *
         * @return station partition of the deep partition
         */
        public StationPartition build() {
            int[] flatPartition = deepPartition;
            for (int i = 0; i < deepPartition.length; i++) {
                if (flatPartition[i] != representative(i)) {
                    flatPartition[i] = representative(i);
                }
            }
            return new StationPartition(flatPartition);
        }
    }

    public static void main(String[] args) {
        Builder a = new Builder(4);
        System.out.println(a.representative(2));
    }
}
