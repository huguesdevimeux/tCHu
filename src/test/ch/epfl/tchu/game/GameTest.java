package ch.epfl.tchu.game;

import ch.epfl.tchu.SortedBag;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

import static ch.epfl.tchu.game.GameTest.PlayerMethod.*;
import static org.junit.jupiter.api.Assertions.*;

class GameTest {
    enum PlayerMethod {
        INIT_PLAYERS,
        RECEIVE_INFO,
        UPDATE_STATE,
        SET_INITIAL_TICKET_CHOICE,
        CHOOSE_INITIAL_TICKETS,
        NEXT_TURN,
        CHOOSE_TICKETS,
        DRAW_SLOT,
        CLAIMED_ROUTE,
        INITIAL_CLAIM_CARDS,
        CHOOSE_ADDITIONAL_CARDS
    }

//    @BeforeAll
//    static void redirectSystemOut() {
//        System.setOut(new PrintStream(OutputStream.nullOutputStream()));
//        System.setErr(new PrintStream(OutputStream.nullOutputStream()));
//    }

    @Test
    void gamePlayFailsWithNotEnoughPlayers() {
        var playerNames = Map.of(PlayerId.PLAYER_1, "1", PlayerId.PLAYER_2, "2");

        assertThrows(IllegalArgumentException.class, () -> {
            Game.play(Map.of(), playerNames, SortedBag.of(), new Random(2021));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            Game.play(
                    Map.of(PlayerId.PLAYER_1, new TestPlayer(0, List.of())),
                    playerNames,
                    SortedBag.of(),
                    new Random(2021));
        });
    }

    @Test
    void gamePlayFailsWithNotEnoughPlayerNames() {
        var players = Map.of(
                PlayerId.PLAYER_1, (Player) new TestPlayer(0, List.of()),
                PlayerId.PLAYER_2, (Player) new TestPlayer(0, List.of()));

        assertThrows(IllegalArgumentException.class, () -> {
            Game.play(players, Map.of(), SortedBag.of(), new Random(2021));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            Game.play(
                    players,
                    Map.of(PlayerId.PLAYER_1, "1"),
                    SortedBag.of(),
                    new Random(2021));
        });
    }

    @Test
    void gamePlayCallsInitPlayersExactlyOnce() {
        var unusedPlayerIds = EnumSet.allOf(PlayerId.class);
        var playerNames = (Map<PlayerId, String>) null;
        for (var player : playRandomGame(2020)) {
            var callSummary = player.callSummary();
            assertEquals(1, callSummary.get(PlayerMethod.INIT_PLAYERS));

            unusedPlayerIds.remove(player.ownId);
            if (playerNames != null) assertEquals(playerNames, player.playerNames);
            playerNames = player.playerNames;
        }
        assertEquals(Set.of(), unusedPlayerIds);
    }

    @Test
    void gamePlayCallsReceiveInfoOftenEnough() {
        var players = playRandomGame(2021);

        var receiveInfo1 = (int) players.get(0).callSummary().get(RECEIVE_INFO);
        var receiveInfo2 = (int) players.get(1).callSummary().get(RECEIVE_INFO);
        assertEquals(receiveInfo1, receiveInfo2);
        assertTrue(100 <= receiveInfo1 && receiveInfo1 <= 1_000);
    }


    @Test
    void gamePlayCallsUpdateStateOftenEnough() {
        var players = playRandomGame(2022);

        var updateState1 = (int) players.get(0).callSummary().get(UPDATE_STATE);
        var updateState2 = (int) players.get(1).callSummary().get(UPDATE_STATE);
        assertEquals(updateState1, updateState2);
        assertTrue(100 <= updateState1 && updateState1 <= 1_000);
    }

    @Test
    void gamePlayCallsSetInitialTicketChoice() {
        for (var player : playRandomGame(2023)) {
            var callSummary = player.callSummary();
            assertEquals(1, callSummary.get(SET_INITIAL_TICKET_CHOICE));
            assertEquals(5, player.allTicketsSeen.getFirst().size());
        }
    }

