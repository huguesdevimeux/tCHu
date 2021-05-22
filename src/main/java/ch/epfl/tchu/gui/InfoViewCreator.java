package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.PlayerId;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.util.Map;

import static ch.epfl.tchu.gui.GuiConstants.*;

/**
 * Representation of the information section in the game.
 * Not instantiable.
 *
 * @author Luca Mouchel (324748)
 * @author Hugues Devimeux (327282)
 */
final class InfoViewCreator {

    /**
     * Not Instantiable.
     */
    private InfoViewCreator() {
    }

    /**
     * Creates the info view part (the left side of the game)
     * dealing with each player's stats : ticket, car, card count
     * and claimPoints. Also contains the area where messages will
     * appear.
     *
     * @param currentPlayer the currentPlayerID
     * @param playerNames   map matching the playerID with the names
     * @param obsGameState  observable state of the game
     * @param infos         contains the information on the progress of the game
     * @return the information part of the game
     */
    public static Node createInfoView(PlayerId currentPlayer,
                                      Map<PlayerId, String> playerNames,
                                      ObservableGameState obsGameState,
                                      ObservableList<Text> infos) {
        VBox root = new VBox();
        root.getStylesheets().addAll(INFO_CSS, COLORS_CSS);

        VBox playerStats = new VBox();
        playerStats.setId(ID_PLAYER_STATS);

        for (PlayerId playerId : PlayerId.ALL) {
            TextFlow playerN = new TextFlow();
            playerN.getStyleClass().add(playerId.name());

            Circle circle = new Circle(5);
            circle.getStyleClass().add(STYLE_CLASS_FILLED);

            //StringExpression will update the properties automatically when they are changed.
            StringExpression updatedExpression = Bindings.format(StringsFr.PLAYER_STATS,
                    playerNames.get(playerId),
                    obsGameState.playerTicketCount(playerId),
                    obsGameState.playerCardCount(playerId),
                    obsGameState.playerCarCount(playerId),
                    obsGameState.playerClaimPoints(playerId));
            Text playerStatsText = new Text();
            playerStatsText.textProperty().bind(updatedExpression);

            playerN.getChildren().addAll(circle, playerStatsText);
            playerStats.getChildren().add(playerN);
        }
        TextFlow gameInfoTextFlow = new TextFlow();
        gameInfoTextFlow.setId(ID_GAME_INFO);
        Bindings.bindContent(gameInfoTextFlow.getChildren(), infos);

        // instantiate two properties, one is the player's ticket points and the other
        // represents a sentence that will appear once a ticket is completed by the player.
        ReadOnlyIntegerProperty points = obsGameState.playerTicketPoints();
        StringProperty isTicketCompleted = new SimpleStringProperty(EMPTY_STRING);
        points.addListener(
                (observableValue, oldV, newV) -> {
                    // we can detect if a ticket is completed if the player's ticket points change
                    // hence the old value != new value. We also have to put the condition that the
                    // old value != 0 as when the game starts all points are at 0 before picking tickets
                    // so the message validated ticket would appear although no one would have played.
                    if (!oldV.equals(newV) && oldV.intValue() != 0) {
                        isTicketCompleted.setValue(StringsFr.VALIDATED_TICKET);
                        // we display the message only for a limited amount of time and once
                        // finished, we reset the string property's value to an empty string.
                        PauseTransition pauseTransition = new PauseTransition(Duration.seconds(8));
                        pauseTransition.setOnFinished(
                                actionEvent -> isTicketCompleted.setValue(EMPTY_STRING));
                        pauseTransition.playFromStart();
                    }
                });
        StringExpression ticketPoints =
                Bindings.format(StringsFr.PLAYER_TICKET_POINTS, points, isTicketCompleted);
        Text ticketPointsText = new Text();
        ticketPointsText.textProperty().bind(ticketPoints);
        root.getChildren()
                .addAll(
                        playerStats,
                        new Separator(),
                        ticketPointsText,
                        new Separator(),
                        gameInfoTextFlow);
        return root;
    }
}
