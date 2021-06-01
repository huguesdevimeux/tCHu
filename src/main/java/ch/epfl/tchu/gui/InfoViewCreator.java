package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.PlayerId;
import ch.epfl.tchu.net.RunClient;
import ch.epfl.tchu.net.RunServer;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.util.Map;

import static ch.epfl.tchu.gui.GuiConstants.*;

/**
 * Representation of the information section in the game. Not instantiable.
 *
 * @author Luca Mouchel (324748)
 * @author Hugues Devimeux (327282)
 */
public final class InfoViewCreator {
    public static VBox root;

    /** Not Instantiable. */
    private InfoViewCreator() {}

    /**
     * Creates the info view part (the left side of the game) dealing with each player's stats :
     * ticket, car, card count and claimPoints. Also contains the area where messages will appear.
     *
     * @param correspondingPlayer the currentPlayerID
     * @param playerNames map matching the playerID with the names
     * @param obsGameState observable state of the game
     * @param infos contains the information on the progress of the game
     * @return the information part of the game
     */
    public static Node createInfoView(
            PlayerId correspondingPlayer,
            Map<PlayerId, String> playerNames,
            ObservableGameState obsGameState,
            ObservableList<Text> infos) {
        root = new VBox();
        root.getStylesheets().addAll(INFO_CSS, COLORS_CSS);

        VBox playerStats = new VBox();
        playerStats.setId(ID_PLAYER_STATS);

        playerStats
                .getChildren()
                .addAll(
                        createPlayerInfoView(correspondingPlayer, obsGameState, playerNames),
                        createPlayerInfoView(
                                correspondingPlayer.next(), obsGameState, playerNames));

        TextFlow gameInfoTextFlow = new TextFlow();
        gameInfoTextFlow.setId(ID_GAME_INFO);
        Bindings.bindContent(gameInfoTextFlow.getChildren(), infos);
        // instantiate two properties, one is the player's ticket points and the other
        // represents a sentence that will appear once a ticket is completed by the player.
        ReadOnlyIntegerProperty points = obsGameState.playerTicketPoints();
        StringProperty isTicketCompleted = new SimpleStringProperty("");
        points.addListener(
                (observableValue, oldV, newV) -> {
                    // we can detect if a ticket is completed if the player's ticket points change
                    // hence the old value != new value. We also have to put the condition that the
                    // player's ticket list isn't empty as at the beginning of the game, each
                    // player's ticket points
                    // goes from 0 to (-n) so the game would perceive it as a completed ticket but
                    // it isn't
                    if (!oldV.equals(newV) && !obsGameState.playersTicketsList().isEmpty()) {
                        isTicketCompleted.setValue(StringsFr.VALIDATED_TICKET);
                        // we display the message only for a limited amount of time and once
                        // finished, we reset the string property's value to an empty string.
                        PauseTransition pt = new PauseTransition(Duration.seconds(5));
                        pt.setOnFinished(actionEvent -> isTicketCompleted.setValue(""));
                        pt.playFromStart();
                    }
                });
        StringExpression ticketPoints =
                Bindings.format(StringsFr.PLAYER_TICKET_POINTS, points, isTicketCompleted);
        Text ticketPointsText = new Text();
        ticketPointsText.textProperty().bind(ticketPoints);
        VBox displayTicketPoints = new VBox(ticketPointsText);
        displayTicketPoints.setId(ID_PLAYER_STATS);
        VBox chat = new VBox();
        Button toChat = new Button("Chat");
        chat.getChildren().add(toChat);
        chat.setAlignment(Pos.CENTER);

        root.getChildren()
                .addAll(
                        playerStats,
                        new Separator(),
                        displayTicketPoints,
                        new Separator(),
                        gameInfoTextFlow);
        return root;
    }

    public static VBox getRoot() {
        return root;
    }

    /**
     * Private method to return a player's info view, ie the player's stats in the form of a text
     * flow.
     *
     * @param player the player's view
     * @param obsGameState the observable game state
     * @param playerNames map with the names of the player
     * @return a node representing the views with the info.
     */
    private static Node createPlayerInfoView(
            PlayerId player, ObservableGameState obsGameState, Map<PlayerId, String> playerNames) {
        TextFlow playerN = new TextFlow();
        playerN.getStyleClass().add(player.name());
        Circle circle = new Circle(5);
        circle.getStyleClass().add(STYLE_CLASS_FILLED);

        // StringExpression will update the properties automatically when they are changed.
        StringExpression updatedExpression =
                Bindings.format(
                        StringsFr.PLAYER_STATS,
                        playerNames.get(player),
                        obsGameState.playerTicketCount(player),
                        obsGameState.playerCardCount(player),
                        obsGameState.playerCarCount(player),
                        obsGameState.playerClaimPoints(player));
        Text playerStatsText = new Text();
        playerStatsText.textProperty().bind(updatedExpression);
        playerN.getChildren().addAll(circle, playerStatsText);
        return playerN;
    }
}
