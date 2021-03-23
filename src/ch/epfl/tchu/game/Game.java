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

    public static void play(
            Map<PlayerId, Player> players,
            Map<PlayerId, String> playerNames,
            SortedBag<Ticket> tickets,
            Random rng) {
        Preconditions.checkArgument(players.size() == 2 && playerNames.size() == 2);

        GameState gameState = GameState.initial(tickets, rng);

        players.forEach((playerId, player) -> player.initPlayers(playerId, playerNames));
        PlayerId firstPlayer = PlayerId.ALL.get(rng.nextInt(PlayerId.ALL.size()));

        Info currentPlayer = new Info(firstPlayer.name());
        Info nextPlayer = new Info(firstPlayer.next().name());

        players.forEach((playerId, player) -> player.receiveInfo(currentPlayer.willPlayFirst()));
        players.forEach((playerId, player) -> player.setInitialTicketChoice(tickets));
        players.forEach((playerId, player) -> player.chooseInitialTickets());

        players.forEach(
                (playerId, player) ->
                        player.receiveInfo(
                                new Info(playerId.name())
                                        .keptTickets(
                                                // tickets.size()? or:
                                                Constants.INITIAL_TICKETS_COUNT
                                                        - player.chooseInitialTickets().size())));

        for (Map.Entry<PlayerId, Player> player : players.entrySet()) {

            Player playerValue = player.getValue();
            playerValue.receiveInfo(currentPlayer.canPlay());
            CardState cardState = CardState.of(Deck.of(Constants.ALL_CARDS, new Random()));

            switch (playerValue.nextTurn()) {
                case DRAW_TICKETS:
                    playerValue.receiveInfo(
                            currentPlayer.drewTickets(Constants.IN_GAME_TICKETS_COUNT));

                    // take the three first of the tickets pile
                    SortedBag<Ticket> retainedTickets =
                            playerValue.chooseTickets(
                                    SortedBag.of(
                                            tickets.toList()
                                                    .subList(0, Constants.IN_GAME_TICKETS_COUNT)));
                    playerValue.receiveInfo(currentPlayer.keptTickets(retainedTickets.size()));
                    players.forEach((Id, both) -> both.receiveInfo(nextPlayer.canPlay()));
                    break;

                case DRAW_CARDS:
                    int totalNumberOfPossibleCardsToDraw = 2;
                    for (int i = 0; i < totalNumberOfPossibleCardsToDraw; i++) {
                        if (playerValue.drawSlot() == -1)
                            players.forEach(
                                    (playerId, allPlayers) ->
                                            allPlayers.receiveInfo(currentPlayer.drewBlindCard()));
                        else
                            players.forEach(
                                    (playerId, allPlayers) ->
                                            allPlayers.receiveInfo(
                                                    currentPlayer.drewVisibleCard(
                                                            cardState.topDeckCard())));
                    }

                    players.forEach((Id, both) -> both.receiveInfo(nextPlayer.canPlay()));
                    break;

                case CLAIM_ROUTE:
                    Route claimedRoute = playerValue.claimedRoute();
                    SortedBag<Card> initialClaimCards = playerValue.initialClaimCards();
                    List<Card> drawnCards = new ArrayList<>();

                    for (int i = 0; i < Constants.ADDITIONAL_TUNNEL_CARDS; i++) {
                        drawnCards.add(cardState.topDeckCard());
                        cardState.withoutTopDeckCard();
                    }

                    SortedBag<Card> additionalCardsToPlay =
                            playerValue.chooseAdditionalCards(List.of(initialClaimCards));
                    List<Card> initialAndAdditionalCards =
                            initialClaimCards.toList().stream()
                                    .filter(additionalCardsToPlay.toList()::add)
                                    .collect(Collectors.toList());

                    if (claimedRoute.level().equals(Route.Level.OVERGROUND))
                        players.forEach(
                                (playerId, allPlayers) ->
                                        allPlayers.receiveInfo(
                                                currentPlayer.claimedRoute(
                                                        claimedRoute, initialClaimCards)));
                    else
                        players.forEach(
                                (playerId, allPlayers) ->
                                        allPlayers.receiveInfo(
                                                currentPlayer.attemptsTunnelClaim(
                                                        claimedRoute,
                                                        SortedBag.of(initialAndAdditionalCards))));
                    players.forEach(
                            (playerId, allPlayers) ->
                                    allPlayers.receiveInfo(
                                            currentPlayer.drewAdditionalCards(
                                                    SortedBag.of(drawnCards),
                                                    claimedRoute.additionalClaimCardsCount(
                                                            initialClaimCards,
                                                            SortedBag.of(drawnCards)))));

                    if (additionalCardsToPlay.isEmpty())
                        players.forEach(
                                (id, both) ->
                                        both.receiveInfo(
                                                currentPlayer.didNotClaimRoute(claimedRoute)));
                    players.forEach((Id, both) -> both.receiveInfo(nextPlayer.canPlay()));
                    break;
            }
//            PublicPlayerState a = new PublicPlayerState(tickets.size(), c)
        }
    }
}