    @Test
    void gamePlayCallsSetInitialTicketChoiceFirstThenChooseInitialTickets() {
        for (var player : playRandomGame(2024)) {
            var filteredCalls = player.calls.stream()
                    .filter(m -> m != INIT_PLAYERS && m != RECEIVE_INFO && m != UPDATE_STATE)
                    .limit(3)
                    .collect(Collectors.toList());
            assertEquals(
                    List.of(SET_INITIAL_TICKET_CHOICE, CHOOSE_INITIAL_TICKETS, NEXT_TURN),
                    filteredCalls);
        }
    }

    @Test
    void gamePlayCallsChooseTicketsWhenPlayerDrawsTickets() {
        var ticketBagB = new SortedBag.Builder<Ticket>();
        for (var player : playRandomGame(2025)) {
            var drawTicketsTurnsCount = player.allTurns.stream()
                    .filter(Player.TurnKind.DRAW_TICKETS::equals)
                    .count();
            var chooseTicketsCallsCount = player.calls.stream()
                    .filter(CHOOSE_TICKETS::equals)
                    .count();
            assertTrue(drawTicketsTurnsCount > 0);
            assertEquals(drawTicketsTurnsCount, chooseTicketsCallsCount);

            player.allTicketsSeen.forEach(ticketBagB::add);
        }
        var allTicketsBag = SortedBag.of(ChMap.ALL_TICKETS);
        assertEquals(allTicketsBag, ticketBagB.build());
    }

    @Test
    void gamePlayUpdatesStateBetweenCardDraws() {
        for (var player : playRandomGame(2026)) {
            var filteredCallsIt = player.calls.stream()
                    .filter(m -> m == UPDATE_STATE || m == DRAW_SLOT)
                    .iterator();
            var prevCall = UPDATE_STATE;
            while (filteredCallsIt.hasNext()) {
                var call = filteredCallsIt.next();
                if (call == DRAW_SLOT)
                    assertEquals(UPDATE_STATE, prevCall);
                prevCall = call;
            }
        }
    }

    @Test
    void gamePlayCallsChooseAdditionalCardsAtLeastOnce() {
        for (var player : playRandomGame(2027))
            assertNotEquals(0, player.callSummary().get(CHOOSE_ADDITIONAL_CARDS));
    }

    @Test
    void gamePlayCallsChooseAdditionalCardsRightAfterClaimedRouteOrInitialClaimCardsOnly() {
        for (var player : playRandomGame(2028)) {
            var filteredCallsIt = player.calls.stream()
                    .filter(m -> m != RECEIVE_INFO && m != UPDATE_STATE)
                    .iterator();
            var prevCall = INIT_PLAYERS;
            while (filteredCallsIt.hasNext()) {
                var call = filteredCallsIt.next();
                if (call == CHOOSE_ADDITIONAL_CARDS)
                    assertTrue(prevCall == CLAIMED_ROUTE || prevCall == INITIAL_CLAIM_CARDS);
                prevCall = call;
            }
        }
    }

    @Test
    void gamePlayProperlyAnnouncesRouteClaims() {
        for (var player : playRandomGame(2029)) {
            var playerClaimedRoute = player.ownName() + " a pris possession de la route ";

            var expectedInfosB = new SortedBag.Builder<String>();
            player.ownState().routes().stream()
                    .map(r -> playerClaimedRoute + r.station1() + " – " + r.station2())
                    .forEach(expectedInfosB::add);
            var expectedInfos = expectedInfosB.build();

            var actualInfosB = new SortedBag.Builder<String>();
            player.allInfos.stream()
                    .filter(i -> i.startsWith(playerClaimedRoute))
                    .map(i -> i.substring(0, i.indexOf(" au moyen de ")))
                    .forEach(actualInfosB::add);
            var actualInfos = actualInfosB.build();

            assertEquals(expectedInfos, actualInfos);
        }
    }

