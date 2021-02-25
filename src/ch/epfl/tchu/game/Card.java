package ch.epfl.tchu.game;

import java.util.List;

public enum Card {
    /**
     * The following enumeration represents the different types of cards you can encounter in the
     * game (8 different possible cars (1 for each color) and a locomotive card)
     *
     * @author Luca Mouchel (324748)
     */
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
     * default card constructor
     * @param color
     */
    Card(Color color) {
        this.color = color;
    }

    /**
     * returns the color of the car
     * @param color
     * @return the color of the car
     */
    public static Card of(Color color) {
        return Card.valueOf(color.name());
    }

    /**
     * Return the color of the car or null if the card is a locomotive
     * @return the color of the car or null if the card is a locomotive
     */
    public Color color() {
        return color;
    }
}
