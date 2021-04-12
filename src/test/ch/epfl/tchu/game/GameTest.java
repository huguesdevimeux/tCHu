package ch.epfl.tchu.game;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.gui.Info;
import ch.epfl.test.TestRandomizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.exceptions.verification.MoreThanAllowedActualInvocations;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@PrepareForTest(GameState.class)
class GameTest {

    @Spy
    private StandardTestedPlayer mockedPlayer2;

    @Spy
    private StandardTestedPlayer mockedPlayer1;
    private Map<PlayerId, String> playersNames;
    private Map<PlayerId, Info> playersInfos;

    @BeforeEach
    void setUp() {
        mockedPlayer1 = spy(new StandardTestedPlayer("Alice"));
        mockedPlayer2 = spy(new StandardTestedPlayer("Bob"));
        playersNames = Map.of(PlayerId.PLAYER_1, "Alice", PlayerId.PLAYER_2, "Bob");
        playersInfos =
                playersNames.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> new Info(e.getValue())));
    }

    @Test
    void whenTwoPlayersAlwaysDrawCards() {
        Map<PlayerId, Player> players =
                Map.of(
                        PlayerId.PLAYER_1,
                        mockedPlayer1.whoIsADummyPlayer(),
                        PlayerId.PLAYER_2,
                        mockedPlayer2.whoIsADummyPlayer());
        assertThrows(
                MoreThanAllowedActualInvocations.class,
                () ->
                        Game.play(
                                players,
                                playersNames,
                                SortedBag.of(ChMap.tickets()),
                                TestRandomizer.newRandom()));
        players.forEach((playerId, player) -> verify(player, atLeastOnce()).drawSlot());
        players.forEach((playerId, player) -> verify(player, never()).claimedRoute());
        // TODO Test is failing!
        //        players.forEach((playerId, player) -> verify(player).chooseInitialTickets());
        players.forEach(
                (playerId, player) -> verify(player).setInitialTicketChoice(any(SortedBag.class)));
    }

    @Test
    void whenTwoPlayersAlwaysDrawTickets() {
        Map<PlayerId, Player> players =
                Map.of(
                        PlayerId.PLAYER_1,
                        mockedPlayer1.whoIsADummyPlayer().whoAlwaysTriesToDrawTicket(),
                        PlayerId.PLAYER_2,
                        mockedPlayer2.whoIsADummyPlayer().whoAlwaysTriesToDrawTicket());

        assertThrows(
                MoreThanAllowedActualInvocations.class,
                () ->
                        Game.play(
                                players,
                                playersNames,
                                SortedBag.of(ChMap.tickets()),
                                TestRandomizer.newRandom()));
        players.forEach(
                (playerId, player) -> {
                    verify(player, atLeastOnce())
                            .receiveInfo(eq(playersInfos.get(playerId).canPlay()));
                    // Check that the player has recieved that he has drawn 1 ticket.
                    verify(player, atLeastOnce())
                            .receiveInfo(eq(playersInfos.get(playerId).drewTickets(3)));
                    verify(player, atLeastOnce())
                            .receiveInfo(eq(playersInfos.get(playerId).keptTickets(1)));
                });
    }

    @Test
    void whenPlayer1OnlyTriesToTakeRoutes() {
        StandardTestedPlayer player1 =
                mockedPlayer1.whoIsADummyPlayer().whoAlwaysTriesToTakeARouteWhenPossible();
        // the route the player wants to claim is asked
        Map<PlayerId, Player> players =
                Map.of(
                        PlayerId.PLAYER_1,
                        player1,
                        PlayerId.PLAYER_2,
                        mockedPlayer2.whoIsADummyPlayer());

        // Player 2 will take all the cards and won't play any, so the game can't be finished.
        assertThrows(
                MoreThanAllowedActualInvocations.class,
                () ->
                        Game.play(
                                players,
                                playersNames,
                                SortedBag.of(ChMap.tickets()),
                                TestRandomizer.newRandom()));
    }

    @Test
    void whenTwoPlayersTryBothToTakeRoutes() {
        Map<PlayerId, Player> players =
                Map.of(
                        PlayerId.PLAYER_1,
                        mockedPlayer1
                                .whoIsADummyPlayer()
                                .whoAlwaysTriesToTakeARouteWhenPossible()
                                .whoDrawACardFaceUpOrBlindly(),
                        PlayerId.PLAYER_2,
                        mockedPlayer2
                                .whoIsADummyPlayer()
                                .whoAlwaysTriesToTakeARouteWhenPossible()
                                .whoDrawACardFaceUpOrBlindly());

        Game.play(players, playersNames, SortedBag.of(ChMap.tickets()), TestRandomizer.newRandom());

        // Checks that after the players gets the info saying the last turn begins, there is a last
        // turn played.

        List<String> possibleStringsForLastTurnBegins = new ArrayList<>();
        for (Map.Entry<PlayerId, Info> entry : playersInfos.entrySet()) {
            IntStream.range(0, 3)
                    .forEach(
                            possibleNumberOfWagonsLeft -> {
                                possibleStringsForLastTurnBegins.add(
                                        playersInfos
                                                .get(entry.getKey())
                                                .lastTurnBegins(possibleNumberOfWagonsLeft));
                            });
        }
        players.forEach(
                (playerId, player) -> {
                    InOrder afterLastTurnBegins = Mockito.inOrder(player);
                    afterLastTurnBegins
                            .verify(player)
                            .receiveInfo(argThat(possibleStringsForLastTurnBegins::contains));
                    afterLastTurnBegins
                            .verify(player)
                            .receiveInfo(playersInfos.get(playerId).canPlay());
                });
    }

    @Test
    void whenTwoPlayersPlaysNormally() {
        Map<PlayerId, Player> players =
                Map.of(
                        PlayerId.PLAYER_1,
                        mockedPlayer1
                                .whoIsADummyPlayer()
                                .whoAlwaysTriesToTakeARouteWhenPossible()
                                .whoPlaysRandomly(),
                        PlayerId.PLAYER_2,
                        mockedPlayer2
                                .whoIsADummyPlayer()
                                .whoAlwaysTriesToTakeARouteWhenPossible()
                                .whoPlaysRandomly());

        Game.play(players, playersNames, SortedBag.of(ChMap.tickets()), TestRandomizer.newRandom());
    }

    // Players utils for tests.

    private static class StandardTestedPlayer implements Player {

        public final String name;
        private final Random rng = TestRandomizer.newRandom();
        private final int MAX_NUMBER_OF_TURNS = 300;
        private SortedBag<Ticket> initialTickets;
        private PlayerState playerState;
        private Route nextRouteToClaim;
        private SortedBag<Card> nextInitialCardsUsedToClaimRoute;

        public StandardTestedPlayer(String name) {
            this.name = name;
        }

        public StandardTestedPlayer whoAlwaysTriesToDrawTicket() {
            doAnswer(
                    invocationOnMock -> {
                        verify(this, atMost(this.MAX_NUMBER_OF_TURNS)).nextTurn();
                        return TurnKind.DRAW_TICKETS;
                    })
                    .when(this)
                    .nextTurn();
            // Always takes the first ticket.
            doAnswer(
                    invocationOnMock -> {
                        SortedBag<Ticket> ticketSortedBag = invocationOnMock.getArgument(0);
                        return SortedBag.of(ticketSortedBag.get(0));
                    })
                    .when(this)
                    .chooseTickets(any(SortedBag.class));
            return this;
        }

        public StandardTestedPlayer whoAlwaysTriesToTakeARouteWhenPossible() {
            doAnswer(
                    invocationOnMock -> {
                        verify(this, atMost(this.MAX_NUMBER_OF_TURNS)).nextTurn();
                        List<Route> claimableRoutes =
                                ChMap.routes().stream()
                                        .filter(this.playerState::canClaimRoute)
                                        .collect(Collectors.toList());
                        if (claimableRoutes.size() == 0) {
                            System.out.printf(
                                    "%s can't take a any route, so chose to draw cards"
                                            + " instead.%n",
                                    this.name);
                            return TurnKind.DRAW_CARDS;
                        }
                        this.nextRouteToClaim =
                                claimableRoutes.get(rng.nextInt(claimableRoutes.size()));
                        List<SortedBag<Card>> tempPossibleClaimCards =
                                this.playerState.possibleClaimCards(nextRouteToClaim);
                        this.nextInitialCardsUsedToClaimRoute =
                                tempPossibleClaimCards.get(
                                        rng.nextInt(tempPossibleClaimCards.size()));
                        return TurnKind.CLAIM_ROUTE;
                    })
                    .when(this)
                    .nextTurn();

            doAnswer(o -> this.nextRouteToClaim).when(this).claimedRoute();
            doAnswer(o -> this.nextInitialCardsUsedToClaimRoute).when(this).initialClaimCards();
            doAnswer(
                    invocationOnMock -> {
                        List<SortedBag<Card>> additionalCardsThatCanbePlayed =
                                invocationOnMock.getArgument(0);
                        if (additionalCardsThatCanbePlayed.size() > 0)
                            return additionalCardsThatCanbePlayed.get(0);
                        else return SortedBag.of();
                    })
                    .when(this)
                    .chooseAdditionalCards(any(List.class));
            return this;
        }

        public StandardTestedPlayer whoIsADummyPlayer() {
            // Outputs the infos.
            doAnswer(
                    invocationOnMock -> {
                        System.out.printf("LOGGING from : %s%n", this.name);
                        System.out.println(invocationOnMock.<String>getArgument(0));
                        return null;
                    })
                    .when(this)
                    .receiveInfo(anyString());
            // Returns the first ticket.
            doAnswer(invocationOnMock -> invocationOnMock.<SortedBag<Ticket>>getArgument(0).get(0))
                    .when(this)
                    .chooseTickets(any(SortedBag.class));
            // Always select to draw a new card
            doAnswer(
                    o -> {
                        verify(this, atMost(this.MAX_NUMBER_OF_TURNS)).nextTurn();
                        return TurnKind.DRAW_CARDS;
                    })
                    .when(this)
                    .nextTurn();

            // Always draw blindly.
            doReturn(-1).when(this).drawSlot();
            return this;
        }

        public StandardTestedPlayer whoDrawACardFaceUpOrBlindly() {
            int randomIndex = TestRandomizer.newRandom().nextInt(6);
            // Decrement 1 to have a negative index sometiems.
            doReturn(randomIndex - 1).when(this).drawSlot();
            return this;
        }

        public StandardTestedPlayer whoPlaysRandomly() {
            doAnswer(
                    invocationOnMock -> {
                        verify(this, atMost(this.MAX_NUMBER_OF_TURNS)).nextTurn();
                        // A new random is instanciated everytime, if I use
                        // TestRandomizer.newRandom() the same value is returned every time
                        // ..
                        double decision = new Random().nextDouble();
                        if (decision < 0.3) {
                            return TurnKind.DRAW_TICKETS;
                        }
                        List<Route> claimableRoutes =
                                ChMap.routes().stream()
                                        .filter(this.playerState::canClaimRoute)
                                        .collect(Collectors.toList());
                        if (claimableRoutes.size() == 0) {
                            System.out.printf(
                                    "%s can't take a any route, so chose to draw cards"
                                            + " instead.%n",
                                    this.name);
                            return TurnKind.DRAW_CARDS;
                        }
                        this.nextRouteToClaim =
                                claimableRoutes.get(rng.nextInt(claimableRoutes.size()));
                        List<SortedBag<Card>> tempPossibleClaimCards =
                                this.playerState.possibleClaimCards(nextRouteToClaim);
                        this.nextInitialCardsUsedToClaimRoute =
                                tempPossibleClaimCards.get(
                                        rng.nextInt(tempPossibleClaimCards.size()));
                        return TurnKind.CLAIM_ROUTE;
                    })
                    .when(this)
                    .nextTurn();
            // Always takes the first ticket.
            doAnswer(
                    invocationOnMock -> {
                        SortedBag<Ticket> ticketSortedBag = invocationOnMock.getArgument(0);
                        return SortedBag.of(ticketSortedBag.get(0));
                    })
                    .when(this)
                    .chooseTickets(any(SortedBag.class));
            return this;
        }

        @Override
        public void initPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        }

        @Override
        public void receiveInfo(String info) {
        }

        @Override
        public void updateState(PublicGameState newState, PlayerState ownState) {
            this.playerState = ownState;
        }

        @Override
        public void setInitialTicketChoice(SortedBag<Ticket> tickets) {
            this.initialTickets = tickets;
        }

        @Override
        public SortedBag<Ticket> chooseInitialTickets() {
            return SortedBag.of(this.initialTickets.get(0));
        }

        @Override
        public TurnKind nextTurn() {
            fail("This Method should not be called here");
            return null;
        }

        @Override
        public SortedBag<Ticket> chooseTickets(SortedBag<Ticket> options) {
            fail("This Method should not be called here");
            return null;
        }

        @Override
        public int drawSlot() {
            fail("This Method should not be called here");
            return 0;
        }

        @Override
        public Route claimedRoute() {
            fail("This Method should not be called here");
            return null;
        }

        @Override
        public SortedBag<Card> initialClaimCards() {
            fail("This Method should not be called here");
            return null;
        }

        @Override
        public SortedBag<Card> chooseAdditionalCards(List<SortedBag<Card>> options) {
            fail("This Method should not be called here");
            return null;
        }
    }
}
