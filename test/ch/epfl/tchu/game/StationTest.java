package ch.epfl.tchu.game;

import ch.epfl.test.TestRandomizer;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StationTest {
    @Test
    void stationConstructorFailsForNegativeId() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Station(-1, "Lausanne");
        });
    }

    @Test
    void idAccessorWorks() {
        var rng = TestRandomizer.newRandom();
        for (int i = 0; i < TestRandomizer.RANDOM_ITERATIONS; i++) {
            var id = rng.nextInt(Integer.MAX_VALUE);
            var station = new Station(id, "Lausanne");
            assertEquals(id, station.id());
        }
    }

    private static final String alphabet = "abcdefghijklmnopqrstuvwxyz";
    private static String randomName(Random rng, int length) {
        var sb = new StringBuilder();
        for (int i = 0; i < length; i++)
            sb.append(alphabet.charAt(rng.nextInt(alphabet.length())));
        return sb.toString();
    }

    @Test
    void nameAccessorWorks() {
        var rng = TestRandomizer.newRandom();
        for (int i = 0; i < TestRandomizer.RANDOM_ITERATIONS; i++) {
            var name = randomName(rng, 1 + rng.nextInt(10));
            var station = new Station(1, name);
            assertEquals(name, station.name());
        }
    }

    @Test
    void stationToStringReturnsName() {
        var rng = TestRandomizer.newRandom();
        for (int i = 0; i < TestRandomizer.RANDOM_ITERATIONS; i++) {
            var name = randomName(rng, 1 + rng.nextInt(10));
            var station = new Station(1, name);
            assertEquals(name, station.toString());
        }
    }
}