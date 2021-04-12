package ch.epfl.tchu.net;

/**
 * Enumeration of all the possible messages sent to the players`.
 *
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public enum MessageId {
    INIT_PLAYERS,
    RECEIVE_INFO,
    UPDATE_STATE,
    SET_INITIAL_TICKETS,
    CHOOSE_INITIAL_TICKETS,
    NEXT_TURN,
    CHOOSE_TICKETS,
    DRAW_SLOT,
    ROUTE,
    CARDS,
    CHOOSE_ADDITIONAL_CARDS
}
