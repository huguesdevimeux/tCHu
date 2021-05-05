package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.PlayerId;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.Map;

/**
 * Representation of the information section in the game.
 * Not instantiable.
 *
 * @author Luca Mouchel (324748)
 * @author Hugues Devimeux (327282)
 */
class InfoViewCreator {
    private static final String STYLE_SHEET_INFO = "info.css";
    private static final String STYLE_SHEET_COLOR = "colors.css";
    private static final String ID_PLAYER_STATS = "player-stats";
    private static final String STYLE_CLASS_FILLED = "filled";
    private static final String ID_GAME_INFO = "game-info";

    /** Not Instantiable. */
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
        root.getStylesheets().addAll(STYLE_SHEET_INFO, STYLE_SHEET_COLOR);

        VBox playerStats = new VBox();
        playerStats.setId(ID_PLAYER_STATS);

        for (PlayerId playerId : PlayerId.ALL) {
            TextFlow playerN = new TextFlow();
            playerN.getStyleClass().add(
                    String.format("PLAYER_%s", PlayerId.ALL.indexOf(playerId) + 1));
            //We add +1 because PLAYER_1 for example is at index 0 in PlayerId.ALL but
            //we need the 1.

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
        //limit to 4 because there are maximum 5 messages
        for (int i = 0; i <= 4; i++) {
            gameInfoTextFlow.getChildren().add(new Text());
        }
        Bindings.bindContent(gameInfoTextFlow.getChildren(), infos);
        root.getChildren().addAll(playerStats, new Separator(), gameInfoTextFlow);
        return root;
    }
}
