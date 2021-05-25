package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.PlayerId;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
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

        VBox chat = new VBox();
        Button toChat = new Button("Chat");
        chat.getChildren().add(toChat);
        chat.setAlignment(Pos.CENTER);

       toChat.setOnAction(e -> {
           try {
               new ChatServerMain().start(new Stage());
               new ChatClientMain().start(new Stage());
           } catch (Exception exception) {
               exception.printStackTrace();
           }
       });

        root.getChildren().addAll(playerStats, new Separator(), gameInfoTextFlow, chat);
        return root;
    }
}
