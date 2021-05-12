package ch.epfl.tchu.net;

import static ch.epfl.tchu.game.Card.*;
import static ch.epfl.tchu.game.PlayerId.PLAYER_1;
import static ch.epfl.tchu.game.PlayerId.PLAYER_2;
import static ch.epfl.tchu.net.Serdes.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static java.nio.charset.StandardCharsets.UTF_8;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;
import ch.epfl.tchu.net.Serde;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

interface EqualityAsserter<T> extends BiConsumer<T, T> {}

class SerdesTest {

    private final Base64.Encoder encoder = Base64.getEncoder();
    private final Random random = ch.epfl.test.TestRandomizer.newRandom();
    private final Base64.Decoder decoder = Base64.getDecoder();
    private List<Card> allCards;
    private ArrayList<Route> allRoutes;
    private ArrayList<Ticket> allTickets;

    @BeforeEach
    void setUp() {
        allCards = new ArrayList<>(Card.ALL);
        allRoutes = new ArrayList<>(ChMap.routes());
        allTickets = new ArrayList<>(ChMap.tickets());
    }

    @Test
    void testWithInteger() {
        testSerdeWithValues(intSerde, 1, "1");
        testSerdeWithValues(intSerde, -1, "-1");
        testSerdeWithValues(intSerde, 0, "0");
    }

    @Test
    void testWithString() {
        for (String toTest : List.of("Hello World", "", " ", "\n", "\\uD83D\\uDC4D", "éà€")) {
            testSerdeWithValues(
                    stringSerde, toTest, encoder.encodeToString(toTest.getBytes(UTF_8)));
        }
    }

    @Test
    void testWithPlayerId() {
        testSerdeWithValues(playerIdSerde, PLAYER_1, "0");
        testSerdeWithValues(playerIdSerde, PLAYER_2, "1");
    }

    @Test
    void testWithTurnKind() {
        for (int i = 0; i < Player.TurnKind.ALL.size(); i++) {
            testSerdeWithValues(turnKindSerde, Player.TurnKind.ALL.get(i), String.valueOf(i));
        }
    }

    @Test
    void testWithCard() {
        for (int i = 0; i < Card.ALL.size(); i++) {
            testSerdeWithValues(cardSerde, Card.ALL.get(i), String.valueOf(i));
        }
    }

    @Test
    void testWithRoute() {
        for (int i = 0; i < ChMap.routes().size(); i++) {
            testSerdeWithValues(routeSerde, ChMap.routes().get(i), String.valueOf(i));
        }
    }

    @Test
    void testWithTicket() {
        ChMap.tickets()
                .forEach(
                        i -> {
                            assertEquals(
                                    String.valueOf(ChMap.tickets().indexOf(i)),
                                    ticketSerde.serialize(i));
                            assertEquals(
                                    i,
                                    ticketSerde.deserialize(
                                            String.valueOf(ChMap.tickets().indexOf(i))));
                            assertEquals(i, ticketSerde.deserialize(ticketSerde.serialize(i)));
                        });
    }

    @Test
    void testWithListOfStrings() {
        String SEPARATOR = ",";
        for (List<String> testedValue : List.of(List.of("Hello", "World"))) {
            String target =
                    testedValue.stream()
                            .map(s -> encoder.encodeToString(s.getBytes(UTF_8)))
                            .collect(Collectors.joining(","));
            testSerdeWithValues(stringListSerde, testedValue, target);
        }
    }

    @Test
    void testWithCardsList() {
        String SEPARATOR = ",";
        int MAX_NUMBER_TESTS = Card.COUNT;
        for (int i = 1; i < MAX_NUMBER_TESTS; i++) {
            var testedValues = new ArrayList<Card>();
            for (int j = 0; j < i; j++) {
                testedValues.add(Card.ALL.get(random.nextInt(Card.COUNT)));
            }
            var target =
                    testedValues.stream()
                            .map(cardSerde::serialize)
                            .collect(Collectors.joining(SEPARATOR));
            testSerdeWithValues(cardListSerde, testedValues, target);
        }
    }

    @Test
    void testWithListOfRoute() {
        String SEPARATOR = ",";
        int MAX_NUMBER_TESTS = ChMap.routes().size();
        for (int i = 1; i < MAX_NUMBER_TESTS; i++) {
            var testedValues = new ArrayList<Route>();
            for (int j = 0; j < i; j++) {
                testedValues.add(ChMap.routes().get(random.nextInt(ChMap.routes().size())));
            }
            var target =
                    testedValues.stream()
                            .map(routeSerde::serialize)
                            .collect(Collectors.joining(SEPARATOR));
            testSerdeWithValues(routeListSerde, testedValues, target);
        }
    }

    @Test
    void testWithSortedBagOfCard() {
        String SEPARATOR = ",";
        int MAX_NUMBER_TESTS = Card.ALL.size();
        for (int i = 1; i < MAX_NUMBER_TESTS; i++) {
            var testedValues = new SortedBag.Builder<Card>();
            for (int j = 0; j < i; j++) {
                testedValues.add(Card.ALL.get(random.nextInt(Card.COUNT)));
            }
            var target =
                    testedValues.build().stream()
                            .map(cardSerde::serialize)
                            .collect(Collectors.joining(SEPARATOR));
            testSerdeWithValues(cardBagSerde, testedValues.build(), target);
        }
    }

