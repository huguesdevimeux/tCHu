package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.Card;
import ch.epfl.tchu.game.Route;
import ch.epfl.tchu.game.Trail;

import java.util.Collections;
import java.util.List;

/**
 * Handle generation of messages describing the game. Immutable.
 *
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public final class Info {
    private final String playerName;

    /**
     * Constructs a message generator for the given player's name.
     *
     * @param playerName The player's name.
     */
    public Info(String playerName) {
        this.playerName = playerName;
    }

    /**
     * Returns the name (french) of the given card, singular iff the absolute value of count is 1.
     *
     * @param card the card to be represented.
     * @param count 1 if singular.
     * @return String representation of the card
     */
    public static String cardName(Card card, int count) {
        // This is slightly hacky, and will get the the attribute named card.name()_CARD (eg
        // BLACK_CARD) from StringsFr.
        try {
            return StringsFr.class.getDeclaredField(card.name() + "_CARD").get(null)
                    + StringsFr.plural(count);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Card's name is not register for " + card.name());
        }
    }

    /**
     * Returns the message saying that the given players finished the game ex-aeqo.
     *
     * @param playerNames The players names.
     * @param points The point shared by the players.
     * @return The message.
     */
    public static String draw(List<String> playerNames, int points) {
        return String.format(StringsFr.DRAW, String.join(", ", playerNames), points);
    }

    private static String displaySortedBagOfCards(SortedBag<Card> cards) {
        List<String> displayed = new java.util.ArrayList<>(Collections.emptyList());
        // Create a list of string with alternated each pieceCard and a comma.
        cards.toMap()
                .forEach(
                        (card, number) -> {
                            String cardStringPiece =
                                    String.format("%s %s", number, cardName(card, number));
                            displayed.addAll(List.of(cardStringPiece, ", "));
                        });
        // Change the before last comma to "and".
        displayed.set(displayed.size() - 3, StringsFr.AND_SEPARATOR);
        // Remove the last comma.
        displayed.remove(displayed.size() - 1);
        return String.join("", displayed);
    }

    private static String displayRoute(Route route) {
        return String.format(
                "%s %s %s",
                route.station1().name(), StringsFr.EN_DASH_SEPARATOR, route.station2().name());
    }

    /**
     * Returns the message stating that the player will play first.
     *
     * @return Message saying that the current player will be playing first.
     */
    public String willPlayFirst() {
        return String.format(StringsFr.WILL_PLAY_FIRST, this.playerName);
    }

    /**
     * Returns the message saying that the player has kept the given amount of tickets.
     *
     * @param count The amount of tickets.
     * @return the message saying that the player has kept the given amount of tickets.
     */
    public String keptTickets(int count) {
        return String.format(
                StringsFr.KEPT_N_TICKETS, this.playerName, count, StringsFr.plural(count));
    }

    /**
     * Returns the message saying the player can play.
     *
     * @return the message saying the player can play.
     */
    public String canPlay() {
        return String.format(StringsFr.CAN_PLAY, this.playerName);
    }

    /**
     * Returns the message saying the player has drawn the given number of tickets.
     *
     * @param count The number of cards.
     * @return The message.
     */
    public String drewTickets(int count) {
        return String.format(
                StringsFr.DREW_TICKETS, this.playerName, count, StringsFr.plural(count));
    }

    /**
     * Returns a message saying that the player has drew a card "blindly" (from the deck).
     *
     * @return The message.
     */
    public String drewBlindCard() {
        return String.format(StringsFr.DREW_BLIND_CARD, this.playerName);
    }

    /**
     * Returns the message saying that the player has drew the visible card.
     *
     * @param card the card drawn.
     * @return The message.
     */
    public String drewVisibleCard(Card card) {
        return String.format(StringsFr.DREW_VISIBLE_CARD, this.playerName, cardName(card, 1));
    }

    /**
     * Returns the message saying the player has taken the given route using given cards.
     *
     * @param route The route taken by the player.
     * @param cards The cards.
     * @return The message.
     */
    public String claimedRoute(Route route, SortedBag<Card> cards) {
        return String.format(
                StringsFr.CLAIMED_ROUTE,
                this.playerName,
                displayRoute(route),
                displaySortedBagOfCards(cards));
    }

    /**
     * Returns the message saying that the player wants to take the given tuennel using the given
     * cards.
     *
     * @param route The route the player wants to take.
     * @param initialCards The cards the player wants to take the tunnel with.
     * @return The message.
     */
    public String attemptsTunnelClaim(Route route, SortedBag<Card> initialCards) {
        return String.format(
                StringsFr.ATTEMPTS_TUNNEL_CLAIM,
                this.playerName,
                displayRoute(route),
                displaySortedBagOfCards(initialCards));
    }

    /**
     * Returns the message saying that the player has drew the three additional cards and that they
     * imply an additional cost. Note : the message change depending on the addionalCost is 0 or
     * not.
     *
     * @param drawnCards The drawn cards.
     * @param additionalCost The additional cards.
     * @return the message.
     */
    public String drewAdditionalCards(SortedBag<Card> drawnCards, int additionalCost) {
        StringBuilder s =
                new StringBuilder(
                        String.format(
                                StringsFr.ADDITIONAL_CARDS_ARE,
                                displaySortedBagOfCards(drawnCards)));
        if (additionalCost == 0) s.append(StringsFr.NO_ADDITIONAL_COST);
        else
            s.append(
                    String.format(
                            StringsFr.SOME_ADDITIONAL_COST,
                            additionalCost,
                            StringsFr.plural(additionalCost)));
        return s.toString();
    }

    /**
     * Returns the message saying the player couldn't (or didn't want to) take the given tunnel.
     *
     * @param route The tunnel (a route).
     * @return the message.
     */
    public String didNotClaimRoute(Route route) {
        return String.format(StringsFr.DID_NOT_CLAIM_ROUTE, this.playerName, displayRoute(route));
    }

    /**
     * Returns the message saying that the player has only carCount (<=2) of wagons, and that the
     * last turn starts.
     *
     * @param carCount Number of wagons (cars) left.
     * @return the message.
     */
    public String lastTurnBegins(int carCount) {
        return String.format(
                StringsFr.LAST_TURN_BEGINS, this.playerName, carCount, StringsFr.plural(carCount));
    }

    /**
     * Returns the message saying that the player gets the end-game bonus thanks to the trail given
     * (the / one of the longest).
     *
     * @param longestTrail The trail with which the player gets the bonus.
     * @return the message.
     */
    public String getsLongestTrailBonus(Trail longestTrail) {
        String trailString =
                String.format(
                        "%s %s %s",
                        longestTrail.station1().name(),
                        StringsFr.EN_DASH_SEPARATOR,
                        longestTrail.station2().name());
        return String.format(StringsFr.GETS_BONUS, this.playerName, trailString);
    }

    /**
     * Returns the message saying the player wins the game with the given amount of points, when
     * their opponent has collected loserPoints.
     *
     * @param points The points.
     * @param loserPoints The points of the loser.
     * @return The message.
     */
    public String won(int points, int loserPoints) {
        return String.format(
                StringsFr.WINS,
                this.playerName,
                points,
                StringsFr.plural(points),
                loserPoints,
                StringsFr.plural(loserPoints));
    }
}
