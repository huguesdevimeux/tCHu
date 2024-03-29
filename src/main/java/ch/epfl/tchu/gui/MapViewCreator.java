package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.Card;
import ch.epfl.tchu.game.ChMap;
import ch.epfl.tchu.game.Route;
import ch.epfl.tchu.gui.ActionHandlers.ChooseCardsHandler;
import ch.epfl.tchu.gui.ActionHandlers.ClaimRouteHandler;
import ch.epfl.tchu.gui.animation.AbstractAnimation;
import ch.epfl.tchu.gui.animation.IndicationAnimation;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.List;

import static ch.epfl.tchu.gui.GuiConstants.*;

/**
 * Representation of the map and the routes in the game.
 *
 * @author Luca Mouchel (324748)
 * @author Hugues Devimeux (327282)
 */
final class MapViewCreator {

    private static Pane gameMapPane;
    /** Not instantiable. */
    private MapViewCreator() {}

    /**
     * Method in charge of creating the whole view of the map. ie the routes, the map, the colors.
     *
     * @param obsGameState the observable part of the game.
     * @param routeHandler responsible for an attempt to claim a route.
     * @param cardChooser responsible for choosing cards.
     * @return the node that contains all elements in the mapView - an instance of Pane in this
     *     case.
     */
    public static Node createMapView(
            ObservableGameState obsGameState,
            ObjectProperty<ClaimRouteHandler> routeHandler,
            CardChooser cardChooser) {
        gameMapPane = new Pane();
        gameMapPane.getStylesheets().addAll(MAP_CSS, COLORS_CSS);
        gameMapPane.getChildren().add(new ImageView());//this adds the background to the pane
        IndicationAnimation indicationAnimation =
                new IndicationAnimation(
                        Duration.millis(DURATION_ANIMATION),
                        SCALING_X_INDICATION,
                        SCALING_Y_INDICATION,
                        NUMBER_BOUNCES_INDICATION);

        for (Route route : ChMap.routes()) {
            Group mainRouteGroup = new Group();
            mainRouteGroup.setId(route.id());
            mainRouteGroup
                    .getStyleClass()
                    .addAll(
                            STYLE_CLASS_ROUTE,
                            route.level().name(),
                            convertColorToCssColor(route.color()));

            for (int i = 1; i <= route.length(); i++) {
                Group eachRoutesBlock = new Group();
                eachRoutesBlock.setId(String.format(ROUTE_RECT_ID, route.id(), i));

                Rectangle rectForTracks = new Rectangle(RECTANGLE_WIDTH, RECTANGLE_HEIGHT);
                rectForTracks.getStyleClass().addAll(STYLE_CLASS_TRACK, STYLE_CLASS_FILLED);

                Group routeCars = new Group();
                routeCars.getStyleClass().add(STYLE_CLASS_CAR);

                Rectangle rectForCars = new Rectangle(RECTANGLE_WIDTH, RECTANGLE_HEIGHT);
                rectForCars.getStyleClass().add(STYLE_CLASS_FILLED);
                Circle circle1 = new Circle(CIRCLE1_CENTER_X, CIRCLE_CENTER_Y, ROUTE_CIRCLE_RADIUS);
                Circle circle2 = new Circle(CIRCLE2_CENTER_X, CIRCLE_CENTER_Y, ROUTE_CIRCLE_RADIUS);
                routeCars.getChildren().addAll(rectForCars, circle1, circle2);

                // established hierarchy : cars group -> block group -> route group
                eachRoutesBlock.getChildren().addAll(rectForTracks, routeCars);
                mainRouteGroup.getChildren().add(eachRoutesBlock);
            }
            gameMapPane.getChildren().add(mainRouteGroup);

            // If the route handler is null or the player can't claim the route
            // we disable the player's attempt to claim the route, ie pressing
            // on a route will do nothing.
            mainRouteGroup
                    .disableProperty()
                    .bind(routeHandler.isNull().or(obsGameState.playerCanClaimRoute(route).not()));

			AbstractAnimation animation = indicationAnimation.attachTo(mainRouteGroup);

            // If the route is owned by a player, we fill the route's blocks
            // with the corresponding player's color (light blue for PLAYER_1 for ex)
            obsGameState
                    .getRoutesOwner(route)
                    .addListener(
                            (observableValue, oldValue, newValue) ->
                                    mainRouteGroup.getStyleClass().add(newValue.name()));

            mainRouteGroup.setOnMouseClicked(
                    event -> {
                        List<SortedBag<Card>> possibleClaimCards =
                                obsGameState.possibleClaimCards(route);
                        if (possibleClaimCards.size() == 1)
                            routeHandler.get().onClaimRoute(route, possibleClaimCards.get(0));
                        else {
                            ChooseCardsHandler chooseCardsH =
                                    chosenCards ->
                                            routeHandler.get().onClaimRoute(route, chosenCards);
                            cardChooser.chooseCards(possibleClaimCards, chooseCardsH);
                        }
                    });
        }
        return gameMapPane;
    }
    /**
     * Returns the main pane of the map.
     * @return the game pane.
     */
    public static Pane getGameMapPane() {
        return gameMapPane;
    }
    /**
     * Interface containing a method intended to be called when the player must choose the cards he
     * wishes to use to seize a route.
     */
    @FunctionalInterface
    public interface CardChooser {
        /**
         * Called so user chooses cards he wants to use to claim a route.
         *
         * @param options cards to choose from
         * @param handler used when the player has made the choice
         */
        void chooseCards(List<SortedBag<Card>> options, ChooseCardsHandler handler);
    }
}
