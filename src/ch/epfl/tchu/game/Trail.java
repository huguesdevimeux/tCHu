package ch.epfl.tchu.game;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Represents a Trail (=path) from one location to another, using several routes. Immutable. WARNING
 * : A {@link Trail} is directed, while a {@link Route} is not!
 *
 * @author Hugues Devimeux (327282)
 */
public final class Trail {

    private final Station station1;
    private final Station station2;
    private final int length;
    private final List<Route> compoundRoutes;

    /**
     * Private constructor for Trail. WARNING : A trail is directed, while a route is NOT.
     *
     * @param station1 Start station of the trail. Can be null.
     * @param station2 End station of the trail. Can be null.
     * @param compoundRoutes route composing the trail. Can be null.
     */
    private Trail(Station station1, Station station2, List<Route> compoundRoutes) {
        // NOTE : Station 1 and Station2 are to specify the direction of the trail (a Route is
        // undirected when a Trail is directed).
        this.compoundRoutes = compoundRoutes;
        int lengthTemp = 0;
        if (!(compoundRoutes == null)) lengthTemp = computeLength(compoundRoutes);
        this.length = lengthTemp;
        // station1 and station2 are null <=> length == 0.
        if (length == 0 || station1 == null || station2 == null) {
            this.station1 = null;
            this.station2 = null;
        } else {
            this.station2 = station2;
            this.station1 = station1;
        }
    }

    private static int computeLength(List<Route> routes) {
        return routes.stream().map(Route::length).reduce(Integer::sum).orElse(0);
    }

    /**
     * Returns the longest {@link Trail} composed by given routes. If there are several longest
     * {@link Trail}, returns unspecified. If the <code>routes</code> list is empty, return {@link
     * Trail} object with length 0 and both {@link Station} null.
     *
     * @param routes routes to work on.
     * @return the longest Trail
     */
    public static Trail longest(List<Route> routes) {
        if (routes.isEmpty()) {
            // Create an empty Trail.
            return new Trail(null, null, null);
        }
        if (routes.size() == 1) {
            return new Trail(routes.get(0).station1(), routes.get(0).station2(), routes);
        }
        // cs in the paper.
        Trail currentLongestTrail = null;
        int currentMaxLength = -9999;

        // Initialization of constructingTrails (cs) .
        List<Trail> constructingTrails = new ArrayList<>();
        for (Route route : routes) {
            Trail toAdd = new Trail(route.station1(), route.station2(), List.of(route));
            constructingTrails.add(toAdd);
            if (toAdd.length() > currentMaxLength) {
                currentMaxLength = toAdd.length();
                currentLongestTrail = toAdd;
            }
            constructingTrails.add(new Trail(route.station2(), route.station1(), List.of(route)));
        }

        while (!constructingTrails.isEmpty()) {
            // cs_prime in the paper.
            List<Trail> newIterationTrails = new ArrayList<>();

            for (Trail trail : constructingTrails) {
                // There are two "direction to check : one where the route station1 is the future tip of the trail, one where the route's station 2 is the future tip of the trail.
                // A route can be added these two conditions are satisfied:
                //  - the end station of the trail is one of the stations of the route.
                //  - the route does not blongs to the trail.
                Predicate<Route> belongsToTrail = (route) -> trail.compoundRoutes().contains(route);

                Predicate<Route> filterRoutesConnectedWithStation1 = (route) -> !belongsToTrail.test(route) &&  (trail.station2().id() == route.station1().id());
                // rs in the paper.
                List<Route> routesConnectedWithStation1 = new ArrayList<>();
                for (Route route1 : routes) {
                    if (filterRoutesConnectedWithStation1.test(route1)) {
                        routesConnectedWithStation1.add(route1);
                    }
                }
                for (Route r: routesConnectedWithStation1) {
                    // The new ending station will be station2. (station1 was the connected node).
                    Trail newTrail = appendToTrail(trail, r.station2(), r);
                    newIterationTrails.add(newTrail);
                    if (newTrail.length() > currentMaxLength) {
                        currentMaxLength = newTrail.length();
                        currentLongestTrail = newTrail;
                    }
                }

                Predicate<Route> filterRouteConnectedWithStation2 = (route) -> !belongsToTrail.test(route) && trail.station2().equals(route.station2());
                // rs in the paper.
                List<Route> routesConnectedWithStation2 = routes.stream().filter(filterRouteConnectedWithStation2).collect(Collectors.toList());
                for (Route r: routesConnectedWithStation2) {
                    // The new ending station will be station1. (station2 was the connected node).
                    Trail newTrail = appendToTrail(trail, r.station1(), r);
                    newIterationTrails.add(newTrail);
                    if (newTrail.length() > currentMaxLength) {
                        currentMaxLength = newTrail.length();
                        currentLongestTrail = newTrail;
                    }
                }
            }
            constructingTrails = newIterationTrails;
        }
        return currentLongestTrail;

    }

    /**
     * Returns a new Trail appended with the route and given station .
     *
     * @param trail the trail to append.
     * @param newStation the new station (must be specified because route are not directed
     * @param route : route to add.
     * @return the new Trail object.
     */
    private static Trail appendToTrail(@NotNull Trail trail, Station newStation, Route route) {
        List<Route> newRoutes = new ArrayList<>(trail.compoundRoutes());
        newRoutes.add(route);
        return new Trail(trail.station1(), newStation, newRoutes);
    }
    /**
     * Gets endStation
     *
     * @return value of endStation
     */
    public Station station2() {
        return station2;
    }

    /**
     * Gets station1
     *
     * @return value of station1
     */
    public Station station1() {
        return station1;
    }

    /**
     * Gets length
     *
     * @return value of length
     */
    public int length() {
        return length;
    }

    @Override
    public String toString() {
        String startStationName = station1 != null ? station1.name() : "null";
        String endStationName = station2 != null ? station2.name() : "null";
        if (compoundRoutes == null || compoundRoutes.size() == 1) {
            return String.format("%s - %s (%s)", startStationName, endStationName, length);
        }
        List<Station> intermediateStations = new ArrayList<>();
        intermediateStations.add(station1);
        for (Route route : compoundRoutes) {
            Station lastStationTemp = intermediateStations.get(intermediateStations.size() - 1);
            intermediateStations.add(route.stationOpposite(lastStationTemp));
        }

         String namesIntermediateStations =
                String.join(
                        " - ",
                        intermediateStations.stream()
                                .map(Station::name)
                                .collect(Collectors.toCollection(ArrayList::new)));
        return String.format(
                "%s (%s)",
                namesIntermediateStations, length);
    }

    /**
     * Gets compoundRoutes. can be null.
     *
     * @return value of compoundRoutes
     */
    private List<Route> compoundRoutes() {
        return compoundRoutes;
    }
}
