package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.gui.Info;

import java.util.*;

/**
 * Represents a game of tCHu (aka les Aventuriers du Rail but shhh).
 *
 * @author Luca Mouchel (324748)
 * @author Hugues Devimeux (327282)
 */
public final class Game {
    private static final Map<PlayerId, Info> playersInfo = new HashMap<>();
    private static GameState gameState;

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
        gameState = GameState.initial(tickets, rng);
        Preconditions.checkArgument(players.size() == 2 && playerNames.size() == 2);

        // putting all of the elements of playerNames in a map where values are of class Info
        // for receiving infos purposes
        playerNames.forEach(
                (playerId, name) -> playersInfo.put(playerId, new Info(playerNames.get(playerId))));

        beginGame(players, playerNames, playersInfo.get(gameState.currentPlayerId()));

        System.out.println(gameState.currentPlayerState().carCount());
        // the following part represents the "mid-game" (ie each turn until the last round
        // begins)

        while ((gameState.lastPlayer() != gameState.currentPlayerId())) {
            gameState = nextTurn(players);
            Player currentPlayer = players.get(gameState.currentPlayerId());
            System.out.printf("%s is playing%n", playerNames.get(gameState.currentPlayerId()));
            updatePlayerStates(players, gameState);
            Player.TurnKind turnKindChosenByCurrentPlayer = currentPlayer.nextTurn();
            System.out.printf(
                    "%s has %s car(s) left.%n",
                    playerNames.get(gameState.currentPlayerId()),
                    gameState.currentPlayerState().carCount());
            System.out.printf(
                    "%s chooses to do %s%n",
                    playerNames.get(gameState.currentPlayerId()), turnKindChosenByCurrentPlayer);

            switch (turnKindChosenByCurrentPlayer) {
                case DRAW_TICKETS:
                    TurnHandler.drawTickets(
                            players, currentPlayer, playersInfo.get(gameState.currentPlayerId()));
                    break;
                case DRAW_CARDS:
                    TurnHandler.drawCards(
                            players,
                            currentPlayer,
                            playersInfo.get(gameState.currentPlayerId()),
                            rng);
                    break;
                case CLAIM_ROUTE:
                    TurnHandler.claimRoute(
                            players,
                            currentPlayer,
                            playersInfo.get(gameState.currentPlayerId()),
                            rng);
                    break;
            }
            System.out.println("Done playing.\n");
            System.out.println("Turn over.");
        }

