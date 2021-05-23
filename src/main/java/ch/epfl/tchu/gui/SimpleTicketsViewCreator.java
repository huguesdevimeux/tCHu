package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.ChMap;
import ch.epfl.tchu.game.Station;
import ch.epfl.tchu.game.Ticket;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

import static ch.epfl.tchu.gui.GuiConstants.*;
import static ch.epfl.tchu.gui.StringsFr.STATION_FIGURES_IN_TICKETS;
import static ch.epfl.tchu.gui.StringsFr.STATION_IS_NOT_ON_TICKETS;

/**
 * Represents a small stage that popups when clicking on a station. It will open a stage where the
 * player will see all the tickets in the game that contain the station as a from or a to station.
 * This allows for a "strategic" aspect to the game. The player can try to guess which tickets the
 * other player has and attempt to block the other player.
 *
 * @author Luca Mouchel (324748)
 * @author Hugues Devimeux (327282)
 */
final class SimpleTicketsViewCreator {
    private static final ListView<String> possibleTicketsView = new ListView<>();
    private static TextFlow description;
    private static Scene scene;

    /** Not instantiable. */
    private SimpleTicketsViewCreator() {}

    /**
     * Creates a stage that will display the tickets that contain the given station.
     *
     * @param station the station to evaluate in which tickets it appears.
     * @return a stage (like a popup) that displays the tickets that contain the station.
     */
    public static Stage createPossibleTicketsView(Station station) {
        Stage stage = new Stage();
        List<String> possibleTicketsForGivenStation =
                ChMap.tickets().stream()
                        .filter(
                                ticket ->
                                        ticket
                                                .getTrips()
                                                .get(0)
                                                .from()
                                                .equals(
                                                        station) // the clickable stations are in switzerland so there is
                                                // only one trip
                                                || ticket.getTrips().get(0).to().equals(station))
                        .map(Ticket::toString)
                        .collect(Collectors.toList());

        possibleTicketsView.setItems(FXCollections.observableList(possibleTicketsForGivenStation));
        // if the list is empty it means that the station is never a "from" or a "to" but just an
        // intermediary station.
        if (!possibleTicketsForGivenStation.isEmpty()) {
            description =
                    new TextFlow(
                            new Text(
                                    String.format(
                                            StringsFr.STATION_FIGURES_IN_TICKETS,
                                            StringsFr.plural(possibleTicketsForGivenStation.size()))));
            scene =
                    new Scene(
                            new VBox(description, possibleTicketsView),
                            SCENE_WIDTH_FOR_POSSIBLE_TICKETS,
                            SCENE_HEIGHT_FOR_POSSIBLE_TICKETS);
        } else {
            description = new TextFlow(new Text(StringsFr.STATION_IS_NOT_ON_TICKETS));
            scene = new Scene(description);
        }
        //adding a little ticket icon for fun.
        stage.getIcons().add(new Image(TICKET_ICON));
        stage.setScene(scene);
        return stage;
    }
}