    @Test
    void testWithSortedBagOfTicket() {
        String SEPARATOR = ",";
        int MAX_NUMBER_TESTS = ChMap.tickets().size();
        for (int i = 1; i < MAX_NUMBER_TESTS; i++) {
            var testedValues = new SortedBag.Builder<Ticket>();
            for (int j = 0; j < i; j++) {
                testedValues.add(ChMap.tickets().get(random.nextInt(ChMap.tickets().size())));
            }
            var target =
                    testedValues.build().stream()
                            .map(ticketSerde::serialize)
                            .collect(Collectors.joining(SEPARATOR));
            testSerdeWithValues(ticketBagSerde, testedValues.build(), target);
        }
    }

    @Test
    void testWithSortedBagOfSortedBagTicket() {
        String SEPARATOR = ";";
        int AMOUNT_INNER_BAGS = ChMap.tickets().size();
        var testedValues = new ArrayList<SortedBag<Card>>();
        for (int i = 1; i < AMOUNT_INNER_BAGS; i++) {
            var innerSortedBag = new SortedBag.Builder<Card>();
            for (int j = 0; j < i; j++) {
                innerSortedBag.add(Card.ALL.get(random.nextInt(Card.ALL.size())));
            }
            testedValues.add(innerSortedBag.build());
        }
        var target =
                testedValues.stream()
                        .map(cardBagSerde::serialize)
                        .collect(Collectors.joining(SEPARATOR));
        testSerdeWithValues(listOfCardBagSerde, testedValues, target);
    }

    @Test
    void testWithPublicCardState() {
        int TESTS_ITERATIONS = 50;
        String SEPARATOR = ";";
        EqualityAsserter<PublicCardState> publicCardStateEqualityAsserter =
                (publicCardState, publicCardState2) -> {
                    assertEquals(publicCardState.deckSize(), publicCardState2.deckSize());
                    assertEquals(publicCardState.discardsSize(), publicCardState2.discardsSize());
                    assertEquals(publicCardState.faceUpCards(), publicCardState2.faceUpCards());
                    assertEquals(publicCardState.totalSize(), publicCardState2.totalSize());
                };

        PublicCardState p = new PublicCardState(List.of(RED, WHITE, BLUE, BLACK, RED), 0, 0);
        String target =
                String.join(
                        SEPARATOR,
                        List.of(
                                cardListSerde.serialize(p.faceUpCards()),
                                intSerde.serialize(p.deckSize()),
                                intSerde.serialize(p.discardsSize())));
        testSerdeWithValues(publicCardStateSerde, p, target, publicCardStateEqualityAsserter);

        for (int i = 0; i < TESTS_ITERATIONS; i++) {
            Collections.shuffle(allCards, random);

            p =
                    new PublicCardState(
                            allCards.subList(0, 5), random.nextInt(1000), random.nextInt(1000));
            target =
                    String.join(
                            SEPARATOR,
                            List.of(
                                    cardListSerde.serialize(p.faceUpCards()),
                                    intSerde.serialize(p.deckSize()),
                                    intSerde.serialize(p.discardsSize())));
            testSerdeWithValues(publicCardStateSerde, p, target, publicCardStateEqualityAsserter);
        }
    }

    @Test
    void testWithPublicPlayerState() {
        int TESTS_ITERATIONS = 50;
        String SEPARATOR = ";";
        EqualityAsserter<PublicPlayerState> publicPlayerStateEqualityAsserter =
                (p1, p2) -> {
                    assertEquals(p1.ticketCount(), p2.ticketCount());
                    assertEquals(p1.carCount(), p2.carCount());
                    assertEquals(p1.routes(), p2.routes());
                };

        PublicPlayerState p = new PublicPlayerState(0, 0, List.of());
        String target =
                String.join(
                        SEPARATOR,
                        List.of(
                                intSerde.serialize(p.ticketCount()),
                                intSerde.serialize(p.cardCount()),
                                routeListSerde.serialize(p.routes())));
        // This test fails. TODO
        testSerdeWithValues(publicPlayerStateSerde, p, target, publicPlayerStateEqualityAsserter);

        for (int i = 0; i < TESTS_ITERATIONS; i++) {
            Collections.shuffle(allRoutes, random);

            p =
                    new PublicPlayerState(
                            random.nextInt(1000),
                            random.nextInt(1000),
                            allRoutes.subList(0, random.nextInt(ChMap.routes().size())));
            target =
                    String.join(
                            SEPARATOR,
                            List.of(
                                    intSerde.serialize(p.ticketCount()),
                                    intSerde.serialize(p.cardCount()),
                                    routeListSerde.serialize(p.routes())));
            testSerdeWithValues(
                    publicPlayerStateSerde, p, target, publicPlayerStateEqualityAsserter);
        }
    }

