package ch.epfl.tchu.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PublicCardStateTest {

    private PublicCardState cards;
    private PublicCardState emptyCards;

    @BeforeEach
    public void setUp() {
        cards = new PublicCardState(Card.ALL.subList(0, 5), 10, 10);
        emptyCards = new PublicCardState(Card.ALL.subList(0, 5), 0, 0);
    }

    @Test
    public void RaisesIllegalArgumentWhenWrongNumberOfCards() {
        List<Card> fourCards = Card.ALL.subList(0, 4);
        assertThrows(IllegalArgumentException.class, () -> new PublicCardState(fourCards, 4, 4));
    }

    @Test
    public void ThrowsIllegalArgumentExceptionWhenNegativeSize() {
        assertThrows(IllegalArgumentException.class, () -> new PublicCardState(Card.ALL, -1, 4));
        assertThrows(IllegalArgumentException.class, () -> new PublicCardState(Card.ALL, 4, -1));
    }

//    @Test
//    public void totalSize() {
//        assertEquals(5 + 10 + 10, cards.totalSize());
//    }

    @Test
    void faceUpCards() {
        assertEquals(Card.ALL.subList(0, 5), cards.faceUpCards());
    }

    @Test
    void faceUpCard() {
        assertEquals(Card.ALL.subList(0, 5).get(0), cards.faceUpCard(0));
        assertEquals(Card.ALL.subList(0, 5).get(4), cards.faceUpCard(4));

        assertThrows(IndexOutOfBoundsException.class, () -> cards.faceUpCard(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> cards.faceUpCard(6));
    }

    @Test
    void deckSize() {
        assertEquals(10, cards.deckSize());
    }

    @Test
    void isDeckEmpty() {
        assertFalse(cards.isDeckEmpty());
        assertTrue(emptyCards.isDeckEmpty());
    }

    @Test
    void discardsSize() {
        assertEquals(10, cards.discardsSize());
    }
}