    @Test
    void gamePlayProperlyCommunicatesLastTurn() {
        for (var player : playRandomGame(2030)) {
            var lastTurnInfoCount = player.allInfos.stream()
                    .filter(i -> i.contains("le dernier tour commence"))
                    .count();
            assertEquals(1, lastTurnInfoCount);
        }
    }

    @Test
    void gamePlayProperlyHandlesLastTurn() {
        for (var player : playRandomGame(2031)) {
            var lastTurnsCount = player.allInfos.stream()
                    .dropWhile(i -> !i.contains("le dernier tour commence"))
                    .filter(i -> i.startsWith("\nC'est à"))
                    .count();
            assertEquals(2, lastTurnsCount);
        }
    }

    @Test
    void gamePlayProperlyCommunicatesLongestTrailBonus() {
        for (var player : playRandomGame(2032)) {
            var bonusInfoCount = player.allInfos.stream()
                    .filter(i -> i.contains("reçoit un bonus de 10 points"))
                    .count();
            assertTrue(1 <= bonusInfoCount && bonusInfoCount <= 2);
        }
    }

    @Test
    void gamePlayProperlyCommunicatesResult() {
        for (var player : playRandomGame(2033)) {
            var outcomeInfo = player.allInfos.stream()
                    .filter(i -> i.contains("remporte la victoire") || i.contains("sont ex æqo"))
                    .collect(Collectors.toList());
            assertEquals(1, outcomeInfo.size());
            assertEquals(player.allInfos.getLast(), outcomeInfo.get(0));
        }
    }

    private static List<TestPlayer> playRandomGame(long randomSeed) {
        var rng = new Random(randomSeed);
        var routes = ChMap.ALL_ROUTES;
        var tickets = ChMap.ALL_TICKETS;
        var p1 = new TestPlayer(rng.nextLong(), routes);
        var p2 = new TestPlayer(rng.nextLong(), routes);
        var players = Map.of(
                PlayerId.PLAYER_1, (Player) p1,
                PlayerId.PLAYER_2, (Player) p2);
        var playerNames = Map.of(
                PlayerId.PLAYER_1, "Ada",
                PlayerId.PLAYER_2, "Charles");
        Game.play(players, playerNames, SortedBag.of(tickets), rng);
        return List.of(p1, p2);
    }

    private static final class TooManyCallsError extends Error {
    }

    private static final class TestPlayer implements Player {
        private static final int CALLS_LIMIT = 10_000;
        private static final int MIN_CARD_COUNT = 16;
        private static final int DRAW_TICKETS_ODDS = 15;
        private static final int ABANDON_TUNNEL_ODDS = 10;
        private static final int DRAW_ALL_TICKETS_TURN = 30;

        private final Random rng;
        private final List<Route> allRoutes;

        private final Deque<PlayerMethod> calls = new ArrayDeque<>();

        private final Deque<TurnKind> allTurns = new ArrayDeque<>();
        private final Deque<String> allInfos = new ArrayDeque<>();
        private final Deque<PublicGameState> allGameStates = new ArrayDeque<>();
        private final Deque<PlayerState> allOwnStates = new ArrayDeque<>();
        private final Deque<SortedBag<Ticket>> allTicketsSeen = new ArrayDeque<>();

        private PlayerId ownId;
        private Map<PlayerId, String> playerNames;

        private Route routeToClaim;
        private SortedBag<Card> initialClaimCards;

        private void registerCall(PlayerMethod key) {
            calls.add(key);
            if (calls.size() >= CALLS_LIMIT)
                throw new TooManyCallsError();
        }

        private Map<PlayerMethod, Integer> callSummary() {
            var summary = new EnumMap<PlayerMethod, Integer>(PlayerMethod.class);
            calls.forEach(c -> summary.merge(c, 1, Integer::sum));
            return summary;
        }

        private PublicGameState gameState() {
            return allGameStates.getLast();
        }

        private PlayerState ownState() {
            return allOwnStates.getLast();
        }

        private String ownName() {
            if (ownId == null || playerNames == null)
                return "<anonyme>";
            else
                return playerNames.getOrDefault(ownId, "<anonyme>");
        }

