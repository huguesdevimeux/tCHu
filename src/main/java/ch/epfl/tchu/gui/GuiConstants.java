package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.Color;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Constants used for the GUI part of the project.
 * Includes .css files, style classes and IDs and
 * other Constants in general.
 */
public final class GuiConstants {
    //style sheets constants
    public static final String MAP_CSS = "map.css";
    public static final String COLORS_CSS = "colors.css";
    public static final String DECKS_CSS = "decks.css";
    public static final String INFO_CSS = "info.css";
    public static final String CHOOSER_CSS = "chooser.css";

    //style class constants
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

    public static final List<String> STYLE_CLASSES_COLOR =
            Color.ALL.stream().map(Objects::toString).collect(Collectors.toList());
    //ID constants
    public static final String ID_TICKETS = "tickets";
    public static final String ID_CARD_PANE = "card-pane";
    public static final String ID_HAND_PANE = "hand-pane";
    public static final String ID_PLAYER_STATS = "player-stats";
    public static final String ID_GAME_INFO = "game-info";

    //Neutral color matching Locomotive color
    public static final String NEUTRAL = "NEUTRAL";
    //total number of visible messages in the info view section
    public static final int VISIBLE_INFOS = 5;
    //minimum number of cards displayed
    public static final int MIN_CARDS_NUMBER_DISPLAYED = 1;
}
