package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.gui.Info;

import java.util.*;

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
    }
}
