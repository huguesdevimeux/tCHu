// Rigel stage 1

package ch.epfl.tchu;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PreconditionsTest {
    @Test
    void checkArgumentSucceedsForTrue() {
        Preconditions.checkArgument(true);
    }

    @Test
    void checkArgumentFailsForFalse() {
        assertThrows(IllegalArgumentException.class, () -> Preconditions.checkArgument(false));
    }
}
