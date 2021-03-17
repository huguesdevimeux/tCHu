package ch.epfl.tchu.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StationPartitionTest {

    private Station s2;
    private Station s1;
    private Station s3;
    private Station s4;
    private Station s5;
    private List<Station> allStations;

    @BeforeEach
    void setUp() {
        this.s1 = new Station(0, "Station1");
        this.s2 = new Station(1, "Station2");
        this.s3 = new Station(2, "Station3");
        this.s4 = new Station(3, "Station4");
        this.s5 = new Station(3, "Station5");

        this.allStations = List.of(s1, s2, s3, s4, s5);
    }

    @Test
    void connectedWithStationsOfTheSameSubset() {
        StationPartition partition = new StationPartition.Builder(2).connect(s1, s2).build();
        assertTrue(partition.connected(s1, s2));
        assertTrue(partition.connected(s1, s1));

        StationPartition.Builder builder = new StationPartition.Builder(5);
        builder.connect(s1, s2);
        builder.connect(s1, s3);
        assertTrue(builder.build().connected(s1, s2));
        assertTrue(builder.build().connected(s2, s1));
        assertTrue(builder.build().connected(s3, s1));
        assertTrue(builder.build().connected(s3, s2));

        // Tries to add the same station to the builder
        builder.connect(s3, s1);
        assertTrue(builder.build().connected(s1, s3));

        // Add a new disjoint set of two station and connect them to the bug one.
        builder.connect(s4, s5);
        StationPartition partition2 = builder.build();
        for (Station s : allStations) {
            for (Station sPrime : allStations) {
                assertTrue(partition2.connected(s, sPrime));
            }
        }

    }

    @Test
    void connectedWithDifferentSubset() {
        StationPartition.Builder builder = new StationPartition.Builder(4);
        builder.connect(s1, s2);
        StationPartition partition = builder.build();
        assertFalse(partition.connected(s1, s3));
    }

    @Test
    void builderFails() {
        assertThrows(IllegalArgumentException.class, () -> new StationPartition.Builder(-1));
        assertDoesNotThrow(() -> new StationPartition.Builder(0));
    }
}