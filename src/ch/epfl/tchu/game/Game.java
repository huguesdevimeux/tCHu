package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.gui.Info;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

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
        Info currentPlayerInfo = new Info(firstPlayer.name());
        Info nextPlayerInfo = new Info(firstPlayer.next().name());
        Preconditions.checkArgument(players.size() == 2 && playerNames.size() == 2);

        // initialising both players
        players.forEach((playerId, player) -> player.initPlayers(playerId, playerNames));
        receiveInfoRelatedToPlayer(players, currentPlayerInfo, InfoToDisplay.WILL_PLAY_FIRST);

        setInitialTicketsChoices(players, firstPlayer);
        setInitialTicketsChoices(players, firstPlayer.next());

        updatePlayerStates(players, gameState, gameState.currentPlayerState());
        // from these 5 tickets, each player chooses their initial tickets
        players.forEach((playerId, player) -> player.chooseInitialTickets());
        receiveInfoRelatedToPlayer(
                players, currentPlayerInfo, InfoToDisplay.CHOOSE_INITIAL_TICKETS);

        // finally, the game can start, the players receive the info that the currentPlayer can play
        players.forEach((playerId, both) -> both.receiveInfo(currentPlayerInfo.canPlay()));

        // the following part represents the "mid-game" (ie each turn until the last round
        // begins)
        boolean endGame = false;
        while (!endGame) {
            // representing the player as the key of Map player to be able to call the necessary
            // methods
            Player playerChoice = players.get(gameState.currentPlayerId());
            updatePlayerStates(players, gameState, gameState.currentPlayerState());
            // following switch statement describes the possible actions
            // at each turn of the game
            switch (playerChoice.nextTurn()) {
                case DRAW_TICKETS:
                    if (gameState.canDrawTickets()) {
                        receiveInfoRelatedToPlayer(
                                players, currentPlayerInfo, InfoToDisplay.DREW_TICKETS);
                        SortedBag<Ticket> topTicketsInGame =
                                gameState.topTickets(Constants.IN_GAME_TICKETS_COUNT);

                        // take the three first of the tickets pile
                        SortedBag<Ticket> retainedTickets =
                                playerChoice.chooseTickets(topTicketsInGame);
                        // the following method already removes top tickets so we don't
                        // have to take care of it
                        gameState =
                                gameState.withChosenAdditionalTickets(
                                        topTicketsInGame, retainedTickets);
                        players.forEach(
                                (playerId, both) ->
                                        both.receiveInfo(
                                                currentPlayerInfo.keptTickets(
                                                        retainedTickets.size())));
                    } // else nothing
                    // https://discord.com/channels/807922527716114432/807922528310788110/826799128306384926

                    // next round can begin
                    gameState = nextTurn(gameState, players, nextPlayerInfo);
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
                                receiveInfoRelatedToPlayer(
                                        players, currentPlayerInfo, InfoToDisplay.DREW_BLIND_CARD);
                                gameState = gameState.withBlindlyDrawnCard();
                            } else {
                                receiveInfoRelatedToPlayer(
                                        players,
                                        currentPlayerInfo,
                                        InfoToDisplay.DREW_VISIBLE_CARD);
                                gameState = gameState.withDrawnFaceUpCard(indexOfChosenCard);
                            }
                            gameState = gameState.withCardsDeckRecreatedIfNeeded(rng);
                            // we update the playerStates after the first card is drawn
                            updatePlayerStates(players, gameState, gameState.currentPlayerState());
                        }
                    }
                    gameState = nextTurn(gameState, players, nextPlayerInfo);
                    break;

                case CLAIM_ROUTE:
                    Route claimedRoute = playerChoice.claimedRoute();
                    SortedBag<Card> initialClaimCards = playerChoice.initialClaimCards();
                    int amountOfCardsToPlay;
                    List<Card> drawnCards = new ArrayList<>();
                    List<SortedBag<Card>> additionalCardsToPlay;
                    SortedBag<Card> chosenCards;
                    SortedBag<Card> cardsPlayedForTunnelClaim;
                    boolean canClaimRoute =
                            gameState.currentPlayerState().canClaimRoute(claimedRoute);

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
                                gameState =
                                        gameState.withClaimedRoute(claimedRoute, initialClaimCards);
                                AdditionalCardsWereDrawnInfo(
                                        players, currentPlayerInfo, drawnCards, 0);
                            } else {
                                chosenCards =
                                        playerChoice.chooseAdditionalCards(additionalCardsToPlay);
                                AdditionalCardsWereDrawnInfo(
                                        players,
                                        currentPlayerInfo,
                                        drawnCards,
                                        amountOfCardsToPlay);
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
                                    cardsPlayedForTunnelClaim =
                                            initialClaimCards.union(chosenCards);
                                    // withClaimedRoute automatically adds cards to discards
                                    gameState =
                                            gameState.withClaimedRoute(
                                                    claimedRoute, cardsPlayedForTunnelClaim);
                                }
                                // we add the drawn cards to the discards
                                gameState =
                                        gameState.withMoreDiscardedCards(SortedBag.of(drawnCards));
                            }
                        }
                    }
                    gameState = nextTurn(gameState, players, nextPlayerInfo);
                    break;
            }
            if (gameState.lastTurnBegins()) {
                // this is wrong but you get the idea - do you think it's the way to go?
                IntStream.range(0, players.size()).forEach(x -> gameState = gameState.forNextTurn());
                endGame = true;
            }
            // TODO - the 2 final rounds before the end of the game
        }
        lastTurnBeginsInfo(gameState, players, currentPlayerInfo);

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

    // private methods created to compress code in main method of this class
    // using an enum as a selection in the switch statements to provide a minimum
    // of description
    private enum InfoToDisplay {
        // enum to be able to select the information we want the players to receive
        WILL_PLAY_FIRST,
        CHOOSE_INITIAL_TICKETS,
        DREW_TICKETS,
        DREW_BLIND_CARD,
        DREW_VISIBLE_CARD,
        CLAIMED_ROUTE,
        ATTEMPTED_TUNNEL_CLAIM,
        DID_NOT_CLAIM_ROUTE;
    }

    private static void receiveInfoRelatedToPlayer(
            Map<PlayerId, Player> players, Info currentPlayer, InfoToDisplay info) {

        switch (info) {
            case WILL_PLAY_FIRST:
                players.forEach(
                        (playerId, player) -> player.receiveInfo(currentPlayer.willPlayFirst()));
                break;
            case CHOOSE_INITIAL_TICKETS:
                players.forEach(
                        (playerId, player) ->
                                player.receiveInfo(
                                        currentPlayer.keptTickets(
                                                // tickets.size()? or:
                                                Constants.INITIAL_TICKETS_COUNT
                                                        - player.chooseInitialTickets().size())));
                break;

            case DREW_TICKETS:
                players.forEach(
                        (playerId, both) ->
                                both.receiveInfo(
                                        currentPlayer.drewTickets(
                                                Constants.IN_GAME_TICKETS_COUNT)));

                break;
            case DREW_BLIND_CARD:
                players.forEach(
                        (playerId, allPlayers) ->
                                allPlayers.receiveInfo(currentPlayer.drewBlindCard()));
                break;

            case DREW_VISIBLE_CARD:
                players.forEach(
                        (playerId, allPlayers) ->
                                allPlayers.receiveInfo(
                                        currentPlayer.drewVisibleCard(gameState.topCard())));
                break;
        }
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

    private static void lastTurnBeginsInfo(
            GameState gameState, Map<PlayerId, Player> players, Info currentPlayer) {
        players.forEach(
                (playerId, both) ->
                        both.receiveInfo(
                                currentPlayer.lastTurnBegins(
                                        gameState.currentPlayerState().carCount())));
    }

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

    private static GameState nextTurn(
            GameState gameState, Map<PlayerId, Player> players, Info nextPlayer) {
        gameState = gameState.forNextTurn();
        players.forEach((playerId, player) -> player.receiveInfo(nextPlayer.canPlay()));
        return gameState;
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
