package ch.epfl.tchu.gui;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static ch.epfl.tchu.gui.GuiConstants.CHOOSER_CSS;
import static ch.epfl.tchu.gui.GuiConstants.VISIBLE_INFOS;
import static javafx.application.Platform.isFxApplicationThread;

/**
 * Represents graphical interface of a tCHu's player.
 *
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public class GraphicalPlayer {

    private final ObservableGameState observableGameState;
    private final PlayerId correspondingPlayer;
    private final Map<PlayerId, String> playerNames;
    private final SimpleListProperty<Text> infoProperty;
    private final SimpleObjectProperty<ActionHandlers.DrawTicketsHandler> drawTicketsHandler;
    private final SimpleObjectProperty<ActionHandlers.DrawCardHandler> drawCardHandler;
    private final SimpleObjectProperty<ActionHandlers.ClaimRouteHandler> takeRouteHandler;
    private Stage root;

    /**
     * Constructor that builds graphical interface designed for the player.
     *
     * @param correspondingPlayer The player associated.
     * @param playerNames The players playing.
     */
    public GraphicalPlayer(PlayerId correspondingPlayer, Map<PlayerId, String> playerNames) {
        assert isFxApplicationThread();
        this.observableGameState = new ObservableGameState(correspondingPlayer);
        this.correspondingPlayer = correspondingPlayer;
        this.playerNames = Map.copyOf(playerNames);
        this.drawTicketsHandler = new SimpleObjectProperty<>(null);
        this.drawCardHandler = new SimpleObjectProperty<>(null);
        this.infoProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.takeRouteHandler = new SimpleObjectProperty<>();

        generateStage().show();
    }

    /**
     * Sets a new state of the game.
     *
     * @param newGameState The new {@link ch.epfl.tchu.game.GameState}.
     * @param playerState The new {@link PlayerState}
     */
    public void setState(PublicGameState newGameState, PlayerState playerState) {
        assert isFxApplicationThread();
        observableGameState.setState(newGameState, playerState);
    }

    public void receiveInfo(String message) {
        assert isFxApplicationThread();
        // remove the first index of the observable list to not have more than VISIBLE_INFOS amount
        // of messages.
        if (infoProperty.size() == VISIBLE_INFOS) infoProperty.remove(0);
        infoProperty.add(new Text(message));
    }

    public void startTurn(
            ActionHandlers.DrawTicketsHandler drawTicketsH,
            ActionHandlers.DrawCardHandler drawCardH,
            ActionHandlers.ClaimRouteHandler claimRouteH) {
        assert isFxApplicationThread();

        if (observableGameState.canDrawCards().getValue()) {
            drawCardHandler.setValue(
                    indexOfChosenCard -> {
                        drawCardH.onDrawCard(indexOfChosenCard);
                        emptyHandlers();
                    });
        }
        if (observableGameState.canDrawTickets().getValue()) {
            drawTicketsHandler.setValue(
                    () -> {
                        drawTicketsH.onDrawTickets();
                        emptyHandlers();
                    });
        }
        takeRouteHandler.setValue(
                (claimedRoute, initialClaimCards) -> {
                    claimRouteH.onClaimRoute(claimedRoute, initialClaimCards);
                    emptyHandlers();
                });
    }

    /**
     * Opens a pop-up allowing the player to choose the tickets. Call chooseTicketsHandler upon
     * confirming the choice.
     *
     * @param choosableTickets The ticket the player can chose. Must be either 3 (start game) or 5
     *     (during the game)
     * @param chooseTicketsHandler The handler called upon choosing tickets.
     * @throws IllegalArgumentException If there is an invalid number of tickets.
     */
    public void chooseTickets(
            SortedBag<Ticket> choosableTickets,
            ActionHandlers.ChooseTicketsHandler chooseTicketsHandler) {
        assert isFxApplicationThread();
        Preconditions.checkArgument(choosableTickets.size() == 3 || choosableTickets.size() == 5);
        int minTickets = choosableTickets.size() - Constants.DISCARDABLE_TICKETS_COUNT;
        String title =
                String.format(StringsFr.CHOOSE_TICKETS, minTickets, StringsFr.plural(minTickets));
        new PopupChoiceBuilder<Ticket>(title, choosableTickets.toList())
                .setTitle(StringsFr.TICKETS_CHOICE)
                .setSelectionMode(SelectionMode.MULTIPLE)
                .setMinimumChoices(minTickets)
                .setMultipleItemsChosenHandler(
                        tickets -> chooseTicketsHandler.onChooseTickets(SortedBag.of(tickets)))
                .build()
                .show();
    }

    /**
     * Called after the player has chosen a card. Does not open a popup, but is nevertheless cool.
     *
     * @param handler The handler of the cards.
     */
    public void drawCards(ActionHandlers.DrawCardHandler handler) {
        assert isFxApplicationThread();
        drawCardHandler.set(
                indexOfChosenCard -> {
                    handler.onDrawCard(indexOfChosenCard);
                    emptyHandlers();
                });
    }

    /**
     * Opens a pop-up allowing the user to choose which cards to claim.
     *
     * @param choosableCards The card the player can choose.
     * @param handler The handler for the cards.
     */
    public void chooseClaimCards(
            List<SortedBag<Card>> choosableCards, ActionHandlers.ChooseCardsHandler handler) {
        assert isFxApplicationThread();
        new PopupChoiceBuilder<SortedBag<Card>>(StringsFr.CHOOSE_CARDS, choosableCards)
                .setTitle(StringsFr.CARDS_CHOICE)
                .setSelectionMode(SelectionMode.SINGLE)
                .setSingleItemChosenHandler(handler::onChooseCards)
                .setMinimumChoices(GuiConstants.MINIMUM_CHOICES_CLAIM_CARDS)
                .setCellStringBuilder(new CardBagStringConverter())
                .build()
                .show();
    }

    /**
     * Opens a pop up allowing the player to choose which additional cards.
     *
     * @param choosableCards THe cards the player can choose.
     * @param handler The handler.
     */
    public void chooseAdditionalCards(
            List<SortedBag<Card>> choosableCards, ActionHandlers.ChooseCardsHandler handler) {
        assert isFxApplicationThread();
        new PopupChoiceBuilder<SortedBag<Card>>(StringsFr.CHOOSE_ADDITIONAL_CARDS, choosableCards)
                .setTitle(StringsFr.CARDS_CHOICE)
                .setSelectionMode(SelectionMode.SINGLE)
                .setSingleItemChosenHandler(handler::onChooseCards)
                .setMinimumChoices(GuiConstants.MINIMUM_CHOICES_ADDITIONAL_CARDS)
                .setCellStringBuilder(new CardBagStringConverter())
                .build()
                .show();
    }

    /**
     * Generates the main stage.
     *
     * @return The main stage.
     */
    private Stage generateStage() {
        root = new Stage();
        root.setTitle(String.format(GuiConstants.TCHU_TITLE, playerNames.get(correspondingPlayer)));

        BorderPane mainPane =
                new BorderPane(
                        MapViewCreator.createMapView( // Center.
                                observableGameState, takeRouteHandler, this::chooseClaimCards),
                        null, // Top.
                        DecksViewCreator.createCardsView(
                                observableGameState, drawTicketsHandler, drawCardHandler), // Right.
                        DecksViewCreator.createHandView(observableGameState), // Bottom.
                        InfoViewCreator.createInfoView(
                                correspondingPlayer,
                                playerNames,
                                observableGameState,
                                infoProperty));
        Scene innerScene = new Scene(mainPane);
        root.setScene(innerScene);
        return root;
    }

    /** Empties the handlers. */
    private void emptyHandlers() {
        drawCardHandler.set(null);
        drawTicketsHandler.set(null);
        takeRouteHandler.set(null);
    }

    private static class CardBagStringConverter extends StringConverter<SortedBag<Card>> {
        @Override
        public String toString(SortedBag<Card> cards) {
            return Info.displaySortedBagOfCards(cards);
        }

        @Override
        public SortedBag<Card> fromString(String s) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Builder for choice popup.
     *
     * @param <T> The type of the items to make a choice from.
     */
    private final class PopupChoiceBuilder<T> {

        private final TextFlow description;
        private final ListView<T> choicesDisplayed = new ListView<>();
        private final Button confirm = new Button(StringsFr.CHOOSE);
        private Consumer<T> singleItemActionHandlerWrapper;
        private Consumer<List<T>> multipleItemsActionHandlerWrapper;
        private String title;

        /**
         * Constructor.
         *
         * @param description The description of the popups.
         * @param possibleChoices The choices possible.
         */
        public PopupChoiceBuilder(String description, List<T> possibleChoices) {
            this.description = new TextFlow(new Text(description));
            this.choicesDisplayed.setItems(FXCollections.observableList(possibleChoices));
        }

        /**
         * Sets the selection mode (multiple, single).
         *
         * @param mode The selection mode.
         * @return The same object (for chaining).
         */
        public PopupChoiceBuilder<T> setSelectionMode(SelectionMode mode) {
            choicesDisplayed.getSelectionModel().setSelectionMode(mode);
            return this;
        }

        /**
         * Sets the title of the popup.
         *
         * @param title The title.
         * @return The same object, for chaining.
         */
        public PopupChoiceBuilder<T> setTitle(String title) {
            this.title = title;
            return this;
        }
        /**
         * Sets the number (included) required of choice.
         *
         * @param threshold The minimum number of choice.
         * @return The object (for chaining).
         */
        public PopupChoiceBuilder<T> setMinimumChoices(int threshold) {
            confirm.disableProperty()
                    .bind(
                            Bindings.size(choicesDisplayed.getSelectionModel().getSelectedItems())
                                    .lessThan(threshold));
            return this;
        }

        /**
         * Sets the handler in case of multiple choice.
         *
         * @param actionHandlerWrapper The handler wrapped in a consumer.
         * @return The object (for chaining).
         */
        public PopupChoiceBuilder<T> setMultipleItemsChosenHandler(
                Consumer<List<T>> actionHandlerWrapper) {
            multipleItemsActionHandlerWrapper = actionHandlerWrapper;
            return this;
        }

        /**
         * Sets the handler in case of single item choice.
         *
         * @param actionHandlerWrapper the handler wrapped in a consumer.
         * @return The object (for chaining).
         */
        public PopupChoiceBuilder<T> setSingleItemChosenHandler(Consumer<T> actionHandlerWrapper) {
            this.singleItemActionHandlerWrapper = actionHandlerWrapper;
            return this;
        }

        /**
         * Sets the string converter for the cells of the choices.
         *
         * @param converter The converter.
         * @return The object (for chaining).
         */
        public PopupChoiceBuilder<T> setCellStringBuilder(StringConverter<T> converter) {
            choicesDisplayed.setCellFactory(tListView -> new TextFieldListCell<>(converter));
            return this;
        }

        /**
         * Builds the popup.
         *
         * @return The popup.
         */
        public Stage build() {
            Scene innerScene = new Scene(new VBox(description, choicesDisplayed, confirm));
            innerScene.getStylesheets().add(CHOOSER_CSS);

            Stage popup = new Stage(StageStyle.UTILITY);
            popup.setTitle(title);
            // This monstrosity adds to the current sets action handler an action that hides the
            // popup after pressing
            // the button.
            // The switch case differs the case when only one item needs to be selected vs multiple.
            confirm.setOnAction(
                    actionEvent -> {
                        switch (choicesDisplayed.getSelectionModel().getSelectionMode()) {
                            case MULTIPLE:
                                multipleItemsActionHandlerWrapper
                                        .andThen(ts -> popup.hide())
                                        .accept(
                                                new ArrayList<>(
                                                        choicesDisplayed
                                                                .getSelectionModel()
                                                                .getSelectedItems()));
                                break;
                            case SINGLE:
                                singleItemActionHandlerWrapper
                                        .andThen(ts -> popup.hide())
                                        .accept(
                                                choicesDisplayed
                                                        .getSelectionModel()
                                                        .getSelectedItem());
                                break;
                        }
                    });

            popup.initOwner(root);
            popup.initModality(Modality.WINDOW_MODAL);
            // Disable close button
            popup.setOnCloseRequest(Event::consume);
            popup.setScene(innerScene);
            return popup;
        }
    }
}
