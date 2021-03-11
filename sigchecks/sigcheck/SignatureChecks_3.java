// Attention : cette classe n'est *pas* un test JUnit, et son code n'est
// pas destiné à être exécuté. Son seul but est de vérifier, autant que
// possible, que les noms et les types des différentes entités à définir
// pour cette étape du projet sont corrects.

final class SignatureChecks_3 {
    private SignatureChecks_3() {}

    void checkDeck() {
        v03 = ch.epfl.tchu.game.Deck.of(v01, v02);
        v05 = v04.isEmpty();
        v06 = v04.size();
        v07 = v04.topCard();
        v08 = v04.topCards(v06);
        v09 = v04.withoutTopCard();
        v09 = v04.withoutTopCards(v06);
    }

    void checkPublicCardState() {
        v10 = new ch.epfl.tchu.game.PublicCardState(v11, v06, v06);
        v06 = v10.deckSize();
        v06 = v10.discardsSize();
        v12 = v10.faceUpCard(v06);
        v11 = v10.faceUpCards();
        v05 = v10.isDeckEmpty();
        v06 = v10.totalSize();
    }

    void checkCardState() {
        v14 = ch.epfl.tchu.game.CardState.of(v13);
        v12 = v14.topDeckCard();
        v14 = v14.withDeckRecreatedFromDiscards(v02);
        v14 = v14.withDrawnFaceUpCard(v06);
        v14 = v14.withMoreDiscardedCards(v15);
        v14 = v14.withoutTopDeckCard();
    }

    void checkInfo() {
        v16 = new ch.epfl.tchu.gui.Info(v17);
        v17 = ch.epfl.tchu.gui.Info.cardName(v12, v06);
        v17 = ch.epfl.tchu.gui.Info.draw(v18, v06);
        v17 = v16.attemptsTunnelClaim(v19, v15);
        v17 = v16.canPlay();
        v17 = v16.claimedRoute(v19, v15);
        v17 = v16.didNotClaimRoute(v19);
        v17 = v16.drewAdditionalCards(v15, v06);
        v17 = v16.drewBlindCard();
        v17 = v16.drewTickets(v06);
        v17 = v16.drewVisibleCard(v12);
        v17 = v16.getsLongestTrailBonus(v20);
        v17 = v16.keptTickets(v06);
        v17 = v16.lastTurnBegins(v06);
        v17 = v16.willPlayFirst();
        v17 = v16.won(v06, v06);
    }

    interface C extends Comparable<C> {}
    ch.epfl.tchu.SortedBag<C> v01;
    java.util.Random v02;
    ch.epfl.tchu.game.Deck<C> v03;
    ch.epfl.tchu.game.Deck<C> v04;
    boolean v05;
    int v06;
    C v07;
    ch.epfl.tchu.SortedBag<C> v08;
    ch.epfl.tchu.game.Deck<C> v09;
    ch.epfl.tchu.game.PublicCardState v10;
    java.util.List<ch.epfl.tchu.game.Card> v11;
    ch.epfl.tchu.game.Card v12;
    ch.epfl.tchu.game.Deck<ch.epfl.tchu.game.Card> v13;
    ch.epfl.tchu.game.CardState v14;
    ch.epfl.tchu.SortedBag<ch.epfl.tchu.game.Card> v15;
    ch.epfl.tchu.gui.Info v16;
    java.lang.String v17;
    java.util.List<java.lang.String> v18;
    ch.epfl.tchu.game.Route v19;
    ch.epfl.tchu.game.Trail v20;
}
