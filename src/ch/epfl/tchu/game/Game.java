package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.gui.Info;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/** Represents a game of tCHu (aka les Aventuriers du Rail but shhh). */
public final class Game {

    /** Not instantiable. */
    private Game() {}

    /**
     * Method that makes the two <code>players</code> play.
     *
     * @param players the two players in the game
     * @param playerNames name of the two players
     * @param tickets bag of tickets
     * @param rng random element
     */
    public static void play(
            Map<PlayerId, Player> players,
            Map<PlayerId, String> playerNames,
            SortedBag<Ticket> tickets,
            Random rng) {
        GameState gameState = GameState.initial(tickets, rng);
        // selecting the first player at random and representing him as the current player
        PlayerId firstPlayer = PlayerId.ALL.get(rng.nextInt(PlayerId.ALL.size()));
        Info currentPlayer = new Info(firstPlayer.name());
        Info nextPlayer = new Info(firstPlayer.next().name());
        Preconditions.checkArgument(players.size() == 2 && playerNames.size() == 2);
        // initialising both players
        players.forEach((playerId, player) -> player.initPlayers(playerId, playerNames));

        receiveNewInfo(players, currentPlayer, "will play first");
        // at the beginning of the game, each player is given a set of initial tickets -
        // from which they have to choose their initial tickets -
        // hence the fact we call these methods for each player
        players.forEach(
                (playerId, player) ->
                        player.setInitialTicketChoice(
                                gameState.topTickets(Constants.INITIAL_TICKETS_COUNT)));
        players.forEach((playerId, player) -> player.chooseInitialTickets());
        receiveNewInfo(players, currentPlayer, "choose initial tickets");

        // the following part represents the "mid-game" (ie each turn until the last round begins)
        for (Map.Entry<PlayerId, Player> player : players.entrySet()) {
            // representing the player as the key of Map player to be able to call the necessary
            // methods
            Player p = player.getValue();
            p.receiveInfo(currentPlayer.canPlay());
            CardState cardState = CardState.of(Deck.of(Constants.ALL_CARDS, new Random()));
            // following switch statement describes the possible actions to take at each turn
            switch (p.nextTurn()) {
                case DRAW_TICKETS:
                    p.receiveInfo(currentPlayer.drewTickets(Constants.IN_GAME_TICKETS_COUNT));
                    // take the three first of the tickets pile
                    SortedBag<Ticket> retainedTickets =
                            p.chooseTickets(
                                    SortedBag.of(
                                            tickets.toList()
                                                    .subList(0, Constants.IN_GAME_TICKETS_COUNT)));
                    // removing the retained tickets from the pile of tickets
                    gameState.withoutTopTickets(retainedTickets.size());
                    p.receiveInfo(currentPlayer.keptTickets(retainedTickets.size()));
                    players.forEach((Id, both) -> both.receiveInfo(nextPlayer.canPlay()));
                    gameState.forNextTurn();
                    break;

                case DRAW_CARDS:
                    // the player only draws two cards
                    int totalNumberOfPossibleCardsToDraw = 2;
                    for (int i = 0; i < totalNumberOfPossibleCardsToDraw; i++) {
                        // method drawslot returns -1 if the player picks a card from the deck of
                        // cards
                        if (p.drawSlot() == -1)
                            receiveNewInfo(players, currentPlayer, "drew blind card");
                        else receiveNewInfo(players, currentPlayer, "drew visible card");
                    }
                    players.forEach((playerId, both) -> both.receiveInfo(nextPlayer.canPlay()));
                    gameState.forNextTurn();
                    break;

                case CLAIM_ROUTE:
                    Route claimedRoute = p.claimedRoute();
                    SortedBag<Card> initialClaimCards = p.initialClaimCards();
                    List<Card> drawnCards = new ArrayList<>();
                    // player must choose which additional cards he wants to play when he attempts
                    // to claim tunnel and drawn cards contains one of the initial claim cards
                    SortedBag<Card> additionalCardsToPlay =
                            p.chooseAdditionalCards(List.of(initialClaimCards));

                    // in case we need the drawn cards for an attempt to claim a tunnel
                    // we add the THREE top deck cards to the drawn cards as when attempting to
                    // claim a tunnel, only three cards are drawn
                    for (int i = 0; i < Constants.ADDITIONAL_TUNNEL_CARDS; i++) {
                        drawnCards.add(cardState.topDeckCard());
                        cardState.withoutTopDeckCard();
                    }

                    // total list of cards to play if the player must play additional cards
                    List<Card> initialAndAdditionalCards =
                            initialClaimCards.toList().stream()
                                    .filter(additionalCardsToPlay.toList()::add)
                                    .collect(Collectors.toList());

                    if (claimedRoute.level().equals(Route.Level.OVERGROUND)) {
                        receiveNewInfo(
                                players,
                                currentPlayer,
                                claimedRoute,
                                initialClaimCards,
                                "claimed route");
                    } else {
                        p.chooseAdditionalCards(List.of(additionalCardsToPlay));
                        receiveNewInfo(
                                players,
                                currentPlayer,
                                claimedRoute,
                                SortedBag.of(initialAndAdditionalCards),
                                "attempt to claim tunnel");
                    }
                    players.forEach(
                            (playerId, allPlayers) ->
                                    allPlayers.receiveInfo(
                                            currentPlayer.drewAdditionalCards(
                                                    SortedBag.of(drawnCards),
                                                    claimedRoute.additionalClaimCardsCount(
                                                            initialClaimCards,
                                                            SortedBag.of(drawnCards)))));
                    // if additional cards to play is empty - it means the player doesn't want to
                    // take the tunnel - or he simply can't
                    if (additionalCardsToPlay.isEmpty())
                        receiveNewInfo(
                                players,
                                currentPlayer,
                                claimedRoute,
                                SortedBag.of(),
                                "did not claim route");

                    players.forEach((Id, both) -> both.receiveInfo(nextPlayer.canPlay()));
                    break;
            }
        }
    }

