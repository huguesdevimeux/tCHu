package ch.epfl.tchu.game;

import java.util.List;

/**
 * Represents all the actions a player can take in the game.
 *
 * @author Luca Mouchel (324748)
 * @author Hugues Devimeux (327282)
 */
enum TurnKind {
    /** Enumeration of all the actions a player can take. */
    DRAW_TICKETS,
    DRAW_CARDS,
    CLAIM_ROUTE;

    /** same function as ALL attribute in Color Card, stocks every element of the enum in a List */
    public static final List<TurnKind> ALL = List.of(TurnKind.values());
}
