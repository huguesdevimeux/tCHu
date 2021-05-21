package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.ChMap;
import ch.epfl.tchu.game.Station;
import ch.epfl.tchu.game.Ticket;
import javafx.scene.Group;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ch.epfl.tchu.game.ChMap.stations;
import static ch.epfl.tchu.gui.GuiConstants.*;

/**
 * Representation of the stations on the map. Highlights the from and to stations {@link Ticket}
 * when pressing on a player's ticket.
 *
 * @author Luca Mouchel (324748)
 * @author Hugues Devimeux (327282)
 */
final class StationViewCreator {

  /* Not instantiable*/
  private StationViewCreator() {}

  /**
   * Represents the stations on the pane {@code gameMapPane} which is the map's pane. Also deals
   * with the "ticket press" : if a player clicks on one of their tickets, the stations figuring on
   * the ticket will be highlighted: the "from" station will be blue-ish and the "to" station will
   * be red IF both the from and to stations are in Switzerland. Otherwise, only the from station
   * will be highlighted in green (if it is in Switzerland). Nothing happens with stations outside
   * of switzerland as the players most likely know where Germany, Austria, France and Italy are.
   *
   * @param gameMapPane The pane that will contain each station.
   * @param ListView the listView where the tickets are located. Press on a ticket to see the
   *     stations on the map.
   */
  public static void createStationsView(Pane gameMapPane, ListView<Ticket> ListView) {
    List<Circle> stationsCircleList = new ArrayList<>();
    // we only go through the swiss stations as they are the only stations figuring on the map.
    // the for loop adds a circle for each station on the map.
    for (Station station : ChMap.swissStations()) {
      Group stationCircle = new Group();
      stationCircle.getStyleClass().add(STYLE_CLASS_STATION);
      Circle circle = (new Circle(STATION_CIRCLE_RADIUS));
      // we set the id as the name of the station but normalized (without any accents) as the CSS
      // files don't read accents correctly.
      circle.setId(ChMap.normalizedStations().get(stations().indexOf(station)));
      stationsCircleList.add(circle);
      stationCircle.getChildren().add(circle);
      stationCircle.setOnMouseClicked(
          e -> SimpleTicketsViewCreator.createPossibleTicketsView(station).show());
      gameMapPane.getChildren().add(stationCircle);
    }
    // this part manages the action where the player clicks a ticket and the stations on it will be
    // highlighted. REMINDER: stations in other countries are not highlighted in any way.
    ListView.setOnMouseClicked(
        e -> {
          Ticket chosen = ListView.getItems().get(ListView.getSelectionModel().getSelectedIndex());
          // there is only one trip on each ticket that does not involve other countries so we just
          // pick the first trip (the only one) that figures on the ticket.
          Station from = chosen.getTrips().get(0).from();
          Station to = chosen.getTrips().get(0).to();

          // we create a new list that only will contain at most 2 circles, that will be the from
          // and to stations that figure on the ticket.
          // The circle's id is the stations' name but without accents so we check the equality with
          // the from station but normalized.
          List<Circle> newList =
              stationsCircleList.stream()
                  .filter(
                      circle ->
                          circle
                                  .getId()
                                  .equals(ChMap.normalizedStations().get(stations().indexOf(from)))
                              || circle
                                  .getId()
                                  .equals(ChMap.normalizedStations().get(stations().indexOf(to))))
                  .collect(Collectors.toList());

          Circle fromStation, toStation;
          // the above list can be of size 1 if for example the ticket is "Coire - {Allemagne (6),
          // France (10), ...}" and in that case, only the station COIRE will be highlighted.
          switch (newList.size()) {
            case 1:
              fromStation = newList.get(0);
              fromStation.setStyle(SWISS_FROM_STATION_TO_COUNTRY_STYLE);
              fromStation.setRadius(INCREASED_CIRCLE_RADIUS);
              break;
            case 2:
              // we have to check which of the stations is the from and to station because the
              // circles are added in alphabetical order.
              // This means that without this verification, the ticket (Zurich - Lugano) will have
              // Lugano as the from station as it comes first alphabetically, but it is wrong.
              fromStation =
                  newList
                          .get(0)
                          .getId()
                          .equals(ChMap.normalizedStations().get(ChMap.stations().indexOf(from)))
                      ? newList.get(0)
                      : newList.get(1);
              toStation = newList.get(0).equals(fromStation) ? newList.get(1) : newList.get(0);

              // we set default colors for the from and to stations: blue-ish = from, red-ish = to.
              fromStation.setStyle(SWISS_FROM_STATION_STYLE);
              toStation.setStyle(SWISS_TO_STATION_STYLE);
              // each circle representing the from and two stations will have their radius increased
              // when a ticket is selected.
              List.of(fromStation, toStation).forEach(c -> c.setRadius(INCREASED_CIRCLE_RADIUS));
              break;
          }
        });
    //On mouse exited, the style is reset to white and the radius is reset to 6, the initial radius.
    ListView.setOnMouseExited(
        event ->
            stationsCircleList.forEach(
                circle -> {
                  circle.setStyle(SWISS_STATION_STYLE);
                  circle.setRadius(STATION_CIRCLE_RADIUS);
                }));
  }
}
