package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.PlayerId;
import ch.epfl.tchu.gui.animation.AbstractAnimation;
import ch.epfl.tchu.gui.animation.FadeAnimation;
import ch.epfl.tchu.gui.animation.TranslationAnimation;
import ch.epfl.tchu.net.*;
import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.io.IOException;
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
        TranslationAnimation translationAnimation =
                new TranslationAnimation(
                        DURATION_INFO_ANIMATION, 0, 0, Interpolator.EASE_IN, Interpolator.LINEAR);
        FadeAnimation fadeAnimation = new FadeAnimation(DURATION_INFO_ANIMATION, 0, 1);
        infos.addListener(
                (ListChangeListener<? super Text>)
                        change -> {
                            change.next();
                            if (change.wasAdded()) {
                                var changed = change.getAddedSubList().get(0);
                                gameInfoTextFlow.getChildren().add(changed);
                                changed.setTranslateX(OFFSET_X_INFOS);
                                AbstractAnimation c = translationAnimation.attachTo(changed);
                                fadeAnimation.attachTo(changed).play();
                                c.play();
                            }
                            if (change.wasRemoved()) {
                                gameInfoTextFlow.getChildren().removeAll(change.getRemoved());
                            }
                        });

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

        Parent chatApp =
                ObservableGameState.isServer.get()
                        ? createChatPanel(
                                PlayerId.PLAYER_1,
                                playerNames,
                                RunServer.messages,
                                RunServer.connection)
                        : createChatPanel(
                                PlayerId.PLAYER_2,
                                playerNames,
                                RunClient.messages,
                                RunClient.connection);
		ScrollPane scrollPaneInfos = new ScrollPane(gameInfoTextFlow);
		scrollPaneInfos.setFitToHeight(true);
		infos.addListener((ListChangeListener<? super Text>) change -> scrollPaneInfos.setVvalue(1.0));
		root.getChildren()
                .addAll(
                        playerStats,
                        new Separator(),
                        displayTicketPoints,
                        new Separator(),
					scrollPaneInfos);

        if (MainMenuServerController.checkBoxSelected || MainMenuClientController.checkBoxSelected)
            root.getChildren().addAll(chatApp);
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
                new ImageView(
                        new Image(ProfileImagesUtils.pathOfImageOf(player).toUri().toString()));
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(76);
        playerN.setSpacing(10);
        playerN.getChildren().addAll(imageView, playerInfoWithcolo);
        return playerN;
    }

    public static Parent createChatPanel(
            PlayerId corresponsingPlayer,
            Map<PlayerId, String> names,
            ObservableList<Map.Entry<PlayerId, String>> messages,
            ChattingConnection connection) {

        TextField input = new TextField();
        input.setStyle("-fx-background-color: #cfcfcf ");
        input.setPromptText("Message");

        String borderCss = "-fx-border-insets: 5;\n" + "-fx-border-width: 1;\n";

        VBox messagesStack = new VBox();
        messagesStack.setStyle(borderCss);
        messagesStack.setMinHeight(250);

        input.setOnAction(
                e -> {
                    String message = input.getText();
                    if (!input.getText().isBlank()) {
                        messages.add(Map.entry(corresponsingPlayer, message));
                        try {
                            connection.send(message);
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }
                    }
                    input.clear();
                });
        ScrollPane scrollPane = new ScrollPane(messagesStack);
        scrollPane.setMinHeight(250);
        scrollPane.setMaxHeight(250);
        messages.addListener(
                (ListChangeListener<? super Map.Entry<PlayerId, String>>)
                        c -> {
                            c.next();
                            PlayerId playerId = c.getAddedSubList().get(0).getKey();
                            HBox individualMessage =
                                    getIndividualMessage(
                                            playerId,
                                            names.get(playerId),
                                            c.getAddedSubList().get(0).getValue());
                            messagesStack.getChildren().add(individualMessage);
                        });
        messagesStack
                .heightProperty()
                .addListener((observable, oldValue, newValue) -> scrollPane.setVvalue(1.0));
        VBox root = new VBox(5, scrollPane, input);
        root.setPrefSize(100, 250);
        return root;
    }

    private static HBox getIndividualMessage(PlayerId playerId, String name, String message) {
        Text playerName = new Text(name + " : ");
        playerName.setStyle("-fx-font-weight: bold");
        Circle profilePicture = new Circle(20);
        profilePicture.setFill(
                new ImagePattern(
                        new Image(ProfileImagesUtils.pathOfImageOf(playerId).toUri().toString())));
        HBox individualMessage = new HBox(profilePicture, playerName, new Text(message));
        individualMessage.setAlignment(Pos.CENTER_LEFT);
        String messageBorderCSS = "-fx-border-insets: 5;\n" + "-fx-border-width: 0.5;";
        individualMessage.setStyle(messageBorderCSS);
        return individualMessage;
    }
}
