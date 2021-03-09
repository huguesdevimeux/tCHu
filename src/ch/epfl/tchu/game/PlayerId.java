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
    // caracterising player ID by its index in attribute ALL
    PLAYER_1(0),
    PLAYER_2(1);

    /**
     * Same function as ALL attribute in other classes. Stocks every element (ie the two players) in
     * a list
     */
    public static final List<PlayerId> ALL = List.of(PlayerId.values());

    /** Attribute that counts the total amount of elements in ALL. */
    public static final int COUNT = ALL.size();

    private final int playerIdIndex;

    /** Default player id constructor */
    PlayerId(int playerIdIndex) {
        this.playerIdIndex = playerIdIndex;
    }

    /**
     * Returns the ID of the next player
     *
     * @return other players' ID
     */
    public PlayerId next() {
        if (playerIdIndex == 0) return PLAYER_2;
        else return PLAYER_1;
    }
}
