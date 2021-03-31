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
        GameState gameStateCopy = gameState;
        players.forEach(
                (playerId, player) ->
                        player.setInitialTicketChoice(
                                gameStateCopy.topTickets(Constants.INITIAL_TICKETS_COUNT)));
        // first player gets delivered the top 5 tickets
        gameState =
                gameState.withInitiallyChosenTickets(
                        firstPlayer, gameState.topTickets(Constants.INITIAL_TICKETS_COUNT));
        // we have to remove the top tickets from the deck of tickets
        gameState = gameState.withoutTopTickets(Constants.INITIAL_TICKETS_COUNT);
        // do this for second player too
        gameState =
                gameState.withInitiallyChosenTickets(
                        firstPlayer.next(), gameState.topTickets(Constants.INITIAL_TICKETS_COUNT));
        gameState = gameState.withoutTopTickets(Constants.INITIAL_TICKETS_COUNT);

        updatePlayerStates(players, gameState, gameState.currentPlayerState());
        // from these 5 tickets, each player chooses their initial tickets
        players.forEach((playerId, player) -> player.chooseInitialTickets());
        receiveNewInfo(players, currentPlayer, "choose initial tickets");

        // finally, the game can start, the players receive the info that the currentPlayer can play
        players.forEach((playerId, both) -> both.receiveInfo(currentPlayer.canPlay()));

        // the following part represents the "mid-game" (ie each turn until the last round
        // begins)
        while (!gameState.lastTurnBegins()) {
            // representing the player as the key of Map player to be able to call the necessary
            // methods
            Player playerChoice = players.get(gameState.currentPlayerId());
            updatePlayerStates(players, gameState, gameState.currentPlayerState());

            // following switch statement describes the possible actions
            // at each turn of the game
            switch (playerChoice.nextTurn()) {
                case DRAW_TICKETS:
                    if (gameState.canDrawTickets()) {
                        receiveNewInfo(players, currentPlayer, "drew tickets");
                        SortedBag<Ticket> topTicketsInGame =
                                gameState.topTickets(Constants.IN_GAME_TICKETS_COUNT);

                        // take the three first of the tickets pile
                        SortedBag<Ticket> retainedTickets =
                                playerChoice.chooseTickets(topTicketsInGame);
                        gameState =
                                gameStateCopy.withChosenAdditionalTickets(
                                        topTicketsInGame, retainedTickets);

                        // removing the 3 top tickets from the tickets deck
                        gameState = gameState.withoutTopTickets(Constants.IN_GAME_TICKETS_COUNT);
                        players.forEach(
                                (playerId, both) ->
                                        both.receiveInfo(
                                                currentPlayer.keptTickets(retainedTickets.size())));
                        // TODO - not quite sure what to do if he can't draw tickets - does the
                        // current players turn restart??
                    } else continue;
                    // next round can begin
                    nextRound(gameState, players, currentPlayer, nextPlayer);
                    break;

                case DRAW_CARDS:
                    // the player only draws two cards
                    int totalNumberOfPossibleCardsToDraw = 2;

                    for (int i = 0; i < totalNumberOfPossibleCardsToDraw; i++) {
                        // if there aren't enough cards to begin with, we shuffle the bigboi
                        gameState = gameState.withCardsDeckRecreatedIfNeeded(rng);

                        if (gameState.canDrawCards()) {
                            int indexOfChosenCard = playerChoice.drawSlot();
                            // method drawSlot returns -1 if the player picks a card from the
                            // deck of cards or a number between 0 and 4 if one of the faceUp cards
                            if (indexOfChosenCard == Constants.DECK_SLOT) {
                                receiveNewInfo(players, currentPlayer, "drew blind card");
                                gameState = gameState.withBlindlyDrawnCard();
                                // if we pick a blind card - we have to remove a card from the
                                // deck
                                gameState = gameState.withoutTopCard();
                            } else {
                                receiveNewInfo(players, currentPlayer, "drew visible card");
                                gameState = gameState.withDrawnFaceUpCard(indexOfChosenCard);
                            }
                            gameState = gameState.withCardsDeckRecreatedIfNeeded(rng);
                            // we update the playerStates after the first card is drawn
                            updatePlayerStates(players, gameState, gameState.currentPlayerState());
                        }
                        // if canDrawCards is false, it means deck is too small - so we just
                        // re-shuffle the bigboi
                    }
                    nextRound(gameState, players, currentPlayer, nextPlayer);
                    break;

                case CLAIM_ROUTE:
                    Route claimedRoute = playerChoice.claimedRoute();
                    SortedBag<Card> initialClaimCards = playerChoice.initialClaimCards();
                    // drawnCards comes only into play when taking over a tunnel
                    List<Card> drawnCards = new ArrayList<>();
                    boolean canClaimRoute =
                            gameState.currentPlayerState().canClaimRoute(claimedRoute);

                    if (canClaimRoute) {
                        // in either case: if the claimed route is overground or underground - both
                        // players receive the info that the current played has claimed route
                        receiveNewInfo(
                                players,
                                currentPlayer,
                                claimedRoute,
                                initialClaimCards,
                                "claimed route");
                        if (claimedRoute.level().equals(Route.Level.OVERGROUND)) {
                            // adding the claimed route to the current player's claimed routes
                            gameState = gameState.withClaimedRoute(claimedRoute, initialClaimCards);
                        } else {
                            receiveNewInfo(
                                    players,
                                    currentPlayer,
                                    claimedRoute,
                                    SortedBag.of(initialClaimCards),
                                    "attempt to claim tunnel");
                            // in case we need the drawn cards for an attempt to claim a tunnel
                            // we add the THREE top deck cards to the drawn cards because when
                            // attempting to claim a tunnel, only three cards are drawn
                            for (int i = 0; i < Constants.ADDITIONAL_TUNNEL_CARDS; i++) {
                                gameState = gameState.withCardsDeckRecreatedIfNeeded(rng);
                                drawnCards.add(gameState.topCard());
                                gameState = gameState.withoutTopCard();
                            }
                            // player must choose which additional cards he wants to play when
                            // he attempts to claim tunnel and drawnCards
                            // contains one of the initial claim cards
                            SortedBag<Card> additionalCardsToPlay =
                                    playerChoice.chooseAdditionalCards(List.of(initialClaimCards));
                            // if additional cards to play is empty - it means the player
                            // doesn't want to take the tunnel - or he simply can't
                            if (additionalCardsToPlay.isEmpty()) {
                                receiveNewInfo(
                                        players,
                                        currentPlayer,
                                        claimedRoute,
                                        SortedBag.of(),
                                        "did not claim route");
                            } else {
                                int cardsToPlay =
                                        claimedRoute.additionalClaimCardsCount(
                                                initialClaimCards, SortedBag.of(drawnCards));
                                AdditionalCardsWereDrawnInfo(
                                        players, currentPlayer, drawnCards, cardsToPlay);

                                // adding the claimed route to the current player's list of
                                // routes however we have to take into account the fact the
                                // player played the initialClaimCards and had to play
                                // additional cards. Moreover, the drawn cards must not be
                                // forgotten. We have to sum up all the cards played
                                SortedBag<Card> cardsPlayedForTunnelClaim =
                                        initialClaimCards.union(additionalCardsToPlay);
                                // method withClaimedRoute already takes into account to add the
                                // cards to the discards
                                gameState =
                                        gameState.withClaimedRoute(
                                                claimedRoute, cardsPlayedForTunnelClaim);
                                // we only have to add the drawn cards to the discards
                            }
                            // we add the drawn cards to the discards
                            gameState =
                                    gameState.withMoreDiscardedCards(SortedBag.of(drawnCards));
                        }
                    }
                    nextRound(gameState, players, currentPlayer, nextPlayer);
                    break;
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

        int winnerPoints;
        int loserPoints;
        // when the last turn begins the last player is said to be the currentPlayer so we can use
        // currentPlayer's finalPoints
        if (longestForCurrentPlayer.length() > longestForNextPlayer.length()) {
            winnerPoints =
                    gameState.currentPlayerState().finalPoints()
                            + Constants.LONGEST_TRAIL_BONUS_POINTS;
            loserPoints = gameState.playerState(firstPlayer.next()).finalPoints();
        } else {
            winnerPoints = gameState.currentPlayerState().finalPoints();
            loserPoints =
                    gameState.playerState(firstPlayer.next()).finalPoints()
                            + Constants.LONGEST_TRAIL_BONUS_POINTS;
        }
        if (winnerPoints > loserPoints)
            currentPlayerWonInfo(players, currentPlayer, winnerPoints, loserPoints);
        else if (winnerPoints == loserPoints)
            playersHaveDrawnInfo(players, new ArrayList<>(playerNames.values()), winnerPoints);
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

    private static void AdditionalCardsWereDrawnInfo(
            Map<PlayerId, Player> p, Info cPlayer, List<Card> dCards, int aCards) {
        p.forEach(
                (playerId, player) ->
                        player.receiveInfo(
                                cPlayer.drewAdditionalCards(SortedBag.of(dCards), aCards)));
    }

    private static void currentPlayerWonInfo(
            Map<PlayerId, Player> players, Info currentPlayer, int winnerPoints, int loserPoints) {
        players.forEach(
                (playerId, player) ->
                        player.receiveInfo(currentPlayer.won(winnerPoints, loserPoints)));
    }

    private static void playersHaveDrawnInfo(
            Map<PlayerId, Player> players, List<String> playerNames, int points) {
        players.forEach((playerId, player) -> player.receiveInfo(Info.draw(playerNames, points)));
    }

    private static void nextRound(
            GameState gameState,
            Map<PlayerId, Player> players,
            Info currentPlayer,
            Info nextPlayer) {
        if (gameState.lastTurnBegins()) {
            GameState finalGameState = gameState;
            players.forEach(
                    (playerId, both) ->
                            both.receiveInfo(
                                    currentPlayer.lastTurnBegins(
                                            finalGameState.currentPlayerState().carCount())));

        } else {
            gameState = gameState.forNextTurn();
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
