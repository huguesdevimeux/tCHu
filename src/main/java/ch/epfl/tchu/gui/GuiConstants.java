package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.Color;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Constants used for the GUI part of the project. Includes .css files, style classes and IDs and
 * other Constants in general.
 */
public class GuiConstants {
    // style sheets constants
    public static final String STYLE_SHEET_MAP = "map.css";
    public static final String STYLE_SHEET_COLORS = "colors.css";
    public static final String STYLE_SHEET_DECKS = "decks.css";
    public static final String STYLE_SHEET_INFO = "info.css";

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
    public static final String STYLE_CLASS_CHOOSER = "chooser.css";

    /* "neutral" color. */
    public static final String STYLE_CLASS_COLOR_NEUTRAL = "NEUTRAL";
    /* All the colors used in css. Does not include neutral, as not a proper color. */
    public static final List<String> STYLE_CLASSES_COLOR =
            Color.ALL.stream().map(Objects::toString).collect(Collectors.toList());
    // ID constants
    public static final String ID_TICKETS = "tickets";
    public static final String ID_CARD_PANE = "card-pane";
    public static final String ID_HAND_PANE = "hand-pane";
    public static final String ID_PLAYER_STATS = "player-stats";
    public static final String ID_GAME_INFO = "game-info";

    /* The default port used for conne	ction */
    public static final int DEFAULT_PORT = 5108;
    /* The default IP used for connection */
    public static final String DEFAULT_IP = "localhost";
    /* Default names for the players.*/
    public static final List<String> DEFAULT_NAMES = List.of("Ada", "Charles");
    /* Number of visible information.*/
    public static final int VISIBLE_INFOS = 5;
    /* The minimum amount of one card before the number if displayed on the gui. */
    public static final int MIN_CARDS_NUMBER_DISPLAYED = 1;

    /* How many hands of cards the player has to choose when claiming a card */
    public static final int MINIMUM_CHOICES_CLAIM_CARDS = 1;
    /* How many hands of cards the player has to choose when choosing additional cards. */
    public static final int MINIMUM_CHOICES_ADDITIONAL_CARDS = 1;
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
}
