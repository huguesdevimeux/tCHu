package ch.epfl.tchu;

/**
 * Preconditions constructor: not instantiable
 *
 * @author Luca Mouchel (324748)
 * @author Hugues Devimeux (327282)
 */
public final class Preconditions {

    private Preconditions() {
        // Instantiation is impossible.
    }

    /**
     * Throws IllegalArgumentException when the argument is false.
     *
     * @param shouldBeTrue Expression to check.
     * @throws IllegalArgumentException if <code>shouldBeTrue</code> is false
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) throw new IllegalArgumentException();
    }
}
