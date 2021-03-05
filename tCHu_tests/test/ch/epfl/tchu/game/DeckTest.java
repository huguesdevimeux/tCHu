package ch.epfl.tchu.game;

import ch.epfl.tchu.SortedBag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class DeckTest {

    private SortedBag<Card> cardList;
    private SortedBag.Builder<Card> cardBuilder;
    private Deck deck;

    @BeforeEach
    void setUp() {
        cardList = SortedBag.of(Card.ALL.subList(0, 3));
        deck = Deck.of(cardList, new Random());
    }

    @Test
    void assertTwoCardsGetShuffledCorrectly() {
        deck = Deck.of(SortedBag.of(List.of(Card.BLACK, Card.VIOLET)), new Random(1));
        // cards get shuffled once so violet must be top card
        assertEquals(Card.VIOLET, deck.topCard());
    }

    @Test
    void returnsCorrectSize() {
        assertEquals(cardList.size(), deck.size());
    }

    @Test
    void assertReturnsTrueIfListOfCardsIsEmpty() {
        assertEquals(0, Deck.of(SortedBag.<Card>of(), new Random()).size());
    }

    @Test
    void returnsCorrectTopCardAndThrowsExceptionIfEmpty() {
        assertTrue(
                deck.topCard().equals(Card.BLACK)
                        || deck.topCard().equals(Card.VIOLET)
                        || deck.topCard().equals(Card.BLUE));
        assertThrows(
                IllegalArgumentException.class,
                () -> Deck.of(SortedBag.<Card>of(), new Random(1)).topCard());
    }

    @Test
    void returnsCorrectAmountOfTopCardsAndThrowsExceptionIfOutOfBounds() {
        deck = Deck.of(SortedBag.of(Card.ALL.subList(0, 6)), new Random());
        assertEquals(4, deck.topCards(4).size());

        assertThrows(IllegalArgumentException.class, () -> deck.topCards(-1));
        assertThrows(IllegalArgumentException.class, () -> deck.topCards(7));
    }

    @Test
    void returnsDeckWithoutTopCard() {
        Deck deck = Deck.of(SortedBag.of(Card.ALL.subList(0, 2)), new Random(1));
        Deck deckWithoutTopCard = deck.withoutTopCard();
        assertEquals(deckWithoutTopCard.topCard(), Card.BLACK);
        assertThrows(
                IllegalArgumentException.class, () -> Deck.of(SortedBag.<Card>of(), new Random()));
    }

    @Test
    void returnsDeckWithoutNCards(){
        assertThrows(IllegalArgumentException.class, () -> deck.withoutTopCards(-1));
        assertThrows(IllegalArgumentException.class, () -> deck.withoutTopCards(4));
        Deck topCards = deck.withoutTopCards(2);
        assertEquals(1, topCards.size());
        topCards = deck.withoutTopCards(3);
        assertTrue(topCards.isEmpty());
    }
}
