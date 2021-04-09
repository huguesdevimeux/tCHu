package ch.epfl.tchu.game;

import java.util.List;

/**
 * Represents the identity of a player. Because the game only has two players, we only have two
 * identities.
 *
 * @author Luca Mouchel (324748)
 * @author Hugues Devimeux (327282)
 */
public enum PlayerId {
    PLAYER_1,
    PLAYER_2;

    /**
     * Same function as ALL attribute in other classes. Stocks every element (ie the two players) in
     * a list
     */
    public static final List<PlayerId> ALL = List.of(PlayerId.values());

    /** Attribute that counts the total amount of elements in ALL. */
    public static final int COUNT = ALL.size();

    /**
     * Returns the ID of the next player
     *
     * @return other player's ID
     */
    public PlayerId next() {
        return this.equals(PLAYER_1) ? PLAYER_2 : PLAYER_1;
    }
}
