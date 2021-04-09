package ch.epfl.tchu.game;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.gui.Info;
import ch.epfl.tchu.gui.StringsFr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InfoTest {
    private List<Card> cardList;
    private String playerOneName;
    private String playerTwoName;
    private List<String> playerNames;
    private String expected;
    private Info info1;
    private Info info2;
    private Route r;
    private SortedBag<Card> sBag;

    @BeforeEach
    void setUp() {
        cardList = Card.ALL.subList(0, 3); // BLACK, VIOLET, BLUE
        playerOneName = "BOB";
        playerTwoName = "KARL";
        playerNames = List.of(playerOneName, playerTwoName);
        info1 = new Info(playerOneName);
        info2 = new Info(playerTwoName);
        r = ChMap.routes().get(0);
        sBag = SortedBag.of(cardList);
    }

    @Test
    void cardNameReturnsCorrectNameWhenSingular() {
        assertEquals("noire", Info.cardName(cardList.get(0), 1));
        assertEquals("violette", Info.cardName(cardList.get(1), 1));
        assertEquals("bleue", Info.cardName(cardList.get(2), 1));
    }

    @Test
    void cardNameReturnsCorrectNameWhenPlural() {
        for (int count = 2; count < 10; count++) {
            assertEquals("noires", Info.cardName(cardList.get(0), count));
            assertEquals("violettes", Info.cardName(cardList.get(1), count));
            assertEquals("bleues", Info.cardName(cardList.get(2), count));
        }
    }

    @Test
    void draw() {
        int points = 5;

        expected =
                "\n"
                        + playerNames.get(0)
                        + " et "
                        + playerNames.get(1)
                        + " sont ex æqo avec "
                        + points
                        + " points !\n";
        assertEquals(expected, Info.draw(playerNames, points));
    }

    @Test
    void willPlayFirstReturnsCorrectFirstPlayer() {
        expected = playerOneName + " jouera en premier.\n\n";
        assertEquals(expected, info1.willPlayFirst());
    }

    @Test
    void keptCorrectAmountOfTickets() {
        expected = playerOneName + " a gardé " + 2 + " billets.\n";
        assertEquals(expected, info1.keptTickets(2));
        expected = playerTwoName + " a gardé " + 1 + " billet.\n";
        assertEquals(expected, info2.keptTickets(1));
    }

    @Test
    void nextPlayerCanPlay() {
        expected = "\nC'est à " + playerOneName + " de jouer.\n";
        assertEquals(expected, info1.canPlay());
        expected = "\nC'est à " + playerTwoName + " de jouer.\n";
        assertEquals(expected, info2.canPlay());
    }

    @Test
    void drewTicketsReturnsCorrectStringWhenSingularOrPlural() {
        expected = playerOneName + " a tiré " + 1 + " billet...\n";
        assertEquals(expected, info1.drewTickets(1));
        expected = playerTwoName + " a tiré " + 2 + " billets...\n";
        assertEquals(expected, info2.drewTickets(2));
    }

    @Test
    void drewBlindCard() {
        expected = playerOneName + " a tiré une carte de la pioche.\n";
        assertEquals(expected, info1.drewBlindCard());
    }

    @Test
    void drewVisibleCardReturnsCorrectString() {
        Card cardForThisTest = Card.GREEN;
        expected =
                playerOneName
                        + " a tiré une carte "
                        + Info.cardName(cardForThisTest, 1)
                        + " visible.\n";
        assertEquals(expected, info1.drewVisibleCard(cardForThisTest));
    }

    @Test
    void claimedRouteReturnsCorrectString() {
        expected =
                playerOneName
                        + " a pris possession de la route "
                        + r.station1().name()
                        + " – "
                        + r.station2().name()
                        + " au moyen de 1 noire, 1 violette et 1 bleue.\n";
        assertEquals(expected, info1.claimedRoute(r, sBag));
    }

    @Test
    void claimedRouteReturnsCorrectStringWithDifferentCards() {
        cardList = List.of(Card.RED, Card.RED, Card.LOCOMOTIVE);
        sBag = SortedBag.of(cardList);
        expected =
                playerOneName
                        + " a pris possession de la route "
                        + r.station1().name()
                        + " – "
                        + r.station2().name()
                        + " au moyen de 2 rouges et 1 locomotive.\n";
        assertEquals(expected, info1.claimedRoute(r, sBag));
    }

    @Test
    void attemptsTunnelClaimReturnsCorrectString() {
        r = ChMap.routes().get(4);
        expected =
                playerOneName
                        + " tente de s'emparer du tunnel "
                        + r.station1().name()
                        + " – "
                        + r.station2().name()
                        + " au moyen de 1 noire, 1 violette et 1 bleue !\n";
        assertEquals(expected, info1.attemptsTunnelClaim(r, sBag));
    }

    @Test
    void drewAdditionalCards() {
        expected =
                "Les cartes supplémentaires sont 1 noire, 1 violette et 1 bleue. "
                        + "Elles impliquent un coût additionnel de 2 cartes.\n";
        assertEquals(expected, info1.drewAdditionalCards(sBag, 2));
        sBag = SortedBag.of(List.of(Card.VIOLET, Card.BLUE, Card.BLUE));
        expected =
                "Les cartes supplémentaires sont 1 violette et 2 bleues."
                        + " Elles n'impliquent aucun coût additionnel.\n";
        assertEquals(expected, info1.drewAdditionalCards(sBag, 0));
    }

    @Test
    void assertThrowsExceptionIfDrawnCardsDifferentThanThree() {
        SortedBag<Card> a = SortedBag.of(List.of(Card.BLUE, Card.VIOLET, Card.BLUE, Card.VIOLET));
        assertThrows(IllegalArgumentException.class, () -> info1.drewAdditionalCards(a, 1));
        SortedBag<Card> b = SortedBag.of(List.of(Card.BLUE, Card.VIOLET));
        assertThrows(IllegalArgumentException.class, () -> info1.drewAdditionalCards(b, 2));
    }

    @Test
    void didNotClaimRouteReturnsCorrectString() {
        expected =
                playerOneName
                        + " n'a pas pu (ou voulu) s'emparer de la route "
                        + r.station1().name()
                        + " – "
                        + r.station2()
                        + ".\n";
        assertEquals(expected, info1.didNotClaimRoute(r));
    }

    @Test
    void lastTurnBeginsReturnsCorrectString() {
        expected = "\n" + playerOneName + " n'a plus que 1 wagon, le dernier tour commence !\n";
        assertEquals(expected, info1.lastTurnBegins(1));
        expected = "\n" + playerTwoName + " n'a plus que 2 wagons, le dernier tour commence !\n";
        assertEquals(expected, info2.lastTurnBegins(2));
    }

    @Test
    void assertThrowsExceptionIfCountCountOutOfBounds() {
        assertThrows(IllegalArgumentException.class, () -> info1.lastTurnBegins(3));
    }

    @Test
    void getsLongestTrailBonusReturnsCorrectString() {
        List<Route> listOfRoutes = ChMap.routes().subList(0, 5);
        Trail longest = Trail.longest(listOfRoutes);
        expected =
                "\n"
                        + playerOneName
                        + " reçoit un bonus de 10 points pour le plus long trajet ("
                        + longest.station1().name()
                        + StringsFr.EN_DASH_SEPARATOR
                        + longest.station2().name()
                        + ").\n";
        assertEquals(expected, info1.getsLongestTrailBonus(longest));
    }

    @Test
    void winningReturnsCorrectString() {
        expected = "\n" + playerOneName + " remporte la victoire avec 3 points, contre 1 point !\n";
        assertEquals(expected, info1.won(3, 1));
        expected = "\n" + playerOneName + " remporte la victoire avec 1 point, contre 1 point !\n";
        assertEquals(expected, info1.won(1, 1));
    }
}
