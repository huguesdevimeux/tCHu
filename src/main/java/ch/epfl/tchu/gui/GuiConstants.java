package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.Color;
import javafx.util.Duration;

/**
 * GuiConstants used for the GUI part of the project. Includes .css files, style classes and IDs and
 * other GuiConstants in general.
 */
public final class GuiConstants {
    // Geometric values constants for mapViewCreator
    public static final int RECTANGLE_WIDTH = 36;
    public static final int RECTANGLE_HEIGHT = 12;
    public static final int CIRCLE1_CENTER_X = 12;
    public static final int CIRCLE2_CENTER_X = 24;
    public static final int CIRCLE_CENTER_Y = 6;
    public static final int ROUTE_CIRCLE_RADIUS = 3;
    // Geometric constants for DecksViewCreator
    public static final int INNER_RECT1_WIDTH = 60;
    public static final int INNER_RECT1_HEIGHT = 90;
    public static final int INNER_RECT_WIDTH = 40;
    public static final int INNER_RECT_HEIGHT = 70;
    public static final int BUTTON_GAUGE_WIDTH = 50;
    public static final int BUTTON_GAUGE_HEIGHT = 5;

    // style sheets constants
    public static final String MAP_CSS = "map.css";
    public static final String COLORS_CSS = "colors.css";
    public static final String DECKS_CSS = "decks.css";
    public static final String INFO_CSS = "info.css";
    public static final String CHOOSER_CSS = "chooser.css";

    // style class constants
    public static final String STYLE_CLASS_ROUTE = "route";
    public static final String STYLE_CLASS_TRACK = "track";
    public static final String STYLE_CLASS_FILLED = "filled";
    public static final String STYLE_CLASS_CAR = "car";
    public static final String STYLE_CLASS_BACKGROUND = "background";
    public static final String STYLE_CLASS_FOREGROUND = "foreground";
    public static final String STYLE_CLASS_GAUGED = "gauged";
    public static final String STYLE_CLASS_CARD = "card";
    public static final String STYLE_CLASS_COUNT = "count";
    public static final String STYLE_CLASS_INSIDE = "inside";
    public static final String STYLE_CLASS_OUTSIDE = "outside";
    public static final String STYLE_CLASS_TRAIN_IMAGE = "train-image";

    /* "neutral" color. */
    private static final String STYLE_CLASS_COLOR_NEUTRAL = "NEUTRAL";
	public static final int OFFSET_X_INFOS = 30;
	public static final Duration DURATION_INFO_ANIMATION = Duration.millis(1000);
    public static final Duration CYCLE_TIME_FADE = Duration.millis(100);

    // ID constants
    public static final String ID_TICKETS = "tickets";
    public static final String ID_CARD_PANE = "card-pane";
    public static final String ID_HAND_PANE = "hand-pane";
    public static final String ID_PLAYER_STATS = "player-stats";
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

	/* Offset Y of the hand's cards animations */
	public static final int OFFSET_Y_HAND_CARDS = -40;
	/* Offset X of the hand's cards animations */
	public static final int OFFSET_X_HAND_CARDS = 0;
	/* Duration of the translation animation of teh hand's cards. */
	public static final int DURATION_ANIMATION_HAND_CARDS = 400;
	/* Offset X of the deck's cards animations */
	public static final int OFFSET_X_DECK_CARDS = -20;
	/* Offset Y of the deck's cards animations */
	public static final int OFFSET_Y_CARDS_DECK = 0;
	/* Duration of the translation animation of deck's cards. */
	public static final int DURATION_ANIMATION_DECK_CARDS = 400;

	/*Duration of the indication animation. In Ms;*/
	public static final int DURATION_ANIMATION = 200;
	/*Added x value during the indication animation. */
	public static final float SCALING_X_INDICATION = 0.5f;
	/*Added y value during the indication animation. */
	public static final float SCALING_Y_INDICATION = 0.5f;
	/*Number of bounces during the indication animation. */
	public static final int NUMBER_BOUNCES_INDICATION = 4;

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
	public static final double FROM_FADE = 0.5;
}
