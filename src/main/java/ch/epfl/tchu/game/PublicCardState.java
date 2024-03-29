package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.List;
import java.util.Objects;

/**
 * Represents the state of cars/locomotives which are NOT in either of the players' hands i.e the 5
 * face up cards, the deck cards and the discarded cards
 *
 * @author Luca Mouchel (324748)
 * @author Hugues Devimeux (327282)
 */
public class PublicCardState {
    private final List<Card> faceUpCards;
    private final int deckSize;
    private final int discardsSize;

    /**
     * Public constructor. Builds public cards, seen by both players
     *
     * @param faceUpCards 5 cards visible by both players
     * @param deckSize number of cards composing the deck
     * @param discardsSize number of discarded cards
     */
    public PublicCardState(List<Card> faceUpCards, int deckSize, int discardsSize) {
        Preconditions.checkArgument(faceUpCards.size() == 5);
        Preconditions.checkArgument(Math.min(deckSize, discardsSize) >= 0);
        this.faceUpCards = List.copyOf(faceUpCards);
        this.deckSize = deckSize;
        this.discardsSize = discardsSize;
    }

    /**
     * Returns the face up cards that contains exactly 5 elements.
     *
     * @return face up cards
     */
    public List<Card> faceUpCards() {
        return faceUpCards;
    }

    /**
     * Returns a card of faceUpCards at a given index.
     *
     * @param slot index to check
     * @return the card at index <code>slot</code>
     * @throws IndexOutOfBoundsException if slot is not within 0 and 5 (included)
     */
    public Card faceUpCard(int slot) {
        return faceUpCards.get(Objects.checkIndex(slot, 5));
    }

    /**
     * Returns deck size.
     *
     * @return deckSize
     */
    public int deckSize() {
        return deckSize;
    }

    /**
     * Returns true if deck is empty.
     *
     * @return whether deck is empty
     */
    public boolean isDeckEmpty() {
        return deckSize == 0;
    }

    /**
     * Returns discards size.
     *
     * @return discardsSize
     */
    public int discardsSize() {
        return discardsSize;
    }
}