        endGame(players, playerNames);
    }

    private static void beginGame(
            Map<PlayerId, Player> players,
            Map<PlayerId, String> playerNames,
            Info currentPlayerInfo) {

        // initialising both players
        players.forEach((playerId, player) -> player.initPlayers(playerId, playerNames));
        ReceiveInfoHandler.willPlayerFirst(players, currentPlayerInfo);

        players.forEach(
                (playerId, player) -> {
                    setInitialTicketsChoices(players, playerId);
                    updatePlayerStates(players, gameState);
                    player.chooseInitialTickets();
                });

        // from these 5 tickets, each player chooses their initial tickets
        playersInfo.forEach(
                (ignored, playerInfo) ->
                        ReceiveInfoHandler.chooseInitialTicketsInfo(players, playerInfo));
        // finally, the game can start, the players receive the info that the current player can
        // play
        players.forEach((ignored, player) -> player.receiveInfo(currentPlayerInfo.canPlay()));
    }

    /**
     * Deals with the ticket management at the beginning. The player receives a set of initial cards
     * (5 top tickets) and must pick at least three.
     *
     * @param players use it to <code>setInitialTicketChoice</code> to the player in question
     * @param playerId the player in question
     */
    private static void setInitialTicketsChoices(Map<PlayerId, Player> players, PlayerId playerId) {
        // player gets delivered the top 5 tickets
        SortedBag<Ticket> playerTickets = gameState.topTickets(Constants.INITIAL_TICKETS_COUNT);
        // the player then chooses the tickets they want to keep
        gameState = gameState.withInitiallyChosenTickets(playerId, playerTickets);
        players.get(playerId).setInitialTicketChoice(playerTickets);
        // we have to remove the top tickets from the deck of tickets
        gameState = gameState.withoutTopTickets(Constants.INITIAL_TICKETS_COUNT);
    }

    /**
     * Deals with the next turn. Calls <code>forNextTurn</code> in gameState and both players
     * receive info that the next player can play.
     *
     * @param players the players in the game
     * @return a new gameState with the next player that will play
     */
    private static GameState nextTurn(Map<PlayerId, Player> players) {
        // the next player will become the current player in relation to the informations received
        if (gameState.lastTurnBegins()) {
            ReceiveInfoHandler.lastTurnBegins(
                    gameState, players, playersInfo.get(gameState.currentPlayerId()));
        }
        gameState = gameState.forNextTurn();
        // now the next player becomes the current player so both players receive the info that the
        // current player can play
        players.forEach(
                (playerId, player) ->
                        player.receiveInfo(
                                (playersInfo.get(gameState.currentPlayerId())).canPlay()));
        return gameState;
    }

    // used to update the player of the states
    private static void updatePlayerStates(Map<PlayerId, Player> players, GameState gameState) {
        // TODO remove the last parameter playerStates
        players.forEach(
                (playerId, player) ->
                        player.updateState(gameState, gameState.playerState(playerId)));
    }

    private static void endGame(Map<PlayerId, Player> players, Map<PlayerId, String> playerNames) {

        Map.Entry<PlayerId, Trail> longestTrail =
                players.keySet().stream()
                        .map(
                                player ->
                                        Map.entry(
                                                player,
                                                Trail.longest(
                                                        gameState.playerState(player).routes())))
                        .max(Comparator.comparingInt(o -> o.getValue().length()))
                        .orElse(null);

        List<Map.Entry<PlayerId, Integer>> sortedPoints = new ArrayList<>();
        for (PlayerId player : players.keySet()) {
            int amountOfPoints = gameState.playerState(player).finalPoints();
            if (player.equals(longestTrail.getKey())) {
                amountOfPoints += Constants.LONGEST_TRAIL_BONUS_POINTS;
            }
            sortedPoints.add(Map.entry(player, amountOfPoints));
        }
        sortedPoints.sort(Map.Entry.comparingByValue());

        // just to tell Intellij to STFU
        assert longestTrail != null;
        ReceiveInfoHandler.longestTrail(
                players, playersInfo.get(longestTrail.getKey()), longestTrail.getValue());
        updatePlayerStates(players, gameState);
        int winnerPoints =
                sortedPoints
                        .get(sortedPoints.size() - 1)
                        .getValue(); // pointsOfEachPlayerSorted.lastEntry().getValue();
        PlayerId winnerId = sortedPoints.get(sortedPoints.size() - 1).getKey();
        int loserPoints = sortedPoints.get(0).getValue();
        if (winnerPoints != loserPoints)
            ReceiveInfoHandler.playerWon(
                    players, playersInfo.get(winnerId), winnerPoints, loserPoints);
        else
            ReceiveInfoHandler.playersHaveDrawn(
                    players, new LinkedList<>(playerNames.values()), winnerPoints);
    }

    /** Handles the different turns logic. */
    private static class TurnHandler {
        public static void drawTickets(
                Map<PlayerId, Player> players, Player currentPlayer, Info currentPlayerInfo) {
            if (gameState.canDrawTickets()) {
                ReceiveInfoHandler.drewTickets(players, currentPlayerInfo);
                SortedBag<Ticket> topTicketsInGame =
                        gameState.topTickets(Constants.IN_GAME_TICKETS_COUNT);

                // take the three first of the tickets pile
                SortedBag<Ticket> retainedTickets = currentPlayer.chooseTickets(topTicketsInGame);
                // the following method already removes top tickets so we don't
                // have to take care of it
                gameState =
                        gameState.withChosenAdditionalTickets(topTicketsInGame, retainedTickets);
                players.forEach(
                        (playerId, player) ->
                                player.receiveInfo(
                                        currentPlayerInfo.keptTickets(retainedTickets.size())));
            } // else nothing
            // https://discord.com/channels/807922527716114432/807922528310788110/826799128306384926
        }

        public static void drawCards(
                Map<PlayerId, Player> players,
                Player currentPlayer,
                Info currentPlayerInfo,
                Random rng) {
            // the player only draws two cards
            int totalNumberOfPossibleCardsToDraw = 2;

            for (int i = 0; i < totalNumberOfPossibleCardsToDraw; i++) {
                // if there aren't enough cards to begin with, we shuffle the bigboi
                gameState = gameState.withCardsDeckRecreatedIfNeeded(rng);
                if (gameState.canDrawCards()) {
                    System.out.println("The player can draw cards !");
                    int indexOfChosenCard = currentPlayer.drawSlot();
                    // method drawSlot returns -1 if the player picks a card from the
                    // deck of cards or a number between 0 and 4 if one of the faceUp cards
                    if (indexOfChosenCard == Constants.DECK_SLOT) {
                        ReceiveInfoHandler.drewBlindCard(players, currentPlayerInfo);
                        gameState = gameState.withBlindlyDrawnCard();
                    } else {
                        ReceiveInfoHandler.drewVisibleCard(players, currentPlayerInfo);
                        gameState = gameState.withDrawnFaceUpCard(indexOfChosenCard);
                    }
                    gameState = gameState.withCardsDeckRecreatedIfNeeded(rng);
                    // we update the playerStates after the first card is drawn
                    updatePlayerStates(players, gameState);
                } else {
                    System.out.println("The player can't draw cards.");
                }
            }
        }

        public static void claimRoute(
                Map<PlayerId, Player> players,
                Player currentPlayer,
                Info currentPlayerInfo,
                Random rng) {
            Route claimedRoute = currentPlayer.claimedRoute();
            SortedBag<Card> initialClaimCards = currentPlayer.initialClaimCards();
            int amountOfCardsToPlay;
            List<Card> drawnCards = new ArrayList<>();
            List<SortedBag<Card>> possibleAdditionalCardsToPlay;
            SortedBag<Card> chosenCards;
            SortedBag<Card> cardsPlayedForTunnelClaim;
            boolean canClaimRoute = gameState.currentPlayerState().canClaimRoute(claimedRoute);

            if (canClaimRoute) {
                if (claimedRoute.level().equals(Route.Level.OVERGROUND)) {
                    // players receive the info that the current played has claimed route
                    ReceiveInfoHandler.claimedRoute(
                            players, currentPlayerInfo, claimedRoute, initialClaimCards);
                    // adding the claimed route to the current player's claimed routes
                    gameState = gameState.withClaimedRoute(claimedRoute, initialClaimCards);
                } else {
                    ReceiveInfoHandler.attemptedTunnelClaim(
                            players, currentPlayerInfo, claimedRoute, initialClaimCards);
                    // in case we need the drawn cards for an attempt to claim a tunnel
                    // we add the THREE top deck cards to the drawn cards because when
                    // attempting to claim a tunnel, only three cards are drawn
                    for (int i = 0; i < Constants.ADDITIONAL_TUNNEL_CARDS; i++) {
                        gameState = gameState.withCardsDeckRecreatedIfNeeded(rng);
                        drawnCards.add(gameState.topCard());
                        gameState = gameState.withoutTopCard();
                    }
                    gameState = gameState.withCardsDeckRecreatedIfNeeded(rng);
                    amountOfCardsToPlay =
                            claimedRoute.additionalClaimCardsCount(
                                    initialClaimCards, SortedBag.of(drawnCards));

                    if (amountOfCardsToPlay == 0) {
                        ReceiveInfoHandler.additionalCardsWereDrawnInfo(
                                players, currentPlayerInfo, drawnCards, 0);
                        // no additional cards to play-> player claims the tunnel directly
                        gameState = gameState.withClaimedRoute(claimedRoute, initialClaimCards);
                        ReceiveInfoHandler.claimedRoute(
                                players, currentPlayerInfo, claimedRoute, initialClaimCards);
                    } else {
                        ReceiveInfoHandler.additionalCardsWereDrawnInfo(
                                players, currentPlayerInfo, drawnCards, amountOfCardsToPlay);
                        // player must choose which additional cards he wants to play when
                        // he attempts to claim tunnel

                        possibleAdditionalCardsToPlay =
                                gameState
                                        .currentPlayerState()
                                        .possibleAdditionalCards(
                                                amountOfCardsToPlay,
                                                initialClaimCards,
                                                SortedBag.of(drawnCards));

                        System.out.println(gameState.currentPlayerState().cards());

                        // possibleAdditionalCardsToPlay is empty -> he can't take the route
                        // chosenCards is empty -> does not want to take the route
                        if (possibleAdditionalCardsToPlay.isEmpty()) {
                            ReceiveInfoHandler.didNotClaimRoute(
                                    players, currentPlayerInfo, claimedRoute);
                        } else {
                            chosenCards =
                                    currentPlayer.chooseAdditionalCards(
                                            possibleAdditionalCardsToPlay);
                            if (chosenCards.isEmpty()) {
                                ReceiveInfoHandler.didNotClaimRoute(
                                        players, currentPlayerInfo, claimedRoute);
                            } else {
                                cardsPlayedForTunnelClaim = initialClaimCards.union(chosenCards);
                                ReceiveInfoHandler.claimedRoute(
                                        players,
                                        currentPlayerInfo,
                                        claimedRoute,
                                        cardsPlayedForTunnelClaim);
                                // we have to sum up all the cards played to claim tunnel
                                // withClaimedRoute automatically adds cards to discards
                                gameState =
                                        gameState.withClaimedRoute(
                                                claimedRoute, cardsPlayedForTunnelClaim);
                            }
                        }
                        // we add the drawn cards to the discards
                        gameState = gameState.withMoreDiscardedCards(SortedBag.of(drawnCards));
                    }
                }
            } else {
                System.out.println("Can't take the route !");
            }
        }
    }

    private static class ReceiveInfoHandler {
        private static void willPlayerFirst(Map<PlayerId, Player> players, Info currentPlayer) {
            players.forEach(
                    (playerId, player) -> player.receiveInfo(currentPlayer.willPlayFirst()));
        }

        private static void chooseInitialTicketsInfo(
                Map<PlayerId, Player> players, Info currentPlayer) {
            players.forEach(
                    (playerId, player) ->
                            player.receiveInfo(
                                    currentPlayer.keptTickets(
                                            player.chooseInitialTickets().size())));
        }

        private static void drewBlindCard(Map<PlayerId, Player> players, Info currentPlayer) {
            players.forEach(
                    (playerId, allPlayers) ->
                            allPlayers.receiveInfo(currentPlayer.drewBlindCard()));
        }

        private static void drewVisibleCard(Map<PlayerId, Player> players, Info currentPlayer) {
            players.forEach(
                    (playerId, allPlayers) ->
                            allPlayers.receiveInfo(
                                    currentPlayer.drewVisibleCard(gameState.topCard())));
        }

        private static void drewTickets(Map<PlayerId, Player> players, Info currentPlayer) {
            players.forEach(
                    (playerId, player) ->
                            player.receiveInfo(
                                    currentPlayer.drewTickets(Constants.IN_GAME_TICKETS_COUNT)));
        }

        private static void claimedRoute(
                Map<PlayerId, Player> players,
                Info currentPlayer,
                Route claimedRoute,
                SortedBag<Card> cards) {
            players.forEach(
                    (playerId, allPlayers) ->
                            allPlayers.receiveInfo(
                                    currentPlayer.claimedRoute(claimedRoute, cards)));
        }

        private static void attemptedTunnelClaim(
                Map<PlayerId, Player> players,
                Info currentPlayer,
                Route claimedRoute,
                SortedBag<Card> cards) {
            players.forEach(
                    (playerId, allPlayers) ->
                            allPlayers.receiveInfo(
                                    currentPlayer.attemptsTunnelClaim(
                                            claimedRoute, SortedBag.of(cards))));
        }

        private static void additionalCardsWereDrawnInfo(
                Map<PlayerId, Player> p, Info cPlayer, List<Card> dCards, int aCards) {
            p.forEach(
                    (playerId, player) ->
                            player.receiveInfo(
                                    cPlayer.drewAdditionalCards(SortedBag.of(dCards), aCards)));
        }

        private static void didNotClaimRoute(
                Map<PlayerId, Player> players, Info currentPlayer, Route claimedRoute) {
            players.forEach(
                    (playerId, player) ->
                            player.receiveInfo(currentPlayer.didNotClaimRoute(claimedRoute)));
        }

        private static void lastTurnBegins(
                GameState gameState, Map<PlayerId, Player> players, Info currentPlayer) {
            players.forEach(
                    (playerId, player) ->
                            player.receiveInfo(
                                    currentPlayer.lastTurnBegins(
                                            gameState.currentPlayerState().carCount())));
        }

        private static void longestTrail(
                Map<PlayerId, Player> players, Info playerLongestTrail, Trail longest) {
            players.forEach(
                    (playerId, player) ->
                            player.receiveInfo(playerLongestTrail.getsLongestTrailBonus(longest)));
        }

        private static void playersHaveDrawn(
                Map<PlayerId, Player> players, List<String> playerNames, int points) {

            players.forEach(
                    (playerId, player) -> player.receiveInfo(Info.draw(playerNames, points)));
        }

        private static void playerWon(
                Map<PlayerId, Player> players, Info winnerInfo, int winnerPoints, int loserPoints) {
            players.forEach(
                    (playerId, player) ->
                            player.receiveInfo(winnerInfo.won(winnerPoints, loserPoints)));
        }
    }
}
