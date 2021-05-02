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
    public static final String ID_CARD_PANE = "card-pane";
    private static final String ID_TICKETS = "tickets";

    // Not instantiable.
    private DecksViewCreator() {}

    public static Node createCardsView(
            ObservableGameState observableGameState,
            ActionHandlers.DrawTicketsHandler drawTicketsHandler,
            ActionHandlers.DrawCardHandler drawCardHandler) {

        // Tickets pile.

        VBox cardsView = new VBox();
        cardsView.setId(ID_CARD_PANE);
        cardsView.getStylesheets().addAll(STYLE_DECKS, STYLE_COLORS);
        return cardsView;
    }

    public static Node createHandView(ObservableGameState gameState) {

        // TICKETS HAND VIEW
        ListView<Ticket> ticketsListView = new ListView<>();
        ticketsListView.setItems(gameState.playersTicketsList());
        ticketsListView.setId(ID_TICKETS);

        //
        HBox cardsHandPanel = new HBox();
        cardsHandPanel.setId(ID_HAND_PANE);

        for (Card card : Card.ALL) {
            // Count.
            Text count = new Text(gameState.playersNumberOfCards(card).getValue().toString());
            gameState
                    .playersNumberOfCards(card)
                    .addListener((observableValue, oV, nV) -> count.setText(nV.toString()));
            count.textProperty().bind(gameState.playersNumberOfCards(card).asString());
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
