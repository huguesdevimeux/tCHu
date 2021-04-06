package ch.epfl.tchu.game;

import ch.epfl.tchu.SortedBag;
import ch.epfl.test.TestRandomizer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class GameTest {

    @Test
    void testStandardGame() {
        Map<PlayerId, String> playersNames =
                Map.of(PlayerId.PLAYER_1, "Alice", PlayerId.PLAYER_2, "Bob");
        Map<PlayerId, Player> players =
                PlayerId.ALL.stream()
                        .collect(
                                Collectors.toMap(
                                        playerId -> playerId, StandardTestedPlayer::new));
        Player mockedPlayer = mock(StandardTestedPlayer.class);
        Game.play(players, playersNames, SortedBag.of(ChMap.tickets()), TestRandomizer.newRandom());
    }

    // Players utils for tests.

    private static class StandardTestedPlayer implements Player {

        private final int MAX_TURNS = 200;

        private final PlayerId ownId;

        private final int turnCounter = 0;
        private PublicGameState gameState;
        private PlayerState playerState;
        private SortedBag<Ticket> intitialTickets;

        public StandardTestedPlayer(PlayerId ownId) {

            this.ownId = ownId;
        }

        /**
         * Called upon when starting the game to communicate the ID of the players as well as the
         * name of all the players, including his or hers (in <code>playernames</code>).
         *
         * @param ownId       the players' id
         * @param playerNames all the names of all the players
         */
        @Override
        public void initPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
            assertEquals(this.ownId, ownId);
        }

        /**
         * Called when an information has to be transmitted to a player under the form of a String.
         *
         * @param info information to be transmitted
         */
        @Override
        public void receiveInfo(String info) {
            System.out.println(info);
            // Meant to be mocked.
        }

        /**
         * Called when the game state changes - to inform the player of the <code>newState</code> as
         * well as his/her own state.
         *
         * @param newState new state of the game
         * @param ownState the state of the player
         */
        @Override
        public void updateState(PublicGameState newState, PlayerState ownState) {

            this.gameState = newState;
            this.playerState = ownState;
        }

        /**
         * Called at the BEGINNING of the game to communicate the 5 tickets that have been
         * distributed.
         *
         * @param tickets 5 tickets distributed
         */
        @Override
        public void setInitialTicketChoice(SortedBag<Ticket> tickets) {
            assertEquals(5, tickets.size());
            this.intitialTickets = tickets;
            // Meant to be mocked. Called once!
        }

        /**
         * Called at the beginning of the game to ask the player which tickets the player wishes to
         * keep (via <code>setInitialTicketChoice</code>).
         *
         * @return the bag of tickets the player wants to keep
         */
        @Override
        public SortedBag<Ticket> chooseInitialTickets() {
            return SortedBag.of(this.intitialTickets.get(0));
        }

        /**
         * Called at the beginning of each round, to know what action the player decides to take.
         *
         * @return the player's chosen course of action at the beginning of the round
         */
        @Override
        public TurnKind nextTurn() {
            return null;
        }

        /**
         * Called when the player decides to draw additional tickets during the game, as to
         * communication the drawn tickets and to know which ones the player keeps.
         *
         * @param options bag of tickets the player can choose from
         * @return communication of drawn tickets to know which ones are kept
         */
        @Override
        public SortedBag<Ticket> chooseTickets(SortedBag<Ticket> options) {
            return null;
        }

        /**
         * Called when the player has decided to draw cards (can be locomotives) to know where he
         * wants to to draw them from. It can be a faceUp card - to which the method returns int in
         * [0,4]. It can be a deck card - to which the method returns <code>Constants.DECK_SLOT
         * </code> aka -1.
         *
         * @return an int that depends on what type of card is drawn
         */
        @Override
        public int drawSlot() {
            return 0;
        }

        /**
         * Called when the player has decided (attempted to) take over a route - to know which route
         * it is.
         *
         * @return the route the player has tried to take over
         */
        @Override
        public Route claimedRoute() {
            return null;
        }

        /**
         * Called when the player has decided to (or attempted to) take over a route - to know which
         * card(s) they want to use initially to do so.
         *
         * @return the initial cards the player wants to use to take over a route
         */
        @Override
        public SortedBag<Card> initialClaimCards() {
            return null;
        }

        /**
         * Called when the player has decided to attempt to take a tunnel (UNDERGROUND route) and
         * that additional cards have to be used - to ultimately know which cards the player wants
         * to use. The available cards he can use is the parameter options. If the returned
         * SortedBag is empty, it means the player doesn't want (or can't) pick any options.
         *
         * @param options list of cards with which the player can choose to take over a tunnel
         * @return the additional cards the player wants to use to take over a tunnel, or an empty
         * sortedBag if he can't or does not want to
         */
        @Override
        public SortedBag<Card> chooseAdditionalCards(List<SortedBag<Card>> options) {
            return null;
        }
    }
}
