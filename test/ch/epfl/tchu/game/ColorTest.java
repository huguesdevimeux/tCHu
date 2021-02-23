package ch.epfl.tchu.game;

import org.junit.jupiter.api.Test;

import java.util.List;

import static ch.epfl.tchu.game.Color.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ColorTest {
    @Test
    void colorValuesAreDefinedInTheRightOrder() {
        var expectedValues = new Color[]{
                BLACK, VIOLET, BLUE, GREEN, YELLOW, ORANGE, RED, WHITE
        };
        assertArrayEquals(expectedValues, Color.values());
    }

    @Test
    void colorAllIsDefinedCorrectly() {
        assertEquals(List.of(Color.values()), ALL);
    }

    @Test
    void colorCountIsDefinedCorrectly() {
        assertEquals(8, COUNT);
    }
}