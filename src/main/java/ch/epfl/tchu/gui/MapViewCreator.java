package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.Card;
import ch.epfl.tchu.game.ChMap;
import ch.epfl.tchu.game.Route;
import ch.epfl.tchu.gui.ActionHandlers.ChooseCardsHandler;
import ch.epfl.tchu.gui.ActionHandlers.ClaimRouteHandler;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class MapViewCreator {
    private static final String LOCOMOTIVE_COLOR = "NEUTRAL";
    private static final String STYLE_CLASS_ROUTE = "route";
    private static final String STYLE_CLASS_TRACK = "track";
    private static final String STYLE_CLASS_FILLED = "filled";
    private static final String STYLE_CLASS_CAR = "car";
    private static final String STYLE_SHEET_MAP = "map.css";
    private static final String STYLE_SHEET_COLORS = "colors.css";

    /** Not instantiable. */
    private MapViewCreator() {}

    public static Node createMapView(
            ObservableGameState obsGameState,
            ObjectProperty<ClaimRouteHandler> routeHandler,
            CardChooser cardChooser) {
        Pane gameMapPane = new Pane();
        gameMapPane.getStylesheets().addAll(STYLE_SHEET_MAP, STYLE_SHEET_COLORS);
        gameMapPane.getChildren().add(new ImageView());

        for (Route route : ChMap.routes()) {
            Group mainRouteGroup = new Group();
            mainRouteGroup.setId(route.id());
            mainRouteGroup
                    .getStyleClass()
                    .addAll(
                            STYLE_CLASS_ROUTE,
                            route.level().name(),
                            route.color() == null ? LOCOMOTIVE_COLOR : route.color().name());

            for (int i = 1; i <= route.length(); i++) {
                Group eachRoutesBlock = new Group();
                eachRoutesBlock.setId(String.format("%s_%s", route.id(), i));

                Rectangle rectForTracks = new Rectangle(36, 12);
                rectForTracks.getStyleClass().addAll(STYLE_CLASS_TRACK, STYLE_CLASS_FILLED);
                eachRoutesBlock.getChildren().add(rectForTracks);

                Group routesCarsGroup = new Group();
                routesCarsGroup.getStyleClass().add(STYLE_CLASS_CAR);

                Rectangle rectForCars = new Rectangle(36, 12);
                rectForCars.getStyleClass().add(STYLE_CLASS_FILLED);

                Circle circle1 = new Circle(12, 6, 3);
                Circle circle2 = new Circle(24, 6, 3);

                // established hierarchy : cars group -> block group -> route group
                routesCarsGroup.getChildren().addAll(rectForCars, circle1, circle2);
                eachRoutesBlock.getChildren().add(routesCarsGroup);
                mainRouteGroup.getChildren().add(eachRoutesBlock);
            }
            gameMapPane.getChildren().add(mainRouteGroup);

            obsGameState
                    .getGameState()
                    .addListener(
                            (observableValue, oldGS, newGS) -> {
                                mainRouteGroup
                                        .disableProperty()
                                        .bind(
                                                routeHandler
                                                        .isNull()
                                                        .or(obsGameState.playerCanClaimRoute(route))
                                                        .not());
                            });

            mainRouteGroup.setOnMouseClicked(
                    event -> {
                        List<SortedBag<Card>> possibleClaimCards = route.possibleClaimCards();
                        ClaimRouteHandler claimRouteH =
                                (claimedRoute, initialClaimCards) ->
                                        obsGameState
                                                .getPlayerState()
                                                .get()
                                                .withClaimedRoute(claimedRoute, initialClaimCards);
                        ChooseCardsHandler chooseCardsH =
                                chosenCards -> claimRouteH.onClaimRoute(route, chosenCards);
                        cardChooser.chooseCards(possibleClaimCards, chooseCardsH);
                    });
        }
        return gameMapPane;
    }

    @FunctionalInterface
    public interface CardChooser {
        void chooseCards(List<SortedBag<Card>> options, ChooseCardsHandler handler);
    }
}
