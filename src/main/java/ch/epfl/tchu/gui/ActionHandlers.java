package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.Card;
import ch.epfl.tchu.game.Route;
import ch.epfl.tchu.game.Ticket;

/**
 * Contains 5 functional interfaces, each responsible for one type
 * of action the player can make.
 *
 * @author Luca Mouchel (324748)
 * @author Hugues Devimeux (327282)
 */
public interface ActionHandlers {

    /** Called upon when the player wants to collect tickets. */
    @FunctionalInterface
    interface DrawTicketsHandler{
        void onDrawTickets();
    }

    /**
     * Called upon when the player wants to collect the card at given index.
     * If {@code indexOfChosenCard} is in [0,4] -> face up card, else deck top card.
     */
    @FunctionalInterface
    interface DrawCardHandler{
        void onDrawCard(int indexOfChosenCard);
    }

    /**
     * Called upon when the player wants to claim {@code claimedRoute} route using
     * {@code initialClaimCards}.
     */
    @FunctionalInterface
    interface ClaimRouteHandler{
        void onClaimRoute(Route claimedRoute, SortedBag<Card> initialClaimCards);
    }

    /** Called upon when the player chooses to keep {@code chosenTickets}. */
    @FunctionalInterface
    interface ChooseTicketsHandler{
        void onChooseTickets(SortedBag<Ticket> chosenTickets);
    }

    /**
     * Called upon when the player uses {@code usedCardsToClaimRoute} to claim a route,
     * as initial or additional cards. If it is additional cards,
     * the argument can be empty, which means the player does not want to take the tunnel.
     */
    @FunctionalInterface
    interface ChooseCardsHandler{
        void onChooseCards(SortedBag<Card> usedCardsToClaimRoute);
    }
}
