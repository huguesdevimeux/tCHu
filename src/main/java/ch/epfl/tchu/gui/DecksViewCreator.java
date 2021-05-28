package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.Card;
import ch.epfl.tchu.game.GameConstants;
import ch.epfl.tchu.game.Ticket;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import static ch.epfl.tchu.gui.GuiConstants.*;

/**
 * Creates Decks. Non instantiable
 *
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
final class DecksViewCreator {

    // Not instantiable.
    private DecksViewCreator() {
    }

    /**
     * Creates the cards view (right side of the game) with two buttons
     * and the five face up cards.
     *
     * @param observableGameState the observable game state
     * @param drawTicketsHandler  the handler that manages the action of drawing tickets
     * @param drawCardHandler     the handles that manages the action of drawing cards
     * @return the node responsible for the cards view in the game
     */
    public static Node createCardsView(
            ObservableGameState observableGameState,
            ObjectProperty<ActionHandlers.DrawTicketsHandler> drawTicketsHandler,
            ObjectProperty<ActionHandlers.DrawCardHandler> drawCardHandler) {

        // Tickets pile.
        // Button group
        Button ticketsPile =
                itemPileWithGauge(StringsFr.TICKETS, observableGameState.percentageTickets());
        ticketsPile.disableProperty().bind(drawTicketsHandler.isNull());
        ticketsPile.setOnMouseClicked(event -> drawTicketsHandler.get().onDrawTickets());
        VBox cardsView = new VBox(ticketsPile);
        cardsView.setId(ID_CARD_PANE);
        cardsView.getStylesheets().addAll(DECKS_CSS, COLORS_CSS);

        for (int slot : GameConstants.FACE_UP_CARD_SLOTS) {
            StackPane displayedCard = individualCard();
            cardsView.getChildren().add(displayedCard);
            displayedCard.disableProperty().bind(drawCardHandler.isNull());
            displayedCard.setOnMouseClicked(event -> drawCardHandler.get().onDrawCard(slot));

            observableGameState
                    .faceUpCard(slot)
                    .addListener(
                            (observable, oldCard, newCard) -> {
                                // oldCard is null during the initialization.
                                if (oldCard != null) {
                                    displayedCard
                                            .getStyleClass()
                                            .remove(convertColorToCssColor(oldCard.color()));
                                }
                                displayedCard
                                        .getStyleClass()
                                        .add(convertColorToCssColor(newCard.color()));
                            });

        }
        Button cardsPile =
                itemPileWithGauge(StringsFr.CARDS, observableGameState.percentageCards());
        cardsPile.disableProperty().bind(drawCardHandler.isNull());
        cardsPile.setOnMouseClicked(e -> drawCardHandler.get().onDrawCard(GameConstants.DECK_SLOT));
        cardsView.getChildren().add(cardsPile);
        return cardsView;
    }

    /**
     * Creates the hand view section of the game.
     * Contains the player's cards and tickets.
     *
     * @param observableGameState the observable game state
     * @return the node responsible for the hand view of the game
     */
    public static Node createHandView(ObservableGameState observableGameState) {

        // TICKETS HAND VIEW
        ListView<Ticket> ticketsListView = new ListView<>();
        ticketsListView.setItems(observableGameState.playersTicketsList());
        ticketsListView.setId(ID_TICKETS);

        HBox cardsHandPanel = new HBox();
        cardsHandPanel.setId(ID_HAND_PANE);

        for (Card card : Card.ALL) {
            StackPane cardOfHand = individualCard();
            cardOfHand.getStyleClass().add(convertColorToCssColor(card.color()));
            cardOfHand
                    .visibleProperty()
                    .bind(
                            observableGameState
                                    .playerNumberOfCards(card)
                                    .greaterThan(MIN_CARDS_REQUIRED));

            // Count.
            Text count = new Text();
            count.textProperty()
                    .bind(Bindings.convert(observableGameState.playerNumberOfCards(card)));
            count.visibleProperty()
                    .bind(Bindings.greaterThan(
                            observableGameState.playerNumberOfCards(card),
                            MIN_CARDS_NUMBER_DISPLAYED));

            count.getStyleClass().add(STYLE_CLASS_COUNT);
            cardOfHand.getChildren().add(count);
            cardsHandPanel.getChildren().add(cardOfHand);
        }

        HBox handView = new HBox(ticketsListView, cardsHandPanel);
        handView.getStylesheets().addAll(DECKS_CSS, COLORS_CSS);
        return handView;
    }

    /**
     * Returns a stack pane of different rectangles that together represent a card.
     * @return an individual card in the form of a stakc pane
     */
    private static StackPane individualCard() {
        // Inner icon of cards. Sorted in an exterior fashion.
        Rectangle inner1 = new Rectangle(INNER_RECT1_WIDTH, INNER_RECT1_HEIGHT);
        inner1.getStyleClass().add(STYLE_CLASS_OUTSIDE);

        Rectangle inner2 = new Rectangle(INNER_RECT_WIDTH, INNER_RECT_HEIGHT);
        inner2.getStyleClass().addAll(STYLE_CLASS_FILLED, STYLE_CLASS_INSIDE);

        Rectangle inner3 = new Rectangle(INNER_RECT_WIDTH, INNER_RECT_HEIGHT);
        inner3.getStyleClass().add(STYLE_CLASS_TRAIN_IMAGE);

        // Outer layout.
        StackPane cardOfHand = new StackPane(inner1, inner2, inner3);
        cardOfHand.getStyleClass().add(STYLE_CLASS_CARD); // we add the style class here
        // because this method will be called for the handsView and the cardsView.
        return cardOfHand;
    }

    /**
     * Creates a button with a gauge.
     *
     * @param itemName the visible text on the button
     * @param percentageProperty the percentage to represent on the gauge (tickets or cards remaining)
     * @return a new button with a gauge
     */
    private static Button itemPileWithGauge(
            String itemName, ReadOnlyIntegerProperty percentageProperty) {
        Button itemPile = new Button(itemName);
        itemPile.getStyleClass().add(STYLE_CLASS_GAUGED);

        Rectangle backgroundButtonGraphic = new Rectangle(BUTTON_GAUGE_WIDTH, BUTTON_GAUGE_HEIGHT);
        backgroundButtonGraphic.getStyleClass().add(STYLE_CLASS_BACKGROUND);
        Rectangle foregroundButtonGraphic = new Rectangle(BUTTON_GAUGE_WIDTH, BUTTON_GAUGE_HEIGHT);
        foregroundButtonGraphic.getStyleClass().add(STYLE_CLASS_FOREGROUND);
        foregroundButtonGraphic.widthProperty().bind(percentageProperty.multiply(BUTTON_GAUGE_WIDTH).divide(100));

        itemPile.setGraphic(new Group(backgroundButtonGraphic, foregroundButtonGraphic));
        return itemPile;
    }
}
