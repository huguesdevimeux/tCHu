package ch.epfl.tchu.game;

import java.util.List;

public enum Card {
    /**
     * these colors represents the different types of cards you can encounter
     * in the game (8 different possible cars (1 for each color) and a locomotive card)
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

    public final static List<Color> ALL = List.of(Color.values());
    public final static int COUNT = ALL.size();
    public final static List<Card> CARS = List.of(Card.BLACK, Card.VIOLET, Card.BLUE,
            Card.GREEN, Card.YELLOW, Card.ORANGE, Card.RED, Card.WHITE);
    private final Color color;

    Card(Color color) {
        this.color = color;
    }

    /**
     * @param color
     * @return the color of the car
     */
    public static Card of(Color color) {
        return Card.valueOf(color.name());
    }

    /**
     * @return the color of the car or null if the card is a locomotive
     */
    public Color color() {
        if (!Card.valueOf(color.toString()).equals(Card.LOCOMOTIVE)) {
            return Color.valueOf(color.name());
        }else{
            return null;
        }
    }
}
