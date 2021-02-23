package ch.epfl.tchu;

public final class Preconditions {
    private Preconditions() {
        // Instantiation is impossible.
    }

    /*
     * throws IllegalArgumentException if parameter is false
     * @param shouldBeTrue Expression to check.
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) throw new IllegalArgumentException();
    }
}