    @Test
    void testWithPlayerState() {
        int TESTS_ITERATIONS = 50;
        String SEPARATOR = ";";
        EqualityAsserter<PlayerState> playerStateEqualityAsserter =
                (p1, p2) -> {
                    assertEquals(p1.tickets(), p2.tickets());
                    assertEquals(p1.cards(), p2.cards());
                    assertEquals(p1.routes(), p2.routes());
                };

        PlayerState p = new PlayerState(SortedBag.of(), SortedBag.of(), List.of());
        String target =
                String.join(
                        SEPARATOR,
                        List.of(
                                ticketBagSerde.serialize(p.tickets()),
                                cardBagSerde.serialize(p.cards()),
                                routeListSerde.serialize(p.routes())));
        testSerdeWithValues(playerStateSerde, p, target, playerStateEqualityAsserter);

        for (int i = 1; i < TESTS_ITERATIONS; i++) {
            Collections.shuffle(allRoutes, random);
            Collections.shuffle(allTickets, random);
            Collections.shuffle(allRoutes, random);
            p =
                    new PlayerState(
                            SortedBag.of(allTickets.subList(0, random.nextInt(allTickets.size()))),
                            SortedBag.of(allCards.subList(0, random.nextInt(COUNT))),
                            allRoutes.subList(0, random.nextInt(allRoutes.size())));
            target =
                    String.join(
                            SEPARATOR,
                            List.of(
                                    ticketBagSerde.serialize(p.tickets()),
                                    cardBagSerde.serialize(p.cards()),
                                    routeListSerde.serialize(p.routes())));
            testSerdeWithValues(playerStateSerde, p, target, playerStateEqualityAsserter);
        }
    }

    @Test
    void testWithPublicGameState() {
        EqualityAsserter<PublicPlayerState> publicPlayerStateEqualityAsserter =
                (p1, p2) -> {
                    assertEquals(p1.ticketCount(), p2.ticketCount());
                    assertEquals(p1.carCount(), p1.carCount());
                    assertEquals(p1.routes(), p2.routes());
                };

        EqualityAsserter<PlayerState> playerStateEqualityAsserter =
                (p1, p2) -> {
                    assertEquals(p1.tickets(), p2.tickets());
                    assertEquals(p1.cards(), p1.cards());
                    assertEquals(p1.routes(), p2.routes());
                };

        EqualityAsserter<PublicCardState> publicCardStateEqualityAsserter =
                (publicCardState, publicCardState2) -> {
                    assertEquals(publicCardState.deckSize(), publicCardState2.deckSize());
                    assertEquals(publicCardState.discardsSize(), publicCardState2.discardsSize());
                    assertEquals(publicCardState.faceUpCards(), publicCardState2.faceUpCards());
                    assertEquals(publicCardState.totalSize(), publicCardState2.totalSize());
                };

        EqualityAsserter<PublicGameState> tester =
                (p1, p2) -> {
                    assertEquals(p1.ticketsCount(), p2.ticketsCount());
                    publicCardStateEqualityAsserter.accept(p1.cardState(), p2.cardState());
                    assertEquals(p1.currentPlayerId(), p2.currentPlayerId());
                    publicPlayerStateEqualityAsserter.accept(
                            p1.playerState(PLAYER_1), p1.playerState(PLAYER_1));
                    publicPlayerStateEqualityAsserter.accept(
                            p1.playerState(PLAYER_2), p1.playerState(PLAYER_2));
                    assertEquals(p1.lastPlayer(), p2.lastPlayer());
                };

        List<Card> fu = List.of(RED, WHITE, BLUE, BLACK, RED);
        PublicCardState cs = new PublicCardState(fu, 30, 31);
        List<Route> rs1 = ChMap.routes().subList(0, 2);
        Map<PlayerId, PublicPlayerState> ps =
                Map.of(
                        PLAYER_1, new PublicPlayerState(10, 11, rs1),
                        PLAYER_2, new PublicPlayerState(20, 21, List.of()));
        PublicGameState gs = new PublicGameState(40, cs, PLAYER_2, ps, null);

        // Comes from paper.
        String target = "40:6,7,2,0,6;30;31:1:10;11;0,1:20;21;:";
        testSerdeWithValues(publicGameStateSerde, gs, target, tester);
    }

    private <T> void testSerdeWithValues(Serde<T> serde, T toSerialize, String expected) {
        assertEquals(expected, serde.serialize(toSerialize));
        assertEquals(toSerialize, serde.deserialize(expected));
        assertEquals(toSerialize, serde.deserialize(serde.serialize(toSerialize)));
    }

    private <T> void testSerdeWithValues(
            Serde<T> serde, T toSerialize, String expected, EqualityAsserter<T> equalityAsserter) {
        assertEquals(expected, serde.serialize(toSerialize));
        equalityAsserter.accept(toSerialize, serde.deserialize(expected));
        equalityAsserter.accept(toSerialize, serde.deserialize(serde.serialize(toSerialize)));
    }
}
