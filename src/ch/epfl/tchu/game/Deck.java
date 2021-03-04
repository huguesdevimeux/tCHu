package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Represents a deck of cards. Immutable.
 *
 * @author Hugues Devimeux (327282)
 */
public final class Deck<C extends Comparable<C>> {

    private final List<C> cards;

    private Deck(List<C> cards) {
        this.cards = cards;
    }

    /**
     * Returns a {@link Deck} with the same cards in <code>cards</code> parameter, but shuffled.
     *
     * @param cards cards to insert in the Deck.
     * @param rng Random Generator.
     * @param <C> type of the cards. Generic.
     * @return the Deck created.
     */
    public static <C extends Comparable<C>> Deck<C> of(SortedBag<C> cards, Random rng) {
        List<C> shuffledCards = cards.toList();
        Collections.shuffle(shuffledCards, rng);
        System.out.println(shuffledCards);
        return new Deck<>(shuffledCards);
    }

    /**
     * Returns the size of the Deck (i.e number of cards in it).
     *
     * @return the size of the Deck.
     */
    public int size() {
        return cards.size();
    }

    /**
     * Returns whether the Deck has no card.
     *
     * @return whether the Deck is empty.
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * Returns the top card of the deck.
     *
     * @throws IllegalArgumentException if the Deck is empty.
     * @return The top card of the Deck.
     */
    public C topCard() {
        if (isEmpty()) throw new IllegalArgumentException("Deck is empty!");
        return cards.get(size() - 1);
    }
    /**
     * Returns the n top cards, where n is the count parameter.
     *
     * @param count number of top cards to get.
     * @return the count top cards.
     * @throws IllegalArgumentException if count is not within 0 and the size of the Deck.
     */
    public SortedBag<C> topCards(int count) {
        Preconditions.checkArgument(0 <= count && count <= size());
        return SortedBag.of(this.cards.subList(cards.size() - count, cards.size()));
    }
    /**
     * Returns an identical Deck but without the top card.
     * @throws IllegalArgumentException if the Deck is empty.
     * @return The same Deck but without the top card. j
     */
    public Deck<C> withoutTopCard() {
        return withoutTopCards(1);
    }

    /**
     * Returns an indentical Deck but without the n top cards, where n is count parameter.
     * @param count Number of cards to substract from the Deck.
     * @return the Deck substracted from count cards.
     * @ŧhrows IllegalArgumentException count is not within 0 and the size of the Deck.
     */
    public Deck<C> withoutTopCards(int count) {
        Preconditions.checkArgument(0 <= count && count <= size());
        return new Deck<>(cards.subList(0, cards.size() - count));
    }
}
