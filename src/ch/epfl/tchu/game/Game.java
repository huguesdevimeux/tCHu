package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.gui.Info;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Represents a game of tCHu (aka les Aventuriers du Rail but shhh).
 *
 * @author Luca Mouchel (324748)
 * @author Hugues Devimeux (327282)
 */
public final class Game {
    /** Not instantiable. */
    private Game() {}

    /**
     * Method that makes the two <code>players</code> play the game.
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
        PlayerId firstPlayer = gameState.currentPlayerId();
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
        // we then have to remove the top tickets from the deck of tickets - ie remove the top 2*5
        // tickets from the deck as each player is handed 5 tickets
        gameState.withoutTopTickets(2 * Constants.INITIAL_TICKETS_COUNT);
        updatePlayerStates(players, gameState, gameState.currentPlayerState());
        // from these 5 tickets, each player chooses their initial tickets
        players.forEach((playerId, player) -> player.chooseInitialTickets());
        receiveNewInfo(players, currentPlayer, "choose initial tickets");

        // finally, the game can start, the players receive the info that the currentPlayer can play
        players.forEach((playerId, both) -> both.receiveInfo(currentPlayer.canPlay()));

        while (!gameState.lastTurnBegins()) {
            // the following part represents the "mid-game" (ie each turn until the last round
            // begins)
            for (Map.Entry<PlayerId, Player> player : players.entrySet()) {
                // representing the player as the key of Map player to be able to call the necessary
                // methods
                Player p = player.getValue();
                updatePlayerStates(players, gameState, gameState.currentPlayerState());

                // following switch statement describes the possible actions
                // at each turn of the game
                switch (p.nextTurn()) {
                    case DRAW_TICKETS:
                        receiveNewInfo(players, currentPlayer, "drew tickets");

                        // take the three first of the tickets pile
                        SortedBag<Ticket> retainedTickets =
                                p.chooseTickets(
                                        gameState.topTickets(Constants.IN_GAME_TICKETS_COUNT));

                        // removing the 3 top tickets from the tickets deck
                        gameState.withoutTopTickets(Constants.IN_GAME_TICKETS_COUNT);
                        players.forEach(
                                (playerId, both) ->
                                        both.receiveInfo(
                                                currentPlayer.keptTickets(retainedTickets.size())));
                        // next round can begin
                        nextRound(gameState, players, currentPlayer, nextPlayer);
                        break;

                    case DRAW_CARDS:
                        // the player only draws two cards
                        int totalNumberOfPossibleCardsToDraw = 2;
                        int indexOfChosenCard = p.drawSlot();
                        for (int i = 0; i < totalNumberOfPossibleCardsToDraw; i++) {
                            // method drawSlot returns -1 if the player picks a card from the deck
                            // of cards or a number between 0 and 4 if one of the faceUp cards
                            if (indexOfChosenCard == -1) {
                                receiveNewInfo(players, currentPlayer, "drew blind card");
                                gameState.withBlindlyDrawnCard();
                                // if we pick a blind card - we have to remove a card from the deck
                                gameState.withoutTopCard();
                            } else {
                                receiveNewInfo(players, currentPlayer, "drew visible card");
                                gameState.withDrawnFaceUpCard(indexOfChosenCard);
                            }
                            gameState.withCardsDeckRecreatedIfNeeded(rng);
                            // we update the playerStates after the first card is drawn
                            updatePlayerStates(players, gameState, gameState.currentPlayerState());
                        }
                        nextRound(gameState, players, currentPlayer, nextPlayer);
                        break;

                    case CLAIM_ROUTE:
                        Route claimedRoute = p.claimedRoute();
                        SortedBag<Card> initialClaimCards = p.initialClaimCards();
                        List<Card> drawnCards = new ArrayList<>();
                        boolean canClaimRoute =
                                gameState.currentPlayerState().canClaimRoute(claimedRoute);

                        // in either case: if the claimed route is overground or underground - both
                        // players receive the info that the current played has claimed route
                        receiveNewInfo(
                                players,
                                currentPlayer,
                                claimedRoute,
                                initialClaimCards,
                                "claimed route");
                        if (canClaimRoute) {
                            if (claimedRoute.level().equals(Route.Level.OVERGROUND)) {
                                // adding the claimed route to the current player's claimed routes
                                gameState.withClaimedRoute(claimedRoute, initialClaimCards);
                                gameState.currentPlayerState().routes().add(claimedRoute);
                                // add the cards the player took the route with to the discards pile
                                gameState.withMoreDiscardedCards(initialClaimCards);
                            } else {
                                // player must choose which additional cards he wants to play when
                                // he attempts to claim tunnel and drawn cards
                                // contains one of the initial claim cards
                                SortedBag<Card> additionalCardsToPlay =
                                        p.chooseAdditionalCards(List.of(initialClaimCards));
                                // in case we need the drawn cards for an attempt to claim a tunnel
                                // we add the THREE top deck cards to the drawn cards because when
                                // attempting to claim a tunnel, only three cards are drawn
                                for (int i = 0; i < Constants.ADDITIONAL_TUNNEL_CARDS; i++) {
                                    drawnCards.add(gameState.topCard());
                                    gameState.withoutTopCard();
                                    gameState.withCardsDeckRecreatedIfNeeded(rng);
                                }
                                // the available cards the player can pick from to choose additional
                                // cards are the claimCards
                                p.chooseAdditionalCards(List.of(initialClaimCards));
                                receiveNewInfo(
                                        players,
                                        currentPlayer,
                                        claimedRoute,
                                        SortedBag.of(initialClaimCards),
                                        "attempt to claim tunnel");
                                // if additional cards to play is empty - it means the player
                                // doesn't want to take the tunnel - or he simply can't
                                if (additionalCardsToPlay.isEmpty()) {
                                    receiveNewInfo(
                                            players,
                                            currentPlayer,
                                            claimedRoute,
                                            SortedBag.of(),
                                            "did not claim route");
                                    // we add the drawn cards to the discards
                                    gameState.withMoreDiscardedCards(SortedBag.of(drawnCards));
                                } else {
                                    players.forEach(
                                            (playerId, allPlayers) ->
                                                    allPlayers.receiveInfo(
                                                            currentPlayer.drewAdditionalCards(
                                                                    SortedBag.of(drawnCards),
                                                                    claimedRoute
                                                                            .additionalClaimCardsCount(
                                                                                    initialClaimCards,
                                                                                    SortedBag.of(
                                                                                            drawnCards)))));

                                    // adding the claimed route to the current player's list of
                                    // routes however we have to take into account the fact the
                                    // player played the initialClaimCards and had to play
                                    // additional cards. Moreover, the drawn cards must not be
                                    // forgotten. We have to sum up all the cards played
                                    SortedBag<Card> cardsPlayedForTunnelClaim =
                                            initialClaimCards.union(additionalCardsToPlay);
                                    gameState.withClaimedRoute(
                                            claimedRoute, cardsPlayedForTunnelClaim);
                                    gameState.withMoreDiscardedCards(
                                            cardsPlayedForTunnelClaim.union(
                                                    SortedBag.of(drawnCards)));
                                }
                            }
                        }
                        nextRound(gameState, players, currentPlayer, nextPlayer);
                        break;
                }
            }
        }
        Trail longestForCurrentPlayer = Trail.longest(gameState.playerState(firstPlayer).routes());
        Trail longestForNextPlayer =
                Trail.longest(gameState.playerState(firstPlayer.next()).routes());

        if (longestForCurrentPlayer.length() > longestForNextPlayer.length()) {
            longestTrailBonus(players, currentPlayer, longestForCurrentPlayer);
        } else if (longestForCurrentPlayer.length() < longestForNextPlayer.length()) {
            longestTrailBonus(players, nextPlayer, longestForNextPlayer);
        }
        updatePlayerStates(players, gameState, gameState.currentPlayerState());

        int winnerTotalPoints;
        int loserTotalPoints;
        // when the last turn begins the last player is said to be the currentPlayer so we can use
        // currentPlayer's finalPoints
        if (longestForCurrentPlayer.length() > longestForNextPlayer.length()) {
            winnerTotalPoints =
                    gameState.currentPlayerState().finalPoints()
                            + Constants.LONGEST_TRAIL_BONUS_POINTS;
            loserTotalPoints = gameState.playerState(firstPlayer.next()).finalPoints();
        } else {
            winnerTotalPoints = gameState.currentPlayerState().finalPoints();
            loserTotalPoints =
                    gameState.playerState(firstPlayer.next()).finalPoints()
                            + Constants.LONGEST_TRAIL_BONUS_POINTS;
        }
        players.forEach(
                (playerId, player) ->
                        player.receiveInfo(currentPlayer.won(winnerTotalPoints, loserTotalPoints)));
    }
    // private methods created to compress code in main method of this class
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

            case "drew tickets":
                players.forEach(
                        (playerId, both) ->
                                both.receiveInfo(
                                        currentPlayer.drewTickets(
                                                Constants.IN_GAME_TICKETS_COUNT)));

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
                break;
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
                break;
        }
    }

    private static void nextRound(
            GameState gameState,
            Map<PlayerId, Player> players,
            Info currentPlayer,
            Info nextPlayer) {
        if (gameState.lastTurnBegins()) {
            players.forEach(
                    (playerId, both) ->
                            both.receiveInfo(
                                    currentPlayer.lastTurnBegins(
                                            gameState.currentPlayerState().carCount())));

        } else {
            gameState.forNextTurn();
            players.forEach((playerId, both) -> both.receiveInfo(nextPlayer.canPlay()));
        }
    }

    private static void updatePlayerStates(
            Map<PlayerId, Player> players, PublicGameState gameState, PlayerState playerState) {
        players.forEach((playerId, player) -> player.updateState(gameState, playerState));
    }

    private static void longestTrailBonus(
            Map<PlayerId, Player> players, Info playerIdentity, Trail longest) {
        players.forEach(
                (playerId, player) ->
                        player.receiveInfo(playerIdentity.getsLongestTrailBonus(longest)));
    }
}
