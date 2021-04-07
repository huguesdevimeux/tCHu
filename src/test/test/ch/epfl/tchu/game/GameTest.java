package ch.epfl.tchu.game;

import ch.epfl.tchu.SortedBag;
import ch.epfl.test.TestRandomizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@PrepareForTest(GameState.class)
class GameTest {

    @Spy
    private StandardTestedPlayer mockedPlayer2;

    @Spy
    private StandardTestedPlayer mockedPlayer1;
    private Map<PlayerId, String> playersNames;

    @BeforeEach
    void setUp() {
        mockedPlayer1 = spy(new StandardTestedPlayer(PlayerId.PLAYER_1, "Alice"));
        mockedPlayer2 = spy(new StandardTestedPlayer(PlayerId.PLAYER_2, "Bob"));
        playersNames = Map.of(PlayerId.PLAYER_1, "Alice", PlayerId.PLAYER_2, "Bob");
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
        Game.play(players, playersNames, SortedBag.of(ChMap.tickets()), TestRandomizer.newRandom());

        // NOTE : This fails because when player_1 is saying he wants to take a route, player2 is
        // asked to get which route.
    }

    @Test
    void whenTwoPlayersTryBothToTakeRoutes() {
        Map<PlayerId, Player> players =
                Map.of(
                        PlayerId.PLAYER_1,
                        mockedPlayer1.whoIsADummyPlayer().whoAlwaysTriesToTakeARouteWhenPossible(),
                        PlayerId.PLAYER_2,
                        mockedPlayer2.whoIsADummyPlayer().whoAlwaysTriesToTakeARouteWhenPossible());

        Game.play(players, playersNames, SortedBag.of(ChMap.tickets()), TestRandomizer.newRandom());
    }


    // Players utils for tests.

    private static class StandardTestedPlayer implements Player {

        public final String name;
        private final PlayerId ownId;
        private SortedBag<Ticket> initialTickets;
        private PublicGameState gameState;
        private PlayerState playerState;

        private Route nextRouteToClaim;
        private SortedBag<Card> nextInitialCardsUsedToClaimRoute;

        public StandardTestedPlayer(PlayerId ownId, String name) {
            this.ownId = ownId;
            this.name = name;
        }

        public StandardTestedPlayer whoAlwaysTriesToTakeARouteWhenPossible() {
            doAnswer(
                    invocationOnMock -> {
                        List<Route> claimableRoutes =
                                ChMap.routes().stream()
                                        .filter(this.playerState::canClaimRoute)
                                        .collect(Collectors.toList());
                        if (claimableRoutes.size() == 0) {
                            return TurnKind.DRAW_CARDS;
                        }
                        this.nextRouteToClaim = claimableRoutes.get(0);
                        this.nextInitialCardsUsedToClaimRoute =
                                this.playerState
                                        .possibleClaimCards(nextRouteToClaim)
                                        .get(0);
                        return TurnKind.CLAIM_ROUTE;
                    })
                    .when(this)
                    .nextTurn();

            doAnswer(o -> this.nextRouteToClaim).when(this).claimedRoute();
            doAnswer(o -> this.nextInitialCardsUsedToClaimRoute).when(this).initialClaimCards();
            doAnswer(invocationOnMock -> invocationOnMock.<List<SortedBag<Card>>>getArgument(0).get(0)).when(this).chooseAdditionalCards(any(List.class));
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
            doAnswer(o -> TurnKind.DRAW_CARDS).when(this).nextTurn();
            // Always draw blindly.
            doReturn(-1).when(this).drawSlot();
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
            this.gameState = newState;
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
