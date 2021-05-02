package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.Card;
import ch.epfl.tchu.game.Ticket;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.stream.Collectors;

/**
 * Creates Decks. Non instanciable
 *
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
class DecksViewCreator {

    public static final String STYLE_DECKS = "decks.css";
    public static final String StYLE_COLORS = "colors.css";
    public static final String ID_HAND_PANE = "hand-pane";
    public static final String CLASS_CARD = "card";
    public static final String CLASS_COUNT = "count";
    public static final String CLASS_OUTSIDE = "outside";
    public static final String CLASS_INSIDE = "inside";
    public static final String CLASS_FILLED = "filled";
    public static final String CLASS_TRAIN_IMAGE = "train-image";
    public static final String CLASS_COLOR_NEUTRAL = "NEUTRAL";
    private static final String ID_TICKETS = "tickets";

    // Not instantiable.
    private DecksViewCreator() {}

    public static Node createHandView(ObservableGameState gameState) {

        // TICKETS HAND VIEW
        ObservableList<String> ticketsNames =
                gameState.playersTicketsList().stream()
                        .map(Ticket::text)
                        .collect(Collectors.toCollection(FXCollections::observableArrayList));
        ListView<String> ticketsListView = new ListView<>(ticketsNames);
        ticketsListView.setId(ID_TICKETS);

        //
        HBox cardsHandPanel = new HBox();
        cardsHandPanel.setId(ID_HAND_PANE);

        for (Card card : Card.ALL) {
            // Count.
            Text count = new Text(gameState.playersNumberOfCards(card).getValue().toString());
            count.getStyleClass().add(CLASS_COUNT);

            // Inner icon of cards. Sorted in an exterior fashion.
            Rectangle inner1 = new Rectangle(60, 90);
            inner1.getStyleClass().add(CLASS_OUTSIDE);
            Rectangle inner2 = new Rectangle(40, 70);
            inner2.getStyleClass().addAll(CLASS_FILLED, CLASS_INSIDE);
            Rectangle inner3 = new Rectangle(40, 70);
            inner3.getStyleClass().add(CLASS_TRAIN_IMAGE);

            // Outer layout.
            StackPane cardOfHand = new StackPane();
            String color = card.color() == null ? CLASS_COLOR_NEUTRAL : card.color().name();
            cardOfHand.getStyleClass().addAll(color, CLASS_CARD);
            cardOfHand.getChildren().addAll(inner1, inner2, inner3, count);

            cardsHandPanel.getChildren().add(cardOfHand);
        }

        HBox handView = new HBox(ticketsListView, cardsHandPanel);
        handView.getStylesheets().addAll(STYLE_DECKS, StYLE_COLORS);
        return handView;
    }
}