    // private methods i created to compress code in main method of this class
    // using strings instead of ints as a selection in the switch statements to provide a minimum
    // of description
    private static void receiveNewInfo(
            Map<PlayerId, Player> players, Info currentPlayer, String s) {
        switch (s) {
            case "will play first":
                players.forEach(
                        (playerId, player) -> player.receiveInfo(currentPlayer.willPlayFirst()));
                break;
            case "choose initial tickets":
                players.forEach(
                        (playerId, player) ->
                                player.receiveInfo(
                                        currentPlayer.keptTickets(
                                                // tickets.size()? or:
                                                Constants.INITIAL_TICKETS_COUNT
                                                        - player.chooseInitialTickets().size())));
                break;
            case "drew blind card":
                players.forEach(
                        (playerId, allPlayers) ->
                                allPlayers.receiveInfo(currentPlayer.drewBlindCard()));
                break;

            case "drew visible card":
                CardState a = CardState.of(Deck.of(Constants.ALL_CARDS, new Random()));
                players.forEach(
                        (playerId, allPlayers) ->
                                allPlayers.receiveInfo(
                                        currentPlayer.drewVisibleCard(a.topDeckCard())));
        }
    }

    private static void receiveNewInfo(
            Map<PlayerId, Player> players,
            Info currentPlayer,
            Route claimedRoute,
            SortedBag<Card> cards,
            String s) {
        switch (s) {
            case "claimed route":
                players.forEach(
                        (playerId, allPlayers) ->
                                allPlayers.receiveInfo(
                                        currentPlayer.claimedRoute(claimedRoute, cards)));
                break;
            case "attempt to claim tunnel":
                players.forEach(
                        (playerId, allPlayers) ->
                                allPlayers.receiveInfo(
                                        currentPlayer.attemptsTunnelClaim(
                                                claimedRoute, SortedBag.of(cards))));
                break;
            case "did not claim route":
                players.forEach(
                        (playerId, player) ->
                                player.receiveInfo(currentPlayer.didNotClaimRoute(claimedRoute)));
        }
    }
}
