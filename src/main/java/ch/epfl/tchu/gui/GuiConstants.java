package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.Color;

/**
 * GuiConstants used for the GUI part of the project. Includes .css files, style classes and IDs and
 * other GuiConstants in general.
 */
public final class GuiConstants {
    private GuiConstants(){}
    // Geometric values constants for mapViewCreator
    //Width used for the rectangles in the game
    public static final int RECTANGLE_WIDTH = 36;
    //Height used for the rectangles in the game
    public static final int RECTANGLE_HEIGHT = 12;
    //the first circle in map view creator's center X
    public static final int CIRCLE1_CENTER_X = 12;
    //the second circle in map view creator's center X
    public static final int CIRCLE2_CENTER_X = 24;
    //The Y center for both circles i nmap views creator
    public static final int CIRCLE_CENTER_Y = 6;
    //The radius of both circles
    public static final int ROUTE_CIRCLE_RADIUS = 3;


    // Geometric constants for DecksViewCreator
    //The first rectangles width
    public static final int INNER_RECT1_WIDTH = 60;
    //The first rectangles height
    public static final int INNER_RECT1_HEIGHT = 90;
    //The rectangle's width for the second and third inner rectangles
    //in decks view creator
    public static final int INNER_RECT_WIDTH = 40;
    //The rectangle's height for the second and third inner rectangles
    //in decks view creator
    public static final int INNER_RECT_HEIGHT = 70;
    //the width of the gauge in the button
    public static final int BUTTON_GAUGE_WIDTH = 50;
    //The height of the gauge in the button
    public static final int BUTTON_GAUGE_HEIGHT = 5;

    // style sheets constants
    //Map style sheet
    public static final String MAP_CSS = "map.css";
    //Colors style sheet
    public static final String COLORS_CSS = "colors.css";
    //Decks style sheet
    public static final String DECKS_CSS = "decks.css";
    //Info style sheet
    public static final String INFO_CSS = "info.css";
    //Chooser style sheet
    public static final String CHOOSER_CSS = "chooser.css";

    // style class constants
    //Route style class
    public static final String STYLE_CLASS_ROUTE = "route";
    //track style class
    public static final String STYLE_CLASS_TRACK = "track";
    //filled style class
    public static final String STYLE_CLASS_FILLED = "filled";
    //car style class
    public static final String STYLE_CLASS_CAR = "car";
    //background style class
    public static final String STYLE_CLASS_BACKGROUND = "background";
    //foreground style class
    public static final String STYLE_CLASS_FOREGROUND = "foreground";
    //gauged style class
    public static final String STYLE_CLASS_GAUGED = "gauged";
    //card style class
    public static final String STYLE_CLASS_CARD = "card";
    //count style class
    public static final String STYLE_CLASS_COUNT = "count";
    //inside style class
    public static final String STYLE_CLASS_INSIDE = "inside";
    //outside style class
    public static final String STYLE_CLASS_OUTSIDE = "outside";
    //train image style class
    public static final String STYLE_CLASS_TRAIN_IMAGE = "train-image";
    // "neutral" color
    private static final String STYLE_CLASS_COLOR_NEUTRAL = "NEUTRAL";
    /**
     * Converts a Color given as parameter to a the css class name used in tCHu. Color can be null,
     * and in this case the color will be NEUTRAL.
     *
     * @param color The color to convert
     * @return The CSS class name.
     */
    public static String convertColorToCssColor(Color color) {
        return (color == null) ? STYLE_CLASS_COLOR_NEUTRAL : color.name();
    }

    // ID constants
    //tickets id
    public static final String ID_TICKETS = "tickets";
    //card pane id
    public static final String ID_CARD_PANE = "card-pane";
    //hand pane id
    public static final String ID_HAND_PANE = "hand-pane";
    //player stats id
    public static final String ID_PLAYER_STATS = "player-stats";
    //game info id
    public static final String ID_GAME_INFO = "game-info";

    /* Constant for the route id and the rectangles composing the route.*/
    public static final String ROUTE_RECT_ID = "%s_%s";
    /* Number of visible information.*/
    public static final int VISIBLE_INFOS = 5;
    /* The minimum amount of one card before the number if displayed on the gui. */
    public static final int MIN_CARDS_NUMBER_DISPLAYED = 1;
    /* The minimum amount of cards needed for the card to be visible or to choose from when choosing additional cards. */
    public static final int MIN_CARDS_REQUIRED = 0;
    /* How many hands of cards the player has to choose when claiming a card */
    public static final int MINIMUM_CHOICES_CLAIM_CARDS = 1;
    /* The title of the holy tCHu window. */
    public static final String TCHU_TITLE = "tCHu \u2014 %s";
}
