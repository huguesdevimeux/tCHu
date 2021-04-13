package ch.epfl.tchu.game;

import java.util.List;

/**
 * The following enumeration represents the different types of cards you can encounter in the game
 * (8 different possible cars (1 for each color) and a locomotive card)
 *
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public enum Card {
    BLACK(Color.BLACK),
    VIOLET(Color.VIOLET),
    BLUE(Color.BLUE),
    GREEN(Color.GREEN),
    YELLOW(Color.YELLOW),
    ORANGE(Color.ORANGE),
    RED(Color.RED),
    WHITE(Color.WHITE),
    LOCOMOTIVE(null);

    /** same function as ALL attribute in Color.java, stocks every element of the enum in a List */
    public static final List<Card> ALL = List.of(Card.values());

    /** attribute that counts the total amount of elements in ALL */
    public static final int COUNT = ALL.size();

    /** List of all the possible cars (all the elements of the enum apart from the locomotive) */
    public static final List<Card> CARS =
            List.of(
                    Card.BLACK,
                    Card.VIOLET,
                    Card.BLUE,
                    Card.GREEN,
                    Card.YELLOW,
                    Card.ORANGE,
                    Card.RED,
                    Card.WHITE);

    private final Color color;

    /**
     * Default card constructor
     *
     * @param color color
     */
    Card(Color color) {
        this.color = color;
    }

    /**
     * Gets the color of the car
     *
     * @param color color of the car
     * @return the card of the given color
     */
    public static Card of(Color color) {
        return Card.valueOf(color.name());
    }

    /**
     * Returns the color of the car or null if the card is a locomotive
     *
     * @return the color of the car or null if the card is a locomotive.
     */
    public Color color() {
        return color;
    }
}
