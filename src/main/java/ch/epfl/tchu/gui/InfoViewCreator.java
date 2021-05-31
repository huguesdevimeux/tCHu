package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.PlayerId;
import ch.epfl.tchu.net.ProfileImagesUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.Map;

import static ch.epfl.tchu.gui.GuiConstants.*;

/**
 * Representation of the information section in the game. Not instantiable.
 *
 * @author Luca Mouchel (324748)
 * @author Hugues Devimeux (327282)
 */
final class InfoViewCreator {

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
        VBox root = new VBox();
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
        root.getChildren().addAll(playerStats, new Separator(), gameInfoTextFlow);
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
        HBox playerN = new HBox();
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
        HBox playerInfoWithcolo = new HBox(circle, playerStatsText);

		ImageView imageView =
                new ImageView(new Image(ProfileImagesUtils.pathOfImageOf(player).toUri().toString()));
		imageView.setPreserveRatio(true);
		imageView.setFitHeight(76);
		playerN.setSpacing(10);
        playerN.getChildren().addAll(imageView, playerInfoWithcolo );
		return playerN;
    }
}