        public TestPlayer(long randomSeed, List<Route> allRoutes) {
            this.rng = new Random(randomSeed);
            this.allRoutes = List.copyOf(allRoutes);
        }

        @Override
        public void initPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
            registerCall(PlayerMethod.INIT_PLAYERS);
            this.ownId = ownId;
            this.playerNames = Map.copyOf(playerNames);
        }

        @Override
        public void receiveInfo(String info) {
            registerCall(RECEIVE_INFO);
            allInfos.addLast(info);
        }

        @Override
        public void updateState(PublicGameState newState, PlayerState ownState) {
            registerCall(UPDATE_STATE);
            allGameStates.addLast(newState);
            allOwnStates.addLast(ownState);
        }

        @Override
        public void setInitialTicketChoice(SortedBag<Ticket> tickets) {
            registerCall(SET_INITIAL_TICKET_CHOICE);
            allTicketsSeen.addLast(tickets);
        }

        @Override
        public SortedBag<Ticket> chooseInitialTickets() {
            registerCall(PlayerMethod.CHOOSE_INITIAL_TICKETS);
            return allTicketsSeen.peekFirst();
        }

        @Override
        public TurnKind nextTurn() {
            registerCall(PlayerMethod.NEXT_TURN);

            var turn = doNextTurn();
            allTurns.addLast(turn);
            return turn;
        }

        private TurnKind doNextTurn() {
            var gameState = gameState();
            if (gameState.canDrawTickets()
                    && (allTurns.size() >= DRAW_ALL_TICKETS_TURN
                    || rng.nextInt(DRAW_TICKETS_ODDS) == 0))
                return TurnKind.DRAW_TICKETS;

            var ownState = ownState();
            var claimedRoutes = new HashSet<>(gameState.claimedRoutes());
            var claimableRoutes = allRoutes.stream()
                    .filter(r -> !claimedRoutes.contains(r))
                    .filter(ownState::canClaimRoute)
                    .collect(Collectors.toCollection(ArrayList::new));
            if (claimableRoutes.isEmpty() || ownState.cardCount() < MIN_CARD_COUNT) {
                return TurnKind.DRAW_CARDS;
            } else {
                var route = claimableRoutes.get(rng.nextInt(claimableRoutes.size()));
                for (int i = 0; i < 3 && route.level() == Route.Level.OVERGROUND; i++) {
                    // slightly favor tunnels
                    route = claimableRoutes.get(rng.nextInt(claimableRoutes.size()));
                }

                var cards = ownState.possibleClaimCards(route);

                routeToClaim = route;
                initialClaimCards = cards.isEmpty() ? null : cards.get(0);
                return TurnKind.CLAIM_ROUTE;
            }
        }

        @Override
        public SortedBag<Ticket> chooseTickets(SortedBag<Ticket> options) {
            registerCall(PlayerMethod.CHOOSE_TICKETS);

            allTicketsSeen.addLast(options);

            var shuffledOptions = new ArrayList<>(options.toList());
            Collections.shuffle(shuffledOptions, rng);
            var ticketsToKeep = 1 + rng.nextInt(options.size());
            return SortedBag.of(shuffledOptions.subList(0, ticketsToKeep));
        }

        @Override
        public int drawSlot() {
            registerCall(PlayerMethod.DRAW_SLOT);
            return rng.nextInt(6) - 1;
        }

        @Override
        public Route claimedRoute() {
            registerCall(PlayerMethod.CLAIMED_ROUTE);
            return routeToClaim;
        }

        @Override
        public SortedBag<Card> initialClaimCards() {
            registerCall(PlayerMethod.INITIAL_CLAIM_CARDS);
            return initialClaimCards;
        }

