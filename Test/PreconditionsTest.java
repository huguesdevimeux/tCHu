import ch.epfl.tchu.Preconditions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class PreconditionsTest {

    @Test
    void checkArgumentSucceedsWhenTrue() {
        Preconditions.checkArgument(true);
    }

    @Test
    void checkArgumentFailsWhenFalse() {
        assertThrows(IllegalArgumentException.class, () -> {
            Preconditions.checkArgument(false);
        });
    }
}
