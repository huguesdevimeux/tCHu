package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.Color;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Constants used for the GUI part of the project. Includes .css files, style classes and IDs and
 * other Constants in general.
 */

public final class GuiConstants {
    //style sheets constants
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
    public static final String STYLE_CLASS_STATION = "station";

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
  /* The radius of the stations circles on the map.*/
  public static final int STATION_CIRCLE_RADIUS = 6;
  /* The radius of the stations circles that is increased when pressing on a ticket.*/
  public static final int INCREASED_CIRCLE_RADIUS = 9;
  /* The style we give to the from station of the selected ticket. */
  public static final String SWISS_FROM_STATION_STYLE = "-fx-fill: #6f6ff3";
  /* The style we give to the to station of the selected ticket. */
  public static final String SWISS_TO_STATION_STYLE = "-fx-fill: #f15454";
  /* The style we give to the station "from" of the selected tickets, but that goes to another country */
    public static final String SWISS_FROM_STATION_TO_COUNTRY_STYLE = "-fx-fill: #04621f";
  /* The initial and default style of the stations */
  public static final String SWISS_STATION_STYLE = "-fx-fill: white";
  /* The custom width of the scene that is generated that displays the tickets where the stations appear.*/
  public static final int SCENE_WIDTH_FOR_POSSIBLE_TICKETS = 220;
  /* The custom height of the scene that is generated that displays the tickets where the stations appear.*/
  public static final int SCENE_HEIGHT_FOR_POSSIBLE_TICKETS = 130;
  /* The description of the possible tickets where a station is used */
  public static final String STATION_FIGURES_IN_TICKETS =
          "Cette station ce trouve sur le%s ticket%<s :";
  /* The description saying the station doesn't figure on any ticket*/
  public static final String STATION_IS_NOT_ON_TICKETS =
          "Aucun ticket n'utilise cette station \ncomme station de départ ou d'arrivée.";

  /* The title of the holy tCHu window. */
  public static final String TCHU_TITLE = "tCHu \u2014 %s";
  /* The icon of the main window */
    public static final String TCHU_ICON = "TCHU.png";
}
