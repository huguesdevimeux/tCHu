package ch.epfl.tchu;

public final class Preconditions {
    /**
     * Preconditions constructor: not instantiable
     *
     * @author Luca Mouchel (324748)
     * @author Hugues Devimeux (327282)
     */
    private Preconditions() {
        // Instantiation is impossible.
    }

    /**
     * Throws IllegalArgumentException when the argument is false.
     *
     * @throws IllegalArgumentException
     * @param shouldBeTrue Expression to check.
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) throw new IllegalArgumentException();
    }
}
