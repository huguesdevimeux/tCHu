package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.ChMap;
import ch.epfl.tchu.game.Station;
import ch.epfl.tchu.game.Ticket;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

final class SimpleTicketsViewCreator {

  public static Stage createPossibleTicketsView(Station station) {
    ScrollPane scrollPane = new ScrollPane();
    Stage stage = new Stage();
    List<String> possibleTicketsForGivenStation =
        ChMap.tickets().stream()
            .map(Ticket::text)
            .filter(text -> text.contains(station.name()))
            .collect(Collectors.toList());

    VBox renderList = new VBox();
    if (!possibleTicketsForGivenStation.isEmpty()) {
      for (String s : possibleTicketsForGivenStation) {
        renderList.getChildren().add(new Text(s + "\n"));
      }
      scrollPane.setContent(renderList);
      stage.setScene(new Scene(scrollPane, 200, 120));
    } else {
      renderList.getChildren().add(new Text("Aucun ticket n'utilise cette station " +
              "\ncomme station de départ ou d'arrivée."));
      stage.setScene(new Scene(renderList, 210, 50));
    }
    return stage;
  }
}
