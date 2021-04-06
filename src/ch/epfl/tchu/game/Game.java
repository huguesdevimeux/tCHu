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
        // selecting the first player at random and representing him as the current player
        PlayerId firstPlayer = gameState.currentPlayerId();
        Info currentPlayerInfo = new Info(playerNames.get(firstPlayer));
        Info nextPlayerInfo = new Info(playerNames.get(firstPlayer.next()));
        Preconditions.checkArgument(players.size() == 2 && playerNames.size() == 2);

        beginGame(players, playerNames, currentPlayerInfo, firstPlayer);

        // the following part represents the "mid-game" (ie each turn until the last round
        // begins)
        boolean endGame = false;
        while (!gameState.lastTurnBegins()) {
            // representing the player as the key of Map player to be able to call the necessary
            // methods
            Player currentPlayer = players.get(gameState.currentPlayerId());
            updatePlayerStates(players, gameState, gameState.currentPlayerState());
            // following switch statement describes the possible actions
            // at each turn of the game
            switch (currentPlayer.nextTurn()) {
                case DRAW_TICKETS:
                    TurnHandler.drawTickets(players, currentPlayer, currentPlayerInfo);
                    // next round can begin
                    gameState = nextTurn(gameState, players, nextPlayerInfo);
                    break;
                case DRAW_CARDS:
                    TurnHandler.drawCards(players, currentPlayer, currentPlayerInfo, rng);
                    gameState = nextTurn(gameState, players, nextPlayerInfo);
                    break;

                case CLAIM_ROUTE:
                    TurnHandler.claimRoute(players, currentPlayer, currentPlayerInfo, rng);
                    gameState = nextTurn(gameState, players, nextPlayerInfo);
                    break;
            }
            // TODO - the 2 final rounds before the end of the game
        }
        lastTurnBeginsInfo(gameState, players, currentPlayerInfo);

        endGame(players, playerNames, currentPlayerInfo, nextPlayerInfo, firstPlayer);
    }

    private static void beginGame(
            Map<PlayerId, Player> players,
            Map<PlayerId, String> playerNames,
            Info currentPlayerInfo,
            PlayerId firstPlayer) {

        // initialising both players
        players.forEach((playerId, player) -> player.initPlayers(playerId, playerNames));
        ReceiveInfoHandler.willPlayerFirst(players, currentPlayerInfo);

        setInitialTicketsChoices(players, firstPlayer);
        setInitialTicketsChoices(players, firstPlayer.next());

        updatePlayerStates(players, gameState, gameState.currentPlayerState());
        // from these 5 tickets, each player chooses their initial tickets
        players.forEach((playerId, player) -> player.chooseInitialTickets());
        ReceiveInfoHandler.chooseInitialTickets(players, currentPlayerInfo);

        // finally, the game can start, the players receive the info that the currentPlayer can play
        players.forEach((playerId, player) -> player.receiveInfo(currentPlayerInfo.canPlay()));
    }

    private static void endGame(
            Map<PlayerId, Player> players,
            Map<PlayerId, String> playerNames,
            Info currentPlayerInfo,
            Info nextPlayerInfo,
            PlayerId firstPlayer) {
        Trail longestForCurrentPlayer = Trail.longest(gameState.playerState(firstPlayer).routes());
        Trail longestForNextPlayer =
                Trail.longest(gameState.playerState(firstPlayer.next()).routes());

        int winnerPoints = 0;
        int loserPoints = 0;
        if (longestForCurrentPlayer.length() > longestForNextPlayer.length()) {
            longestTrailBonus(players, currentPlayerInfo, longestForCurrentPlayer);
            winnerPoints =
                    gameState.currentPlayerState().finalPoints()
                            + Constants.LONGEST_TRAIL_BONUS_POINTS;
            loserPoints = gameState.currentPlayerState().finalPoints();
        } else if (longestForCurrentPlayer.length() < longestForNextPlayer.length()) {
            longestTrailBonus(players, nextPlayerInfo, longestForNextPlayer);
            winnerPoints = gameState.currentPlayerState().finalPoints();
            loserPoints =
                    gameState.playerState(firstPlayer.next()).finalPoints()
                            + Constants.LONGEST_TRAIL_BONUS_POINTS;
        }

        updatePlayerStates(players, gameState, gameState.currentPlayerState());

        if (winnerPoints > loserPoints)
            currentPlayerWonInfo(players, currentPlayerInfo, winnerPoints, loserPoints);
        else if (winnerPoints == loserPoints)
            playersHaveDrawnInfo(players, new ArrayList<>(playerNames.values()), winnerPoints);
    }

    private static void receiveInfoRelatedToRoute(
            Map<PlayerId, Player> players,
            Info currentPlayer,
            Route claimedRoute,
            SortedBag<Card> cards,
            InfoToDisplay info) {
        switch (info) {
            case CLAIMED_ROUTE:
                players.forEach(
                        (playerId, allPlayers) ->
                                allPlayers.receiveInfo(
                                        currentPlayer.claimedRoute(claimedRoute, cards)));
                break;
            case ATTEMPTED_TUNNEL_CLAIM:
                players.forEach(
                        (playerId, allPlayers) ->
                                allPlayers.receiveInfo(
                                        currentPlayer.attemptsTunnelClaim(
                                                claimedRoute, SortedBag.of(cards))));
                break;
            case DID_NOT_CLAIM_ROUTE:
                players.forEach(
                        (playerId, player) ->
                                player.receiveInfo(currentPlayer.didNotClaimRoute(claimedRoute)));
                break;
        }
    }

    // info the players receive to know additional cards were drawn
    private static void AdditionalCardsWereDrawnInfo(
            Map<PlayerId, Player> p, Info cPlayer, List<Card> dCards, int aCards) {
        p.forEach(
                (playerId, player) ->
                        player.receiveInfo(
                                cPlayer.drewAdditionalCards(SortedBag.of(dCards), aCards)));
    }

    // info the players receive to know that a player won
    private static void currentPlayerWonInfo(
            Map<PlayerId, Player> players, Info currentPlayer, int winnerPoints, int loserPoints) {
        players.forEach(
                (playerId, player) ->
                        player.receiveInfo(currentPlayer.won(winnerPoints, loserPoints)));
    }

    // info the players receive to know both players have drawn
    private static void playersHaveDrawnInfo(
            Map<PlayerId, Player> players, List<String> playerNames, int points) {
        players.forEach((playerId, player) -> player.receiveInfo(Info.draw(playerNames, points)));
    }

    // info the players receive to know that the last turn begins
    private static void lastTurnBeginsInfo(
            GameState gameState, Map<PlayerId, Player> players, Info currentPlayer) {
        players.forEach(
                (playerId, player) ->
                        player.receiveInfo(
                                currentPlayer.lastTurnBegins(
                                        gameState.currentPlayerState().carCount())));
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
        // get the key of the map matching the current playerId and set initial ticket choice
        players.get(playerId).setInitialTicketChoice(playerTickets);
        // we have to remove the top tickets from the deck of tickets
        gameState = gameState.withoutTopTickets(Constants.INITIAL_TICKETS_COUNT);
    }

    /**
     * Deals with the next turn. Calls <code>forNextTurn</code> in gameState and both players
     * receive info that the next player can play.
     *
     * @param gameState gamestate to end - go to the next round
     * @param players the players in the game
     * @param nextPlayer the player
     * @return a new gameState with the next player that will play
     */
    private static GameState nextTurn(
            GameState gameState, Map<PlayerId, Player> players, Info nextPlayer) {
        gameState = gameState.forNextTurn();
        players.forEach((playerId, player) -> player.receiveInfo(nextPlayer.canPlay()));
        return gameState;
    }

    // used to update the player of the states
    private static void updatePlayerStates(
            Map<PlayerId, Player> players, PublicGameState gameState, PlayerState playerState) {
        players.forEach((playerId, player) -> player.updateState(gameState, playerState));
    }

    // info where both players receive the info that a player has received the longest trail bonus
    private static void longestTrailBonus(
            Map<PlayerId, Player> players, Info playerIdentity, Trail longest) {
        players.forEach(
                (playerId, player) ->
                        player.receiveInfo(playerIdentity.getsLongestTrailBonus(longest)));
    }

    // private methods created to compress code in main method of this class
    // using an enum as a selection in the switch statements to provide a minimum
    // of description
    private enum InfoToDisplay {
        // enum to be able to select the information we want the players to receive
        CLAIMED_ROUTE,
        ATTEMPTED_TUNNEL_CLAIM,
        DID_NOT_CLAIM_ROUTE,
    }

    private static class ReceiveInfoHandler {
        public static void willPlayerFirst(Map<PlayerId, Player> players, Info currentPlayer) {
            players.forEach(
                    (playerId, player) -> player.receiveInfo(currentPlayer.willPlayFirst()));
        }

        public static void chooseInitialTickets(Map<PlayerId, Player> players, Info currentPlayer) {
            players.forEach(
                    (playerId, player) ->
                            player.receiveInfo(
                                    currentPlayer.keptTickets(
                                            // tickets.size()? or:
                                            Constants.INITIAL_TICKETS_COUNT
                                                    - player.chooseInitialTickets().size())));
        }

        public static void drewBlindCard(Map<PlayerId, Player> players, Info currentPlayer) {
            players.forEach(
                    (playerId, allPlayers) ->
                            allPlayers.receiveInfo(currentPlayer.drewBlindCard()));
        }

        public static void drewVisibleCard(Map<PlayerId, Player> players, Info currentPlayer) {
            players.forEach(
                    (playerId, allPlayers) ->
                            allPlayers.receiveInfo(
                                    currentPlayer.drewVisibleCard(gameState.topCard())));
        }

        public static void drewTickets(Map<PlayerId, Player> players, Info currentPlayer) {
            players.forEach(
                    (playerId, player) ->
                            player.receiveInfo(
                                    currentPlayer.drewTickets(Constants.IN_GAME_TICKETS_COUNT)));
        }
    }

    /**
     * Handles the different turns logic.
     */
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
                Map<PlayerId, Player> players, Player currentPlayer, Info currentPlayerInfo, Random rng) {
            // the player only draws two cards
            int totalNumberOfPossibleCardsToDraw = 2;

            for (int i = 0; i < totalNumberOfPossibleCardsToDraw; i++) {
                // if there aren't enough cards to begin with, we shuffle the bigboi
                gameState = gameState.withCardsDeckRecreatedIfNeeded(rng);

                if (gameState.canDrawCards()) {
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
                    updatePlayerStates(players, gameState, gameState.currentPlayerState());
                }
            }
        }

        public static void claimRoute(
                Map<PlayerId, Player> players, Player currentPlayer, Info currentPlayerInfo, Random rng) {
            Route claimedRoute = currentPlayer.claimedRoute();
            SortedBag<Card> initialClaimCards = currentPlayer.initialClaimCards();
            int amountOfCardsToPlay;
            List<Card> drawnCards = new ArrayList<>();
            List<SortedBag<Card>> additionalCardsToPlay;
            SortedBag<Card> chosenCards;
            SortedBag<Card> cardsPlayedForTunnelClaim;
            boolean canClaimRoute = gameState.currentPlayerState().canClaimRoute(claimedRoute);

            if (canClaimRoute) {
                if (claimedRoute.level().equals(Route.Level.OVERGROUND)) {
                    // players receive the info that the current played has claimed route
                    receiveInfoRelatedToRoute(
                            players,
                            currentPlayerInfo,
                            claimedRoute,
                            initialClaimCards,
                            InfoToDisplay.CLAIMED_ROUTE);
                    // adding the claimed route to the current player's claimed routes
                    gameState = gameState.withClaimedRoute(claimedRoute, initialClaimCards);
                } else {
                    receiveInfoRelatedToRoute(
                            players,
                            currentPlayerInfo,
                            claimedRoute,
                            SortedBag.of(initialClaimCards),
                            InfoToDisplay.ATTEMPTED_TUNNEL_CLAIM);
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
                    // player must choose which additional cards he wants to play when
                    // he attempts to claim tunnel
                    additionalCardsToPlay =
                            gameState
                                    .currentPlayerState()
                                    .possibleAdditionalCards(
                                            amountOfCardsToPlay,
                                            initialClaimCards,
                                            SortedBag.of(drawnCards));

                    if (additionalCardsToPlay.isEmpty()) {
                        // no additional cards to play-> player claims the tunnel directly
                        gameState = gameState.withClaimedRoute(claimedRoute, initialClaimCards);
                        AdditionalCardsWereDrawnInfo(players, currentPlayerInfo, drawnCards, 0);
                    } else {
                        chosenCards = currentPlayer.chooseAdditionalCards(additionalCardsToPlay);
                        AdditionalCardsWereDrawnInfo(
                                players, currentPlayerInfo, drawnCards, amountOfCardsToPlay);
                        // if additional cards to play is empty - it means the player
                        // doesn't want to take the tunnel - or he simply can't
                        if (chosenCards.isEmpty()) {
                            receiveInfoRelatedToRoute(
                                    players,
                                    currentPlayerInfo,
                                    claimedRoute,
                                    SortedBag.of(),
                                    InfoToDisplay.DID_NOT_CLAIM_ROUTE);
                        } else {
                            // we have to sum up all the cards played to claim tunnel
                            cardsPlayedForTunnelClaim = initialClaimCards.union(chosenCards);
                            // withClaimedRoute automatically adds cards to discards
                            gameState =
                                    gameState.withClaimedRoute(
                                            claimedRoute, cardsPlayedForTunnelClaim);
                        }
                        // we add the drawn cards to the discards
                        gameState = gameState.withMoreDiscardedCards(SortedBag.of(drawnCards));
                    }
                }
            }
        }
    }
}
