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

        Info player1 = new Info(firstPlayer.name());
        Info player2 = new Info(firstPlayer.next().name());

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
            player.getValue().receiveInfo(new Info(player.getKey().name()).canPlay());
            switch (player.getValue().nextTurn()) {
                case DRAW_TICKETS:
                    player.getValue()
                            .receiveInfo(
                                    new Info(player.getKey().name())
                                            .drewTickets(Constants.IN_GAME_TICKETS_COUNT));
                    player.getValue().chooseTickets(tickets);
                    player.getValue()
                            .receiveInfo(
                                    new Info(player.getKey().name())
                                            .keptTickets(
                                                    player.getValue()
                                                            .chooseInitialTickets()
                                                            .size()));

                    break;
                case DRAW_CARDS:
                    CardState cardState = CardState.of(Deck.of(Constants.ALL_CARDS, new Random()));
                    int totalNumberOfPossibleCardsToDraw = 2;
                    for (int i = 0; i < totalNumberOfPossibleCardsToDraw; i++) {
                        if (player.getValue().drawSlot() == -1)
                            player.getValue()
                                    .receiveInfo(new Info(player.getKey().name()).drewBlindCard());
                        else
                            player.getValue()
                                    .receiveInfo(
                                            new Info(player.getKey().name())
                                                    .drewVisibleCard(cardState.topDeckCard()));
                    }
                case CLAIM_ROUTE:
                    player.getValue().claimedRoute();
                    player.getValue().initialClaimCards();
                    player.getValue()
                            .chooseAdditionalCards(List.of(player.getValue().initialClaimCards()));
                    break;
            }
        }
    }
}
