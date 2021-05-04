package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.Card;
import ch.epfl.tchu.game.Route;
import ch.epfl.tchu.game.Ticket;

/**
 * Contains 5 functional interfaces, each responsible for one type of action the player can make.
 *
 * @author Luca Mouchel (324748)
 * @author Hugues Devimeux (327282)
 */
public interface ActionHandlers {

    @FunctionalInterface
    interface DrawTicketsHandler {
        /**
         * Called upon when the player wants to collect tickets.
         */
        void onDrawTickets();
    }

    @FunctionalInterface
    interface DrawCardHandler {
        /**
         * Called upon when the player wants to collect the card at given index. If {@code
         * indexOfChosenCard} is in [0,4] -> face up card, else deck top card.
         *
         * @param indexOfChosenCard index of the chosen card
         */
        void onDrawCard(int indexOfChosenCard);
    }

    @FunctionalInterface
    interface ClaimRouteHandler {
        /**
         * Called upon when the player wants to claim {@code claimedRoute} route using {@code
         * initialClaimCards}.
         *
         * @param claimedRoute      the route the player wants to claim
         * @param initialClaimCards the cards the player uses to claim the route (initially)
         */
        void onClaimRoute(Route claimedRoute, SortedBag<Card> initialClaimCards);
    }

    @FunctionalInterface
    interface ChooseTicketsHandler {
        /**
         * Called upon when the player chooses to keep {@code chosenTickets}.
         *
         * @param chosenTickets the kept tickets
         */
        void onChooseTickets(SortedBag<Ticket> chosenTickets);
    }

    @FunctionalInterface
    interface ChooseCardsHandler {
        /**
         * Called upon when the player uses {@code usedCardsToClaimRoute} to claim a route, as initial
         * or additional cards. If it is additional cards, the argument can be empty, which means the
         * player does not want to take the tunnel.
         *
         * @param usedCardsToClaimRoute cards used to claim the route, be it initial or additional cards
         */
        void onChooseCards(SortedBag<Card> usedCardsToClaimRoute);
    }
}
