package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;

/** The type Station partition. */
public final class StationPartition implements StationConnectivity {
    private final int[] flatPartition;

    private StationPartition(int[] partition) {
        this.flatPartition = partition;
    }

    @Override
    public boolean connected(Station s1, Station s2) {
        boolean notInPartition =
                IntStream.of(flatPartition).noneMatch(x -> x == s1.id())
                        || IntStream.of(flatPartition).noneMatch(y -> y == s2.id());
        if (notInPartition) return s1.id() == s2.id();
        return true;
    }

    public static final class Builder {
        private int stationCount;
        private int[] deepPartition;

        public Builder(int stationCount) {
            Objects.checkIndex(0, stationCount);
            Preconditions.checkArgument(stationCount >= 0);
            this.stationCount = stationCount;
            this.deepPartition = new int[stationCount];
        }

        private int representative(int stationId) {
            return deepPartition[stationId];
        }

        public Builder connect(Station s1, Station s2) {
            int representative = representative(s1.id());
            //dont really know what to do in these two last methods
            Arrays.fill(deepPartition, representative);
            return new Builder(representative);
        }

        public StationPartition build() {
            return new StationPartition(deepPartition);
        }
    }
}
