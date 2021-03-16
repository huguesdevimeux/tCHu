package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.Objects;
import java.util.stream.IntStream;

/** The type Station partition. */
public final class StationPartition implements StationConnectivity {
    private final int[] flatPartition;

    private StationPartition(int[] flatPartition) {
        this.flatPartition = flatPartition;
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
            deepPartition[s2.id()] = representative(s1.id());
            return this;
        }

        public StationPartition build() {
            return new StationPartition(deepPartition);
        }
    }
}
