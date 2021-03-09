package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * CardState represents the state of cards, be it face up cards, deck cards or discarded cards.
 *
 * @author Luca Mouchel (324748)
 * @author Hugues Devimeux (327282)
 */
public final class CardState extends PublicCardState {
    private final Deck<Card> deck;
    private final SortedBag<Card> discardCards;
    /**
     * Private constructor. Builds public cards, seen by both players.
     *
     * @param faceUpCards 5 cards visible by both players
     * @param deckSize number of cards composing the deck
     * @param discardsSize number of discarded cards
     * @param deck
     * @param discardCards
     */
    private CardState(
            List<Card> faceUpCards,
            int deckSize,
            int discardsSize,
            Deck<Card> deck,
            SortedBag<Card> discardCards) {
        super(faceUpCards, deckSize, discardsSize);
        // added two additional parameters to the constructor
        this.deck = deck;
        this.discardCards = discardCards;
    }

    /**
     * Returns state of cards where: the first 5 cards form the face up cards and the rest forms the
     * deck and the discards are empty.
     *
     * @param deck deck of cards
     * @throws IllegalArgumentException if deck size is smaller than 5
     * @return a new Card state
     */
    public static CardState of(Deck<Card> deck) {
        Preconditions.checkArgument(deck.size() >= 5);
        // faceUpCards represents the first 5 cards of the deck
        List<Card> faceUpCards = deck.topCards(Constants.FACE_UP_CARDS_COUNT).toList();
        // discarded cards is 0
        return new CardState(
                faceUpCards,
                deck.size() - Constants.FACE_UP_CARDS_COUNT,
                0,
                deck.withoutTopCards(Constants.FACE_UP_CARDS_COUNT),
                SortedBag.of());
    }

    /**
     * Returns a new card state in which the face up card at index <code>slot</code> is replaced by
     * the decks' top card.
     *
     * @param slot index at which we switch the face up card with the decks' top card
     * @throws IndexOutOfBoundsException if <code>slot</code> is not between 0 and 5 (excluded)
     * @return card state with switched face up card at index slot with decks' top card
     */
    public CardState withDrawnFaceUpCard(int slot) {
        // faire des copies de faceUpCard()?
        Objects.checkIndex(slot, Constants.FACE_UP_CARDS_COUNT);
        Preconditions.checkArgument(!isDeckEmpty());
        faceUpCards().set(slot, topDeckCard());
        // in the return statement we make sure we use deck but without the top card as it is
        // removed
        // once it replaces the face up card at index <code>slot</code>
        return new CardState(
                faceUpCards(), deckSize(), discardsSize(), deck.withoutTopCard(), discardCards);
    }

    /**
     * Returns the decks' top card.
     *
     * @throws IllegalArgumentException if deck is empty
     * @return top <code>deck</code> card
     */
    public Card topDeckCard() {
        return deck.topCard();
    }

    /**
     * Returns deck but without the top card.
     *
     * @throws IllegalArgumentException if deck is empty
     * @return <code>deck</code> without top card
     */
    public CardState withoutTopDeckCard() {
        Preconditions.checkArgument(!isDeckEmpty());
        return new CardState(
                faceUpCards(),
                deck.withoutTopCard().size(),
                discardsSize(),
                deck.withoutTopCard(),
                discardCards);
    }

    /**
     * Returns new shuffled deck made up of the discards.
     *
     * @param rng random
     * @throws IllegalArgumentException if deck is NOT empty
     * @return card state from a new deck created from discards
     */
    public CardState withDeckRecreatedFromDiscards(Random rng) {
        Preconditions.checkArgument(isDeckEmpty());
        // creating a shuffled deck from the discards as they're in a SortedBag thanks to
        // Deck.of(...)
        Deck<Card> randomDeckFromDiscards = Deck.of(discardCards, rng);
        return new CardState(
                faceUpCards(),
                deckSize(),
                randomDeckFromDiscards.size(),
                randomDeckFromDiscards,
                SortedBag.of());
    }

    /**
     * Returns card state but with additional cards added to the discards.
     *
     * @param additionalDiscards additional cards added to discards
     * @return cardState with additional cards to the discards.
     */
    public CardState withMoreDiscardedCards(SortedBag<Card> additionalDiscards) {
        return new CardState(
                faceUpCards(), deckSize(), additionalDiscards.size(), deck, additionalDiscards);
    }
}
