package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.PlayerId;
import ch.epfl.tchu.game.PlayerState;
import ch.epfl.tchu.game.PublicGameState;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Map;

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
        this.playerNames = playerNames;
        this.drawTicketsHandler = new SimpleObjectProperty<>(null);
        this.drawCardHandler = new SimpleObjectProperty<>(null);
        this.infoProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.takeRouteHandler = new SimpleObjectProperty<>();

        this.generateStage().show();
    }

    /**
     * Sets a new state of the game.
     *
     * @param newGameState The new {@link ch.epfl.tchu.game.GameState}.
     * @param playerState The new {@link PlayerState}
     */
    public void setState(PublicGameState newGameState, PlayerState playerState) {
        assert isFxApplicationThread();
        this.observableGameState.setState(newGameState, playerState);
    }

    public void receiveInfo(String message) {
        assert isFxApplicationThread();
        infoProperty.getValue().add(new Text(message));
    }

    public void startTurn(
            ActionHandlers.DrawTicketsHandler drawTicketsH,
            ActionHandlers.DrawCardHandler drawCardH,
            ActionHandlers.ClaimRouteHandler claimRouteH) {
        assert isFxApplicationThread();

        if (this.observableGameState.canDrawCards().getValue()) {
            this.drawCardHandler.setValue(
                    indexOfChosenCard -> {
                        drawCardH.onDrawCard(indexOfChosenCard);
                        emptyHandlers();
                    });
        }
        if (this.observableGameState.canDrawTickets().getValue()) {
            this.drawTicketsHandler.setValue(
                    () -> {
                        drawTicketsH.onDrawTickets();
                        emptyHandlers();
                    });
        }
        this.takeRouteHandler.setValue(
                (claimedRoute, initialClaimCards) -> {
                    emptyHandlers();
                    claimRouteH.onClaimRoute(claimedRoute, initialClaimCards);
                });
    }

    private Stage generateStage() {
        Stage root = new Stage();
        root.setTitle(
                String.format("tCHu \u2014 %s", this.playerNames.get(this.correspondingPlayer)));

        BorderPane mainPane =
                new BorderPane(
                        MapViewCreator.createMapView( // Center.
                                this.observableGameState, this.takeRouteHandler, null),
                        null, // Top.
                        DecksViewCreator.createCardsView(
                                this.observableGameState,
                                this.drawTicketsHandler,
                                this.drawCardHandler), // Right.
                        DecksViewCreator.createHandView(this.observableGameState), // Bottom.
                        InfoViewCreator.createInfoView(
                                this.correspondingPlayer,
                                this.playerNames,
                                this.observableGameState,
                                infoProperty));
        Scene innerScene = new Scene(mainPane);
        root.setScene(innerScene);
        return root;
    }

    private void emptyHandlers() {
        this.drawCardHandler.set(null);
        this.drawTicketsHandler.set(null);
        this.drawTicketsHandler.set(null);
    }
}