        @Override
        public SortedBag<Card> chooseAdditionalCards(List<SortedBag<Card>> options) {
            registerCall(PlayerMethod.CHOOSE_ADDITIONAL_CARDS);
            return rng.nextInt(ABANDON_TUNNEL_ODDS) == 0
                    ? SortedBag.of()
                    : options.get(rng.nextInt(options.size()));
        }
    }

    // Simplified Swiss map (only single routes)
    private static final class ChMap {
        private ChMap() {
        }

        // Stations - cities
        private static final Station BAD = new Station(0, "Baden");
        private static final Station BAL = new Station(1, "Bâle");
        private static final Station BEL = new Station(2, "Bellinzone");
        private static final Station BER = new Station(3, "Berne");
        private static final Station BRI = new Station(4, "Brigue");
        private static final Station BRU = new Station(5, "Brusio");
        private static final Station COI = new Station(6, "Coire");
        private static final Station DAV = new Station(7, "Davos");
        private static final Station DEL = new Station(8, "Delémont");
        private static final Station FRI = new Station(9, "Fribourg");
        private static final Station GEN = new Station(10, "Genève");
        private static final Station INT = new Station(11, "Interlaken");
        private static final Station KRE = new Station(12, "Kreuzlingen");
        private static final Station LAU = new Station(13, "Lausanne");
        private static final Station LCF = new Station(14, "La Chaux-de-Fonds");
        private static final Station LOC = new Station(15, "Locarno");
        private static final Station LUC = new Station(16, "Lucerne");
        private static final Station LUG = new Station(17, "Lugano");
        private static final Station MAR = new Station(18, "Martigny");
        private static final Station NEU = new Station(19, "Neuchâtel");
        private static final Station OLT = new Station(20, "Olten");
        private static final Station PFA = new Station(21, "Pfäffikon");
        private static final Station SAR = new Station(22, "Sargans");
        private static final Station SCE = new Station(23, "Schaffhouse");
        private static final Station SCZ = new Station(24, "Schwyz");
        private static final Station SIO = new Station(25, "Sion");
        private static final Station SOL = new Station(26, "Soleure");
        private static final Station STG = new Station(27, "Saint-Gall");
        private static final Station VAD = new Station(28, "Vaduz");
        private static final Station WAS = new Station(29, "Wassen");
        private static final Station WIN = new Station(30, "Winterthour");
        private static final Station YVE = new Station(31, "Yverdon");
        private static final Station ZOU = new Station(32, "Zoug");
        private static final Station ZUR = new Station(33, "Zürich");

        // Stations - countries
        private static final Station DE1 = new Station(34, "Allemagne");
        private static final Station DE2 = new Station(35, "Allemagne");
        private static final Station DE3 = new Station(36, "Allemagne");
        private static final Station DE4 = new Station(37, "Allemagne");
        private static final Station DE5 = new Station(38, "Allemagne");
        private static final Station AT1 = new Station(39, "Autriche");
        private static final Station AT2 = new Station(40, "Autriche");
        private static final Station AT3 = new Station(41, "Autriche");
        private static final Station IT1 = new Station(42, "Italie");
        private static final Station IT2 = new Station(43, "Italie");
        private static final Station IT3 = new Station(44, "Italie");
        private static final Station IT4 = new Station(45, "Italie");
        private static final Station IT5 = new Station(46, "Italie");
        private static final Station FR1 = new Station(47, "France");
        private static final Station FR2 = new Station(48, "France");
        private static final Station FR3 = new Station(49, "France");
        private static final Station FR4 = new Station(50, "France");

        // Countries
        private static final List<Station> DE = List.of(DE1, DE2, DE3, DE4, DE5);
        private static final List<Station> AT = List.of(AT1, AT2, AT3);
        private static final List<Station> IT = List.of(IT1, IT2, IT3, IT4, IT5);
        private static final List<Station> FR = List.of(FR1, FR2, FR3, FR4);

        // Routes (without double routes!)
        private static final List<Route> ALL_ROUTES = List.of(
                new Route("AT1_STG_1", AT1, STG, 4, Route.Level.UNDERGROUND, null),
                new Route("AT2_VAD_1", AT2, VAD, 1, Route.Level.UNDERGROUND, Color.RED),
                new Route("BAD_BAL_1", BAD, BAL, 3, Route.Level.UNDERGROUND, Color.RED),
                new Route("BAD_OLT_1", BAD, OLT, 2, Route.Level.OVERGROUND, Color.VIOLET),
                new Route("BAD_ZUR_1", BAD, ZUR, 1, Route.Level.OVERGROUND, Color.YELLOW),
                new Route("BAL_DE1_1", BAL, DE1, 1, Route.Level.UNDERGROUND, Color.BLUE),
                new Route("BAL_DEL_1", BAL, DEL, 2, Route.Level.UNDERGROUND, Color.YELLOW),
                new Route("BAL_OLT_1", BAL, OLT, 2, Route.Level.UNDERGROUND, Color.ORANGE),
                new Route("BEL_LOC_1", BEL, LOC, 1, Route.Level.UNDERGROUND, Color.BLACK),
                new Route("BEL_LUG_1", BEL, LUG, 1, Route.Level.UNDERGROUND, Color.RED),
                new Route("BEL_WAS_1", BEL, WAS, 4, Route.Level.UNDERGROUND, null),
                new Route("BER_FRI_1", BER, FRI, 1, Route.Level.OVERGROUND, Color.ORANGE),
                new Route("BER_INT_1", BER, INT, 3, Route.Level.OVERGROUND, Color.BLUE),
                new Route("BER_LUC_1", BER, LUC, 4, Route.Level.OVERGROUND, null),
                new Route("BER_NEU_1", BER, NEU, 2, Route.Level.OVERGROUND, Color.RED),
                new Route("BER_SOL_1", BER, SOL, 2, Route.Level.OVERGROUND, Color.BLACK),
                new Route("BRI_INT_1", BRI, INT, 2, Route.Level.UNDERGROUND, Color.WHITE),
                new Route("BRI_IT5_1", BRI, IT5, 3, Route.Level.UNDERGROUND, Color.GREEN),
                new Route("BRI_LOC_1", BRI, LOC, 6, Route.Level.UNDERGROUND, null),
                new Route("BRI_SIO_1", BRI, SIO, 3, Route.Level.UNDERGROUND, Color.BLACK),
                new Route("BRI_WAS_1", BRI, WAS, 4, Route.Level.UNDERGROUND, Color.RED),
                new Route("BRU_COI_1", BRU, COI, 5, Route.Level.UNDERGROUND, null),
                new Route("BRU_DAV_1", BRU, DAV, 4, Route.Level.UNDERGROUND, Color.BLUE),
                new Route("BRU_IT2_1", BRU, IT2, 2, Route.Level.UNDERGROUND, Color.GREEN),
                new Route("COI_DAV_1", COI, DAV, 2, Route.Level.UNDERGROUND, Color.VIOLET),
                new Route("COI_SAR_1", COI, SAR, 1, Route.Level.UNDERGROUND, Color.WHITE),
                new Route("COI_WAS_1", COI, WAS, 5, Route.Level.UNDERGROUND, null),
                new Route("DAV_AT3_1", DAV, AT3, 3, Route.Level.UNDERGROUND, null),
                new Route("DAV_IT1_1", DAV, IT1, 3, Route.Level.UNDERGROUND, null),
                new Route("DAV_SAR_1", DAV, SAR, 3, Route.Level.UNDERGROUND, Color.BLACK),
                new Route("DE2_SCE_1", DE2, SCE, 1, Route.Level.OVERGROUND, Color.YELLOW),
                new Route("DE3_KRE_1", DE3, KRE, 1, Route.Level.OVERGROUND, Color.ORANGE),
                new Route("DE4_KRE_1", DE4, KRE, 1, Route.Level.OVERGROUND, Color.WHITE),
                new Route("DE5_STG_1", DE5, STG, 2, Route.Level.OVERGROUND, null),
                new Route("DEL_FR4_1", DEL, FR4, 2, Route.Level.UNDERGROUND, Color.BLACK),
                new Route("DEL_LCF_1", DEL, LCF, 3, Route.Level.UNDERGROUND, Color.WHITE),
                new Route("DEL_SOL_1", DEL, SOL, 1, Route.Level.UNDERGROUND, Color.VIOLET),
                new Route("FR1_MAR_1", FR1, MAR, 2, Route.Level.UNDERGROUND, null),
                new Route("FR2_GEN_1", FR2, GEN, 1, Route.Level.OVERGROUND, Color.YELLOW),
                new Route("FR3_LCF_1", FR3, LCF, 2, Route.Level.UNDERGROUND, Color.GREEN),
                new Route("FRI_LAU_1", FRI, LAU, 3, Route.Level.OVERGROUND, Color.RED),
                new Route("GEN_LAU_1", GEN, LAU, 4, Route.Level.OVERGROUND, Color.BLUE),
                new Route("GEN_YVE_1", GEN, YVE, 6, Route.Level.OVERGROUND, null),
                new Route("INT_LUC_1", INT, LUC, 4, Route.Level.OVERGROUND, Color.VIOLET),
                new Route("IT3_LUG_1", IT3, LUG, 2, Route.Level.UNDERGROUND, Color.WHITE),
                new Route("IT4_LOC_1", IT4, LOC, 2, Route.Level.UNDERGROUND, Color.ORANGE),
                new Route("KRE_SCE_1", KRE, SCE, 3, Route.Level.OVERGROUND, Color.VIOLET),
                new Route("KRE_STG_1", KRE, STG, 1, Route.Level.OVERGROUND, Color.GREEN),
                new Route("KRE_WIN_1", KRE, WIN, 2, Route.Level.OVERGROUND, Color.YELLOW),
                new Route("LAU_MAR_1", LAU, MAR, 4, Route.Level.UNDERGROUND, Color.ORANGE),
                new Route("LAU_NEU_1", LAU, NEU, 4, Route.Level.OVERGROUND, null),
                new Route("LCF_NEU_1", LCF, NEU, 1, Route.Level.UNDERGROUND, Color.ORANGE),
                new Route("LCF_YVE_1", LCF, YVE, 3, Route.Level.UNDERGROUND, Color.YELLOW),
                new Route("LOC_LUG_1", LOC, LUG, 1, Route.Level.UNDERGROUND, Color.VIOLET),
                new Route("LUC_OLT_1", LUC, OLT, 3, Route.Level.OVERGROUND, Color.GREEN),
                new Route("LUC_SCZ_1", LUC, SCZ, 1, Route.Level.OVERGROUND, Color.BLUE),
                new Route("LUC_ZOU_1", LUC, ZOU, 1, Route.Level.OVERGROUND, Color.ORANGE),
                new Route("MAR_SIO_1", MAR, SIO, 2, Route.Level.UNDERGROUND, Color.GREEN),
                new Route("NEU_SOL_1", NEU, SOL, 4, Route.Level.OVERGROUND, Color.GREEN),
                new Route("NEU_YVE_1", NEU, YVE, 2, Route.Level.OVERGROUND, Color.BLACK),
                new Route("OLT_SOL_1", OLT, SOL, 1, Route.Level.OVERGROUND, Color.BLUE),
                new Route("OLT_ZUR_1", OLT, ZUR, 3, Route.Level.OVERGROUND, Color.WHITE),
                new Route("PFA_SAR_1", PFA, SAR, 3, Route.Level.UNDERGROUND, Color.YELLOW),
                new Route("PFA_SCZ_1", PFA, SCZ, 1, Route.Level.OVERGROUND, Color.VIOLET),
                new Route("PFA_STG_1", PFA, STG, 3, Route.Level.OVERGROUND, Color.ORANGE),
                new Route("PFA_ZUR_1", PFA, ZUR, 2, Route.Level.OVERGROUND, Color.BLUE),
                new Route("SAR_VAD_1", SAR, VAD, 1, Route.Level.UNDERGROUND, Color.ORANGE),
                new Route("SCE_WIN_1", SCE, WIN, 1, Route.Level.OVERGROUND, Color.BLACK),
                new Route("SCE_ZUR_1", SCE, ZUR, 3, Route.Level.OVERGROUND, Color.ORANGE),
                new Route("SCZ_WAS_1", SCZ, WAS, 2, Route.Level.UNDERGROUND, Color.GREEN),
                new Route("SCZ_ZOU_1", SCZ, ZOU, 1, Route.Level.OVERGROUND, Color.BLACK),
                new Route("STG_VAD_1", STG, VAD, 2, Route.Level.UNDERGROUND, Color.BLUE),
                new Route("STG_WIN_1", STG, WIN, 3, Route.Level.OVERGROUND, Color.RED),
                new Route("STG_ZUR_1", STG, ZUR, 4, Route.Level.OVERGROUND, Color.BLACK),
                new Route("WIN_ZUR_1", WIN, ZUR, 1, Route.Level.OVERGROUND, Color.BLUE),
                new Route("ZOU_ZUR_1", ZOU, ZUR, 1, Route.Level.OVERGROUND, Color.GREEN));

        // Tickets
        private static final Ticket deToNeighbors = ticketToNeighbors(DE, 0, 5, 13, 5);
        private static final Ticket atToNeighbors = ticketToNeighbors(AT, 5, 0, 6, 14);
        private static final Ticket itToNeighbors = ticketToNeighbors(IT, 13, 6, 0, 11);
        private static final Ticket frToNeighbors = ticketToNeighbors(FR, 5, 14, 11, 0);

        private static final List<Ticket> ALL_TICKETS = List.of(
                // City-to-city tickets
                new Ticket(BAL, BER, 5),
                new Ticket(BAL, BRI, 10),
                new Ticket(BAL, STG, 8),
                new Ticket(BER, COI, 10),
                new Ticket(BER, LUG, 12),
                new Ticket(BER, SCZ, 5),
                new Ticket(BER, ZUR, 6),
                new Ticket(FRI, LUC, 5),
                new Ticket(GEN, BAL, 13),
                new Ticket(GEN, BER, 8),
                new Ticket(GEN, SIO, 10),
                new Ticket(GEN, ZUR, 14),
                new Ticket(INT, WIN, 7),
                new Ticket(KRE, ZUR, 3),
                new Ticket(LAU, INT, 7),
                new Ticket(LAU, LUC, 8),
                new Ticket(LAU, STG, 13),
                new Ticket(LCF, BER, 3),
                new Ticket(LCF, LUC, 7),
                new Ticket(LCF, ZUR, 8),
                new Ticket(LUC, VAD, 6),
                new Ticket(LUC, ZUR, 2),
                new Ticket(LUG, COI, 10),
                new Ticket(NEU, WIN, 9),
                new Ticket(OLT, SCE, 5),
                new Ticket(SCE, MAR, 15),
                new Ticket(SCE, STG, 4),
                new Ticket(SCE, ZOU, 3),
                new Ticket(STG, BRU, 9),
                new Ticket(WIN, SCZ, 3),
                new Ticket(ZUR, BAL, 4),
                new Ticket(ZUR, BRU, 11),
                new Ticket(ZUR, LUG, 9),
                new Ticket(ZUR, VAD, 6),

                // City to country tickets
                ticketToNeighbors(List.of(BER), 6, 11, 8, 5),
                ticketToNeighbors(List.of(COI), 6, 3, 5, 12),
                ticketToNeighbors(List.of(LUG), 12, 13, 2, 14),
                ticketToNeighbors(List.of(ZUR), 3, 7, 11, 7),

                // Country to country tickets (two of each)
                deToNeighbors, deToNeighbors,
                atToNeighbors, atToNeighbors,
                itToNeighbors, itToNeighbors,
                frToNeighbors, frToNeighbors);

        private static Ticket ticketToNeighbors(List<Station> from, int de, int at, int it, int fr) {
            var trips = new ArrayList<Trip>();
            if (de != 0) trips.addAll(Trip.all(from, DE, de));
            if (at != 0) trips.addAll(Trip.all(from, AT, at));
            if (it != 0) trips.addAll(Trip.all(from, IT, it));
            if (fr != 0) trips.addAll(Trip.all(from, FR, fr));
            return new Ticket(trips);
        }
    }
}
