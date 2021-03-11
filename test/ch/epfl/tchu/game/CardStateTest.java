package ch.epfl.tchu.game;

import ch.epfl.tchu.SortedBag;
import ch.epfl.test.TestRandomizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardStateTest {
    private Deck<Card> testedDeck;
    private CardState testedCardState;
    private int originalDeckSize;
    private CardState cardStateWithEmptyDeck;

    @BeforeEach
    void setUp() {
        testedDeck = Deck.of(SortedBag.of(Card.ALL), TestRandomizer.newRandom());
        testedCardState = CardState.of(testedDeck);
        cardStateWithEmptyDeck =
                CardState.of(Deck.of(SortedBag.of(5, Card.BLUE), TestRandomizer.newRandom()));
        originalDeckSize = testedCardState.deckSize();
    }

    @Test
    void ofFailWhenTooSmallDeck() { // et non pas dick xD
        Deck<Card> tooSmallDeck = Deck.of(SortedBag.of(Card.BLUE), TestRandomizer.newRandom());
        assertThrows(IllegalArgumentException.class, () -> CardState.of(tooSmallDeck));
    }

    @Test
    void withDrawnFaceUpCard() {
        CardState c = testedCardState.withDrawnFaceUpCard(4);
        assertEquals(Card.RED, c.faceUpCard(4));
        assertEquals(Card.YELLOW, c.topDeckCard());
        assertEquals(testedCardState.deckSize() - 1, c.deckSize());
    }

    @Test
    void withDrawnFaceUpCardFailsWhenWrongIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> testedCardState.withDrawnFaceUpCard(5));
        assertThrows(
                IndexOutOfBoundsException.class, () -> testedCardState.withDrawnFaceUpCard(-1));
        assertDoesNotThrow(() -> testedCardState.withDrawnFaceUpCard(0));
    }

    @Test
    void withDrawnFaceUpCardFailsWhenNoDeck() {
        assertThrows(
                IllegalArgumentException.class,
                () -> cardStateWithEmptyDeck.withDrawnFaceUpCard(1));
    }

    @Test
    void topDeckCard() {
        assertEquals(Card.RED, testedCardState.topDeckCard());
        // c's deck is empty.
        assertThrows(IllegalArgumentException.class, cardStateWithEmptyDeck::topDeckCard);
        assertEquals(Card.RED, testedCardState.topDeckCard());
    }

    @Test
    void topDeckCardFailsWhenThereIsNoDeck() {
        assertThrows(IllegalArgumentException.class, cardStateWithEmptyDeck::topDeckCard);
    }

    @Test
    void withoutTopDeckCard() {
        assertThrows(IllegalArgumentException.class, cardStateWithEmptyDeck::withoutTopDeckCard);
        assertEquals(Card.YELLOW, testedCardState.withoutTopDeckCard().topDeckCard());
        assertEquals(originalDeckSize - 1, testedCardState.withoutTopDeckCard().deckSize());
    }

    @Test
    void withoutTopDeckCardKeepsSameSetOfCards() {
        CardState c = testedCardState.withoutTopDeckCard();
        assertEquals(testedCardState.discardsSize(), c.discardsSize());
        assertEquals(testedCardState.faceUpCards(), c.faceUpCards());
    }

    @Test
    void withMoreDiscardedCards() {
        CardState addedDiscard = testedCardState.withMoreDiscardedCards(SortedBag.of(Card.BLUE));
        assertEquals(1, addedDiscard.discardsSize());
        assertEquals(testedCardState.deckSize(), addedDiscard.deckSize());
        assertEquals(testedCardState.faceUpCards(), addedDiscard.faceUpCards());
    }

    @Test
    void withDeckRecreatedFromDiscards() {

        CardState newState = cardStateWithEmptyDeck.withMoreDiscardedCards(SortedBag.of(Card.BLUE));
        newState = newState.withDeckRecreatedFromDiscards(TestRandomizer.newRandom());
        assertEquals(0, newState.discardsSize());
        assertEquals(1, newState.deckSize());
        assertEquals(cardStateWithEmptyDeck.faceUpCards(), newState.faceUpCards());
    }

    @Test
    void withDeckRecreatedFromDiscardsFailsWhenDeckIsNotEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> testedCardState.withDeckRecreatedFromDiscards(TestRandomizer.newRandom()));
    }
}
