package ch.epfl.tchu.net;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import static ch.epfl.tchu.game.PlayerId.PLAYER_1;
import static ch.epfl.tchu.game.PlayerId.PLAYER_2;
import static ch.epfl.tchu.net.AttributesForTest.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class RemotePlayerTest {

    @Test
    public void testNet() throws InterruptedException {
        RemotePlayerTest.Server server = new RemotePlayerTest.Server(PlayerId.PLAYER_1, players,
                "info", pgs, player2State, Player.TurnKind.CLAIM_ROUTE, 2,
                ChMap.routes().get(5), SortedBag.of(Card.ALL));
        RemotePlayerTest.Client client = new RemotePlayerTest.Client(PlayerId.PLAYER_1, players,
                "info", pgs, player2State, Player.TurnKind.CLAIM_ROUTE, 2,
                ChMap.routes().get(5), SortedBag.of(Card.ALL));

        Thread serverThread = new Thread(server);
        Thread clientThread = new Thread(client);
        serverThread.start();
        clientThread.start();
      serverThread.join();
      clientThread.join();

    }
    static class Server implements Runnable {
        private static PlayerId expectedPlayerId;
        private static Map<PlayerId, String> expectedPlayerNames;
        private static String expectedInfos;
        private static PublicGameState expectedPGS;
        private static PlayerState expectedOwnState;
        private static Player.TurnKind nextTurn;
        private static int drawSlot;
        private static Route claimedRoute;
        private static SortedBag<Card> initialClaimCards;
        public static boolean hasFinishedRunning = false;

        public Server(PlayerId expectedPlayerId, Map<PlayerId, String> expectedPlayerNames,
                      String expectedInfos, PublicGameState expectedPGS,
                      PlayerState expectedOwnState, Player.TurnKind nextTurn,
                      int drawSlot, Route claimedRoute, SortedBag<Card> initialClaimCard) {
            Server.expectedPlayerId = expectedPlayerId;
            Server.expectedPlayerNames = expectedPlayerNames;
            Server.expectedInfos = expectedInfos;
            Server.expectedPGS = expectedPGS;
            Server.expectedOwnState = expectedOwnState;
            Server.nextTurn = nextTurn;
            Server.drawSlot = drawSlot;
            Server.claimedRoute = claimedRoute;
            Server.initialClaimCards = initialClaimCard;
        }

        public void run() {
            String INFO = "info";
            System.out.println("Starting server!");
            try (ServerSocket serverSocket = new ServerSocket(5108);
                 Socket socket = serverSocket.accept()) {
                Player playerProxy = new RemotePlayerProxy(socket);
                playerProxy.initPlayers(PLAYER_1, players);
                playerProxy.receiveInfo(INFO);
                playerProxy.updateState(pgs, player2State);
                playerProxy.setInitialTicketChoice(SortedBag.of(ChMap.tickets().subList(0, 5)));
                assertEquals(nextTurn, playerProxy.nextTurn());
                assertEquals(drawSlot, playerProxy.drawSlot());
                assertEquals(3, playerProxy.chooseTickets(SortedBag.of(ChMap.tickets().subList(0, 5))).size());
                assertEquals(claimedRoute, playerProxy.claimedRoute());
                assertEquals(initialClaimCards, playerProxy.initialClaimCards());
                assertEquals(2, playerProxy.chooseInitialTickets().size());
                assertEquals(6, playerProxy.chooseAdditionalCards(List.of(SortedBag.of(2, Card.BLUE, 4, Card.BLACK),
                        SortedBag.of(1, Card.VIOLET))).size());
                hasFinishedRunning = true;
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Server done!");
        }
    }

    static class Client implements Runnable {
        private static PlayerId expectedPlayerId;
        private static Map<PlayerId, String> expectedPlayerNames;
        private static String expectedInfos;
        private static PublicGameState expectedPGS;
        private static PlayerState expectedOwnState;
        private static Player.TurnKind nextTurn;
        private static int drawSlot;
        private static Route claimedRoute;
        private static SortedBag<Card> initialClaimCards;
        public static boolean hasFinishedRunning = false;

        public Client(PlayerId expectedPlayerId, Map<PlayerId, String> expectedPlayerNames,
                      String expectedInfos, PublicGameState expectedPGS,
                      PlayerState expectedOwnState, Player.TurnKind nextTurn,
                      int drawSlot, Route claimedRoute, SortedBag<Card> initialClaimCard) {
            Client.expectedPlayerId = expectedPlayerId;
            Client.expectedPlayerNames = expectedPlayerNames;
            Client.expectedInfos = expectedInfos;
            Client.expectedPGS = expectedPGS;
            Client.expectedOwnState = expectedOwnState;
            Client.nextTurn = nextTurn;
            Client.drawSlot = drawSlot;
            Client.claimedRoute = claimedRoute;
            Client.initialClaimCards = initialClaimCard;
        }

        public void run() {
            RemotePlayerClient playerClient = new RemotePlayerClient(new TestPlayer(
                    expectedPlayerId, expectedPlayerNames, expectedInfos, expectedPGS, expectedOwnState,
                    nextTurn, drawSlot, claimedRoute, initialClaimCards), "localhost", 5108);
            playerClient.run();
            hasFinishedRunning = true;
        }
    }

    static class TestPlayer implements Player {
        private PlayerId expectedPlayerId;
        private Map<PlayerId, String> expectedPlayerNames;
        private String expectedInfos;
        private PublicGameState expectedPGS;
        private PlayerState expectedOwnState;
        private SortedBag<Ticket> initialTicketsChoice;
        private TurnKind nextTurn;
        private int drawSlot;
        private Route claimedRoute;
        private SortedBag<Card> initialClaimCards;

        public TestPlayer(PlayerId expectedPlayerId, Map<PlayerId, String> expectedPlayerNames, String expectedInfos,
                          PublicGameState expectedPGS, PlayerState expectedOwnState, TurnKind nextTurn,
                          int drawSlot, Route claimedRoute, SortedBag<Card> initialClaimCards) {
            this.expectedPlayerId = expectedPlayerId;
            this.expectedPlayerNames = expectedPlayerNames;
            this.expectedInfos = expectedInfos;
            this.expectedPGS = expectedPGS;
            this.expectedOwnState = expectedOwnState;
            this.nextTurn = nextTurn;
            this.drawSlot = drawSlot;
            this.claimedRoute = claimedRoute;
            this.initialClaimCards = initialClaimCards;
        }

        @Override
        public void initPlayers(PlayerId ownId,
                                Map<PlayerId, String> names) {
            assertEquals(expectedPlayerId, ownId);
            assertEquals(expectedPlayerNames, names);
        }

        @Override
        public void receiveInfo(String info) {
            assertEquals(expectedInfos, info);
        }

        @Override
        public void updateState(PublicGameState newState, PlayerState ownState) {
            assertPublicGameStatesAreEqual(expectedPGS, newState);
            assertPlayerStateEquals(expectedOwnState, ownState);
        }

        @Override
        public void setInitialTicketChoice(SortedBag<Ticket> tickets) {
            this.initialTicketsChoice = tickets;
        }

        @Override
        public SortedBag<Ticket> chooseInitialTickets() {
            return SortedBag.of(2, this.initialTicketsChoice.get(0));
        }

        @Override
        public TurnKind nextTurn() {
            return nextTurn;
        }

        @Override
        public SortedBag<Ticket> chooseTickets(SortedBag<Ticket> options) {
            return SortedBag.of(List.of(options.get(0), options.get(1), options.get(2)));
        }

        @Override
        public int drawSlot() {
            return drawSlot;
        }

        @Override
        public Route claimedRoute() {
            return claimedRoute;
        }

        @Override
        public SortedBag<Card> initialClaimCards() {
            return initialClaimCards;
        }

        @Override
        public SortedBag<Card> chooseAdditionalCards(List<SortedBag<Card>> options) {
            return options.get(0);
        }
    }

    private static void assertPublicGameStatesAreEqual(PublicGameState expected, PublicGameState current) {
        assertEquals(expected.currentPlayerId(), current.currentPlayerId());
        assertEquals(expected.lastPlayer(), current.lastPlayer());
        assertEquals(expected.ticketsCount(), current.ticketsCount());
        assertPublicCardStatesEquals(expected.cardState(), current.cardState());
        for (PlayerId pid : PlayerId.ALL) {
            assertPublicPlayerStateEquals(expected.playerState(pid), current.playerState(pid));
        }
    }

    private static void assertPublicCardStatesEquals(PublicCardState expected, PublicCardState current) {
        assertEquals(expected.faceUpCards(), current.faceUpCards());
        assertEquals(expected.deckSize(), current.deckSize());
        assertEquals(expected.discardsSize(), current.discardsSize());
    }

    private static void assertPublicPlayerStateEquals(PublicPlayerState expected, PublicPlayerState current) {
        assertEquals(expected.ticketCount(), current.ticketCount());
        assertEquals(expected.cardCount(), current.cardCount());
        assertEquals(expected.routes(), current.routes());
    }

    private static void assertPlayerStateEquals(PlayerState expected, PlayerState current) {
        assertPublicPlayerStateEquals(expected, current);
        assertEquals(expected.tickets(), current.tickets());
        assertEquals(expected.cards(), current.cards());
    }
}

class AttributesForTest {
    static PublicPlayerState player1State = new PublicPlayerState(4, 2, ChMap.routes().subList(0, 3));
    static PlayerState player2State = new PlayerState(SortedBag.of(ChMap.tickets().subList(0, 4)), SortedBag.of(Card.ALL), ChMap.routes().subList(0, 2));
    static PublicCardState cardState = new PublicCardState(Card.ALL.subList(0, 5), 2, 3);
    static PublicGameState pgs = new PublicGameState(2, cardState, PLAYER_1, Map.of(PLAYER_1, player1State, PLAYER_2, player2State), null);
    static Map<PlayerId, String> players = Map.of(PLAYER_1, "Ada",
            PLAYER_2, "Charles");
}


