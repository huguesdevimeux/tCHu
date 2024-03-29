package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.gui.Info;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a game of tCHu (aka les Aventuriers du Rail but shhh).
 *
 * @author Luca Mouchel (324748)
 * @author Hugues Devimeux (327282)
 */
public final class Game {
    private static final Map<PlayerId, Info> playersInfo = new EnumMap<>(PlayerId.class);
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
        playerNames
                .keySet()
                .forEach(
                        (playerId) ->
                                playersInfo.put(playerId, new Info(playerNames.get(playerId))));

        beginGame(players, playerNames);

        // the following part represents the "mid-game" (ie each turn until the last round begins)
        while (true) {
            Player currentPlayer = players.get(gameState.currentPlayerId());
            updatePlayerStates(players, gameState);
            Player.TurnKind turnKindChosenByCurrentPlayer = currentPlayer.nextTurn();
            switch (turnKindChosenByCurrentPlayer) {
                case DRAW_TICKETS:
                    TurnHandler.drawTickets(
                            players, currentPlayer, playersInfo.get(gameState.currentPlayerId()));
                    break;
                case DRAW_CARDS:
                    TurnHandler.drawCards(
                            players, currentPlayer, playersInfo.get(gameState.currentPlayerId()), rng);
                    break;
                case CLAIM_ROUTE:
                    TurnHandler.claimRoute(
                            players, currentPlayer, playersInfo.get(gameState.currentPlayerId()), rng);
                    break;
            }
            if (gameState.lastPlayer() == gameState.currentPlayerId()) break;
            gameState = nextTurn(players);
        }
        endGame(players, playerNames);
    }

    /**
     * Deals with the beginning of the game. Initialises the players and deals with the ticket
     * management at the beginning of the game
     *
     * @param players players in the game
     * @param playerNames names of <code>players</code>
     */
    private static void beginGame(
            Map<PlayerId, Player> players, Map<PlayerId, String> playerNames) {
        // initialising both players
        players.forEach((playerId, player) -> player.initPlayers(playerId, playerNames));
        ReceiveInfoHandler.willPlayFirst(players, playersInfo.get(gameState.currentPlayerId()));
        // each player receives 5 tickets and chooses at least 3 from these
        initialTicketsManagement(players);
        //  the game can start, the players receive the info that the current player can play
        ReceiveInfoHandler.canPlay(players);
    }

    /**
     * Deals with the ticket management at the beginning. The player receives a set of initial cards
     * (5 top tickets) and must pick at least three.
     *
     * @param players use it to <code>setInitialTicketChoice</code> to the player in question
     */
    private static void initialTicketsManagement(Map<PlayerId, Player> players) {
        for (PlayerId playerId : players.keySet()) {
            // player gets delivered the top 5 tickets
            SortedBag<Ticket> initialTicketsChoice =
                    gameState.topTickets(GameConstants.INITIAL_TICKETS_COUNT);
            players.get(playerId).setInitialTicketChoice(initialTicketsChoice);
            gameState = gameState.withoutTopTickets(GameConstants.INITIAL_TICKETS_COUNT);
        }
        // we update the states before the player can pick desired tickets
        updatePlayerStates(players, gameState);

        for (PlayerId playerId : players.keySet()) {
            SortedBag<Ticket> chosenInitialTickets = players.get(playerId).chooseInitialTickets();
            // the player then chooses the tickets they want to keep and we have to remove the top
            // tickets from the deck of tickets
            gameState = gameState.withInitiallyChosenTickets(playerId, chosenInitialTickets);
        }

        for (PlayerId playerId : players.keySet()) {
            ReceiveInfoHandler.chosenTicketsInfo(
                    players,
                    playersInfo.get(playerId),
                    gameState.playerState(playerId).ticketCount());
        }
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
        ReceiveInfoHandler.canPlay(players);
        return gameState;
    }

    // used to update the player of the states
    private static void updatePlayerStates(Map<PlayerId, Player> players, GameState gameState) {
        players.forEach(
                (playerId, player) ->
                        player.updateState(gameState, gameState.playerState(playerId)));
    }

    private static void endGame(Map<PlayerId, Player> players, Map<PlayerId, String> playerNames) {
        updatePlayerStates(players, gameState);

        Map<PlayerId, Integer> points = new EnumMap<>(PlayerId.class);
        for (PlayerId playerId : PlayerId.ALL) {
            points.put(playerId, gameState.playerState(playerId).finalPoints());
        }

        Map<PlayerId, Trail> longestTrails =
                PlayerId.ALL.stream()
                        .collect(
                                Collectors.toMap(
                                        Function.identity(),
                                        playerId ->
                                                Trail.longest(
                                                        gameState.playerState(playerId).routes())));

        int maxLength =
                longestTrails.values().stream()
                        .map(Trail::length)
                        .max(Integer::compareTo)
                        .orElse(0);

        // Adds the bonus for each player that has the longest trail.
        longestTrails.entrySet().stream()
                .filter(playerIdTrailEntry -> playerIdTrailEntry.getValue().length() == maxLength)
                .forEach(
                        playerIdTrailEntry -> {
                            points.computeIfPresent(
                                    playerIdTrailEntry.getKey(),
                                    (p, pointsOfPlayer) ->
                                            pointsOfPlayer + GameConstants.LONGEST_TRAIL_BONUS_POINTS);
                            ReceiveInfoHandler.longestTrail(
                                    players,
                                    playersInfo.get(playerIdTrailEntry.getKey()),
                                    playerIdTrailEntry.getValue());
                        });

        int maxPoints = Collections.max(points.values());
        Map<PlayerId, Integer> playersWithMaxPoints =
                points.entrySet().stream()
                        .filter(
                                playerIdIntegerEntry ->
                                        playerIdIntegerEntry.getValue() == maxPoints)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // Unique winner.
        if (playersWithMaxPoints.size() == 1) {
            Map.Entry<PlayerId, Integer> winner = playersWithMaxPoints.entrySet().iterator().next();
            int loserPoints = Collections.min(points.values());
            ReceiveInfoHandler.playerWon(
                    players, playersInfo.get(winner.getKey()), maxPoints, loserPoints);
            // Several players have the same points => draw.
        } else if (playersWithMaxPoints.size() > 1) {
            List<String> playersWithSamePointsNames =
                    playersWithMaxPoints.keySet().stream()
                            .map(playerNames::get)
                            .collect(Collectors.toList());
            ReceiveInfoHandler.playersHaveDrawn(players, playersWithSamePointsNames, maxPoints);
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
                        gameState.topTickets(GameConstants.IN_GAME_TICKETS_COUNT);

                // take the three first of the tickets pile
                SortedBag<Ticket> retainedTickets = currentPlayer.chooseTickets(topTicketsInGame);
                // the following method already removes top tickets so we don't
                // have to take care of it
                gameState =
                        gameState.withChosenAdditionalTickets(topTicketsInGame, retainedTickets);
                ReceiveInfoHandler.chosenTicketsInfo(
                        players, currentPlayerInfo, retainedTickets.size());
            } // else nothing
            // https://discord.com/channels/807922527716114432/807922528310788110/826799128306384926
        }

        public static void drawCards(
                Map<PlayerId, Player> players,
                Player currentPlayer,
                Info currentPlayerInfo,
                Random rng) {
            // the player only draws two cards
            int numberOfPossibleCardsToDraw = 2;
            if (gameState.canDrawCards()) {
                for (int i = 0; i < numberOfPossibleCardsToDraw; i++) {
                // if there aren't enough cards to begin with, we shuffle the bigboi
                gameState = gameState.withCardsDeckRecreatedIfNeeded(rng);
                    int indexOfChosenCard = currentPlayer.drawSlot();
                    // method drawSlot returns -1 if the player picks a card from the
                    // deck of cards or a number between 0 and 4 if one of the faceUp cards
                    if (indexOfChosenCard == GameConstants.DECK_SLOT) {
                        ReceiveInfoHandler.drewBlindCard(players, currentPlayerInfo);
                        gameState = gameState.withBlindlyDrawnCard();
                    } else {
                        ReceiveInfoHandler.drewVisibleCard(
                                players,
                                currentPlayerInfo,
                                gameState.cardState().faceUpCard(indexOfChosenCard));
                        gameState = gameState.withDrawnFaceUpCard(indexOfChosenCard);
                    }
                    gameState = gameState.withCardsDeckRecreatedIfNeeded(rng);
                    // we update the playerStates after the first card is drawn
                    updatePlayerStates(players, gameState);
                }
            }
        }

        public static void claimRoute(
                Map<PlayerId, Player> players, Player currentPlayer, Info currentPlayerInfo, Random rng) {
            SortedBag<Card> chosenCards;
            SortedBag<Card> cardsPlayedForTunnelClaim;
            Route claimedRoute = currentPlayer.claimedRoute();
            SortedBag<Card> initialClaimCards = currentPlayer.initialClaimCards();
            boolean canClaimRoute = gameState.currentPlayerState().canClaimRoute(claimedRoute);
            List<Card> drawnCards = new ArrayList<>();
            if (!canClaimRoute) return;

            if (claimedRoute.level().equals(Route.Level.OVERGROUND)) {
                // players receive the info that the current played has claimed route
                ReceiveInfoHandler.claimedRoute(
                        players, currentPlayerInfo, claimedRoute, initialClaimCards);
                // adding the claimed route to the current player's claimed routes
                gameState = gameState.withClaimedRoute(claimedRoute, initialClaimCards);
            } else {
                ReceiveInfoHandler.attemptedTunnelClaim(
                        players, currentPlayerInfo, claimedRoute, initialClaimCards);
                // in case drawn cards are needed for an attempt to claim a tunnel
                // the program must add the THREE top deck cards to the drawn cards because when
                // attempting to claim a tunnel, only three cards are drawn
                for (int i = 0; i < GameConstants.ADDITIONAL_TUNNEL_CARDS; i++) {
                    gameState = gameState.withCardsDeckRecreatedIfNeeded(rng);
                    drawnCards.add(gameState.topCard());
                    gameState = gameState.withoutTopCard();
                }
                gameState = gameState.withCardsDeckRecreatedIfNeeded(rng);
                int amountOfCardsToPlay =
                        claimedRoute.additionalClaimCardsCount(initialClaimCards, SortedBag.of(drawnCards));

                if (amountOfCardsToPlay == 0) {
                    ReceiveInfoHandler.additionalCardsWereDrawnInfo(
                            players, currentPlayerInfo, drawnCards, 0);
                    // no additional cards to play-> player claims the tunnel directly
                    gameState =
                            gameState
                                    .withClaimedRoute(claimedRoute, initialClaimCards)
                                    .withMoreDiscardedCards(SortedBag.of(drawnCards));
                    ReceiveInfoHandler.claimedRoute(
                            players, currentPlayerInfo, claimedRoute, initialClaimCards);
                } else {
                    ReceiveInfoHandler.additionalCardsWereDrawnInfo(
                            players, currentPlayerInfo, drawnCards, amountOfCardsToPlay);
                    // player must choose which additional cards they want to play when
                    // attempting to claim tunnel
                    List<SortedBag<Card>> possibleAdditionalCardsToPlay =
                            gameState
                                    .currentPlayerState()
                                    .possibleAdditionalCards(amountOfCardsToPlay, initialClaimCards);
                    // possibleAdditionalCardsToPlay empty -> can't take the route
                    if (possibleAdditionalCardsToPlay.isEmpty()) {
                        ReceiveInfoHandler.didNotClaimRoute(players, currentPlayerInfo, claimedRoute);
                    } else {
                        chosenCards = currentPlayer.chooseAdditionalCards(possibleAdditionalCardsToPlay);
                        // chosenCards is empty -> does not want to take the route
                        if (chosenCards.isEmpty()) {
                            ReceiveInfoHandler.didNotClaimRoute(players, currentPlayerInfo, claimedRoute);
                        } else {
                            cardsPlayedForTunnelClaim = initialClaimCards.union(chosenCards);
                            ReceiveInfoHandler.claimedRoute(
                                    players, currentPlayerInfo, claimedRoute, cardsPlayedForTunnelClaim);
                            // we have to sum up all the cards played to claim tunnel
                            // withClaimedRoute automatically adds cards to discards
                            gameState = gameState.withClaimedRoute(claimedRoute, cardsPlayedForTunnelClaim);
                        }
                    }
                    // we add the drawn cards to the discards
                    gameState = gameState.withMoreDiscardedCards(SortedBag.of(drawnCards));
                }
            }
        }
    }

    private static class ReceiveInfoHandler {
        public static void willPlayFirst(Map<PlayerId, Player> players, Info currentPlayer) {
            players.forEach((playerId, player) -> player.receiveInfo(currentPlayer.willPlayFirst()));
        }

        public static void canPlay(Map<PlayerId, Player> players) {
            players
                    .values()
                    .forEach(
                            (player) ->
                                    player.receiveInfo(playersInfo.get(gameState.currentPlayerId()).canPlay()));
        }

        public static void chosenTicketsInfo(
                Map<PlayerId, Player> players, Info currentPlayer, int numberOfAdditionalChosenTickets) {
            players.forEach(
                    (playerId, player) ->
                            player.receiveInfo(currentPlayer.keptTickets(numberOfAdditionalChosenTickets)));
        }

        public static void drewBlindCard(Map<PlayerId, Player> players, Info currentPlayer) {
            players.forEach(
                    (playerId, allPlayers) -> allPlayers.receiveInfo(currentPlayer.drewBlindCard()));
        }

        public static void drewVisibleCard(
                Map<PlayerId, Player> players, Info currentPlayer, Card card) {
            players.forEach(
                    (playerId, allPlayers) -> allPlayers.receiveInfo(currentPlayer.drewVisibleCard(card)));
        }

        public static void drewTickets(Map<PlayerId, Player> players, Info currentPlayer) {
            players.forEach(
                    (playerId, player) ->
                            player.receiveInfo(currentPlayer.drewTickets(GameConstants.IN_GAME_TICKETS_COUNT)));
        }

        public static void claimedRoute(
                Map<PlayerId, Player> players,
                Info currentPlayer,
                Route claimedRoute,
                SortedBag<Card> cards) {
            players.forEach(
                    (playerId, allPlayers) ->
                            allPlayers.receiveInfo(currentPlayer.claimedRoute(claimedRoute, cards)));
        }

        public static void attemptedTunnelClaim(
                Map<PlayerId, Player> players,
                Info currentPlayer,
                Route claimedRoute,
                SortedBag<Card> cards) {
            players.forEach(
                    (playerId, allPlayers) ->
                            allPlayers.receiveInfo(currentPlayer.attemptsTunnelClaim(claimedRoute, cards)));
        }

        public static void additionalCardsWereDrawnInfo(
                Map<PlayerId, Player> p, Info cPlayer, List<Card> dCards, int aCards) {
            p.forEach(
                    (playerId, player) ->
                            player.receiveInfo(cPlayer.drewAdditionalCards(SortedBag.of(dCards), aCards)));
        }

        public static void didNotClaimRoute(
                Map<PlayerId, Player> players, Info currentPlayer, Route claimedRoute) {
            players.forEach(
                    (playerId, player) -> player.receiveInfo(currentPlayer.didNotClaimRoute(claimedRoute)));
        }

        public static void lastTurnBegins(
                GameState gameState, Map<PlayerId, Player> players, Info currentPlayer) {
            players.forEach(
                    (playerId, player) ->
                            player.receiveInfo(
                                    currentPlayer.lastTurnBegins(gameState.currentPlayerState().carCount())));
        }

        public static void longestTrail(
                Map<PlayerId, Player> players, Info playerLongestTrail, Trail longest) {
            players.forEach(
                    (playerId, player) ->
                            player.receiveInfo(playerLongestTrail.getsLongestTrailBonus(longest)));
        }

        public static void playersHaveDrawn(
                Map<PlayerId, Player> players, List<String> playerNames, int points) {

            players.forEach((playerId, player) -> player.receiveInfo(Info.draw(playerNames, points)));
        }

        public static void playerWon(
                Map<PlayerId, Player> players, Info winnerInfo, int winnerPoints, int loserPoints) {
            players.forEach(
                    (playerId, player) -> player.receiveInfo(winnerInfo.won(winnerPoints, loserPoints)));
        }
    }
}

