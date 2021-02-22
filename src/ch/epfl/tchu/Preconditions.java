package ch.epfl.tchu;

public final class Preconditions {
    private Preconditions() {
        //no possible instantiations
    }

    /*
     * throws IllegalArgumentException parameter is false
     * @param shouldBeTrue Expression to check.
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) throw new IllegalArgumentException();
    }
}
