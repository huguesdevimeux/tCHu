package ch.epfl.tchu.game;

import java.util.List;

/**
 * Representation of all the cars' colors. immutable enumeration
 *
 * @author Luca Mouchel (324748)
 */
public enum Color {
    BLACK,
    VIOLET,
    BLUE,
    GREEN,
    YELLOW,
    ORANGE,
    RED,
    WHITE;

    /** adding all the colors in a List in the order they appear in the enumeration */
    public static final List<Color> ALL = List.of(Color.values());

    /** static attribute that counts the total number of colors */
    public static final int COUNT = ALL.size();
}
