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
        Info currentPlayer = new Info(firstPlayer.name());
        Info nextPlayer = new Info(firstPlayer.next().name());
        Preconditions.checkArgument(players.size() == 2 && playerNames.size() == 2);

        // initialising both players
        players.forEach((playerId, player) -> player.initPlayers(playerId, playerNames));
        receiveNewInfo(players, currentPlayer, "will play first");

       setInitialTicketsChoices(players, firstPlayer);
       setInitialTicketsChoices(players, firstPlayer.next());

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
                                gameState.withChosenAdditionalTickets(
                                        topTicketsInGame, retainedTickets);

                        // removing the 3 top tickets from the tickets deck
                        gameState = gameState.withoutTopTickets(Constants.IN_GAME_TICKETS_COUNT);
                        players.forEach(
                                (playerId, both) ->
                                        both.receiveInfo(
                                                currentPlayer.keptTickets(retainedTickets.size())));
                    } // else nothing
                    // https://discord.com/channels/807922527716114432/807922528310788110/826799128306384926

                    // next round can begin
                    gameState = nextTurn(gameState, players, nextPlayer);
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
                                // if we pick a blind card - we have to remove the top card
                                gameState = gameState.withoutTopCard();
                            } else {
                                receiveNewInfo(players, currentPlayer, "drew visible card");
                                gameState = gameState.withDrawnFaceUpCard(indexOfChosenCard);
                            }
                            // we update the playerStates after the first card is drawn
                            updatePlayerStates(players, gameState, gameState.currentPlayerState());
                        }
                    }
                    gameState = nextTurn(gameState, players, nextPlayer);
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
                            receiveNewInfo(
                                    players,
                                    currentPlayer,
                                    claimedRoute,
                                    initialClaimCards,
                                    "claimed route");
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
                                AdditionalCardsWereDrawnInfo(players, currentPlayer, drawnCards, 0);
                            } else {
                                chosenCards =
                                        playerChoice.chooseAdditionalCards(additionalCardsToPlay);
                                // if additional cards to play is empty - it means the player
                                // doesn't want to take the tunnel - or he simply can't
                                if (chosenCards.isEmpty()) {
                                    receiveNewInfo(
                                            players,
                                            currentPlayer,
                                            claimedRoute,
                                            SortedBag.of(),
                                            "did not claim route");
                                } else {
                                    AdditionalCardsWereDrawnInfo(
                                            players,
                                            currentPlayer,
                                            drawnCards,
                                            amountOfCardsToPlay);
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
                    gameState = nextTurn(gameState, players, nextPlayer);
                    break;
            }
            // TODO - the 2 final rounds before the end of the game
        }
        lastTurnBeginsInfo(gameState, players, currentPlayer);

        Trail longestForCurrentPlayer = Trail.longest(gameState.playerState(firstPlayer).routes());
        Trail longestForNextPlayer =
                Trail.longest(gameState.playerState(firstPlayer.next()).routes());

        int winnerPoints = 0;
        int loserPoints = 0;
        if (longestForCurrentPlayer.length() > longestForNextPlayer.length()) {
            longestTrailBonus(players, currentPlayer, longestForCurrentPlayer);
            winnerPoints =
                    gameState.currentPlayerState().finalPoints()
                            + Constants.LONGEST_TRAIL_BONUS_POINTS;
            loserPoints = gameState.currentPlayerState().finalPoints();
        } else if (longestForCurrentPlayer.length() < longestForNextPlayer.length()) {
            longestTrailBonus(players, nextPlayer, longestForNextPlayer);
            winnerPoints = gameState.currentPlayerState().finalPoints();
            loserPoints =
                    gameState.playerState(firstPlayer.next()).finalPoints()
                            + Constants.LONGEST_TRAIL_BONUS_POINTS;
        }

        updatePlayerStates(players, gameState, gameState.currentPlayerState());

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
                players.forEach(
                        (playerId, allPlayers) ->
                                allPlayers.receiveInfo(
                                        currentPlayer.drewVisibleCard(gameState.topCard())));
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

    private static void lastTurnBeginsInfo(
            GameState gameState, Map<PlayerId, Player> players, Info currentPlayer) {
        players.forEach(
                (playerId, both) ->
                        both.receiveInfo(
                                currentPlayer.lastTurnBegins(
                                        gameState.currentPlayerState().carCount())));
    }

    private static void setInitialTicketsChoices (Map<PlayerId, Player> players, PlayerId playerId){
        // player gets delivered the top 5 tickets
        SortedBag<Ticket> playerTickets =
                gameState.topTickets(Constants.INITIAL_TICKETS_COUNT);
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
