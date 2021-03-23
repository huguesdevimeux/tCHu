package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.gui.Info;

import java.util.List;
import java.util.Map;
import java.util.Random;

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
        players.forEach((playerId, player) -> player.initPlayers(playerId, playerNames));
        PlayerId firstPlayer = PlayerId.ALL.get(rng.nextInt(PlayerId.ALL.size()));

        Info currentPlayer = new Info(firstPlayer.name());
        Info nextPlayer = new Info(firstPlayer.next().name());

        players.forEach(
                (playerId, player) ->
                        player.receiveInfo(new Info(firstPlayer.name()).willPlayFirst()));
        players.forEach((playerId, player) -> player.setInitialTicketChoice(tickets));
        players.forEach((playerId, player) -> player.chooseInitialTickets());

        players.forEach(
                (playerId, player) ->
                        player.receiveInfo(
                                new Info(playerId.name())
                                        .keptTickets(
                                                tickets.size()
                                                        - player.chooseInitialTickets().size())));

        for (Map.Entry<PlayerId, Player> player : players.entrySet()) {
            player.getValue().receiveInfo(currentPlayer.canPlay());
            Player playerValue = player.getValue();

            switch (player.getValue().nextTurn()) {
                case DRAW_TICKETS:
                    playerValue.receiveInfo(
                            currentPlayer.drewTickets(Constants.IN_GAME_TICKETS_COUNT));

                    playerValue.chooseTickets(tickets);
                    playerValue.receiveInfo(
                            currentPlayer.keptTickets(
                                    player.getValue().chooseInitialTickets().size()));

                    playerValue.receiveInfo(nextPlayer.canPlay());
                    break;

                case DRAW_CARDS:
                    CardState cardState = CardState.of(Deck.of(Constants.ALL_CARDS, new Random()));
                    int totalNumberOfPossibleCardsToDraw = 2;
                    for (int i = 0; i < totalNumberOfPossibleCardsToDraw; i++) {
                        if (playerValue.drawSlot() == -1)
                            playerValue.receiveInfo(currentPlayer.drewBlindCard());
                        else
                            playerValue.receiveInfo(
                                    currentPlayer.drewVisibleCard(cardState.topDeckCard()));
                    }
                    playerValue.receiveInfo(nextPlayer.canPlay());
                    break;

                case CLAIM_ROUTE:
                    Route claimedRoute = playerValue.claimedRoute();
                    SortedBag<Card> initialClaimCards = playerValue.initialClaimCards();
                    int drawnCardsSize = playerValue.claimedRoute().length();
                    playerValue.chooseAdditionalCards(List.of(initialClaimCards));

                    if (claimedRoute.level().equals(Route.Level.OVERGROUND))
                        playerValue.receiveInfo(
                                currentPlayer.claimedRoute(claimedRoute, initialClaimCards));
                    else
                        //   for(int i = 0; i < drawnCardsSize; i++){
                        playerValue.receiveInfo(
                                currentPlayer.attemptsTunnelClaim(
                                        claimedRoute,
                                        initialClaimCards)); // TODO have to use drawnCards
                    // but where do u get it? &&
                    // currentPlayer.drewAdditionalCards();

                    playerValue.receiveInfo(nextPlayer.canPlay());
                    break;
            }
        }
    }
}
