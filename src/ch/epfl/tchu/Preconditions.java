package ch.epfl.tchu;

public final class Preconditions {
    /**
     * Preconditions constructor: not instantiable
     *
     * @author Luca Mouchel (324748)
     */
    private Preconditions() {
        // Instantiation is impossible.
    }

    /**
     * throws exception if argument passed is parameter is false
     *
     * @throws IllegalArgumentException parameter is false
     * @param shouldBeTrue Expression to check.
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) throw new IllegalArgumentException();
    }
}
