package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.PlayerId;
import ch.epfl.tchu.game.PlayerState;
import ch.epfl.tchu.game.PublicGameState;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.Map;

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

    /**
     * Constructor that builds graphical interface designed for the player.
     *
     * @param correspondingPlayer The player associated.
     * @param playerNames The players playing.
     */
    public GraphicalPlayer(PlayerId correspondingPlayer, Map<PlayerId, String> playerNames) {
        this.observableGameState = new ObservableGameState(correspondingPlayer);
        this.correspondingPlayer = correspondingPlayer;
        this.playerNames = playerNames;

        this.generateStage().show();
    }

    /**
     * Sets a new state of the game.
     *
     * @param newGameState The new {@link ch.epfl.tchu.game.GameState}.
     * @param playerState The new {@link PlayerState}
     */
    public void setState(PublicGameState newGameState, PlayerState playerState) {
        this.observableGameState.setState(newGameState, playerState);
    }

    public void receiveInfo(String message) {
        // update infos (do a queue size)
    }

    public void startTurn(
            ActionHandlers.DrawTicketsHandler drawTicketsH,
            ActionHandlers.DrawCardHandler drawCardH,
            ActionHandlers.ClaimRouteHandler claimRouteH) {}

    private Stage generateStage() {
        Stage root = new Stage();

        BorderPane mainPane =
                new BorderPane(
                        MapViewCreator.createMapView( // Center.
                                this.observableGameState, new SimpleObjectProperty<>(null), null),
                        null, // Top.
                        DecksViewCreator.createCardsView(
                                this.observableGameState,
                                new SimpleObjectProperty<>(null),
                                new SimpleObjectProperty<>(null)), // Right.
                        DecksViewCreator.createHandView(this.observableGameState), // Bottom.
                        InfoViewCreator.createInfoView(
                                correspondingPlayer,
                                playerNames,
                                this.observableGameState,
                                new SimpleListProperty<>()));
        Scene innerScene = new Scene(mainPane);
        root.setScene(innerScene);
        return root;
    }
}
