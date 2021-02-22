package ch.epfl.tchu.utils;

public class Preconditions {
    private Preconditions() {
        /* Prevents manual instantiation. */
    }

    /**
     * Raises IllegalArgumentException if the expression passed as parameter is false.
     * @param shouldBeTrue Expression to check.
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }
}
