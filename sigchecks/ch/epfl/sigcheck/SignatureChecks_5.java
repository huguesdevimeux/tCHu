package ch.epfl.sigcheck; // Attention : cette classe n'est *pas* un test JUnit, et son code n'est
// pas destiné à être exécuté. Son seul but est de vérifier, autant que
// possible, que les noms et les types des différentes entités à définir
// pour cette étape du projet sont corrects.

final class SignatureChecks_5 {
    private SignatureChecks_5() {}

    void checkPublicGameState() {
        v01 = new ch.epfl.tchu.game.PublicGameState(v02, v03, v04, v05, v04);
        v06 = v01.canDrawCards();
        v06 = v01.canDrawTickets();
        v03 = v01.cardState();
        v07 = v01.claimedRoutes();
        v04 = v01.currentPlayerId();
        v08 = v01.currentPlayerState();
        v04 = v01.lastPlayer();
        v08 = v01.playerState(v04);
        v02 = v01.ticketsCount();
    }

    void checkGameState() {
        v11 = ch.epfl.tchu.game.GameState.initial(v09, v10);
        v12 = v11.currentPlayerState();
        v11 = v11.forNextTurn();
        v06 = v11.lastTurnBegins();
        v12 = v11.playerState(v04);
        v13 = v11.topCard();
        v09 = v11.topTickets(v02);
        v11 = v11.withBlindlyDrawnCard();
        v11 = v11.withCardsDeckRecreatedIfNeeded(v10);
        v11 = v11.withChosenAdditionalTickets(v09, v09);
        v11 = v11.withClaimedRoute(v14, v15);
        v11 = v11.withDrawnFaceUpCard(v02);
        v11 = v11.withInitiallyChosenTickets(v04, v09);
        v11 = v11.withMoreDiscardedCards(v15);
        v11 = v11.withoutTopCard();
        v11 = v11.withoutTopTickets(v02);
    }

    void checkPlayer() {
        v15 = v16.chooseAdditionalCards(v17);
        v09 = v16.chooseInitialTickets();
        v09 = v16.chooseTickets(v09);
        v14 = v16.claimedRoute();
        v02 = v16.drawSlot();
        v16.initPlayers(v04, v18);
        v15 = v16.initialClaimCards();
        v19 = v16.nextTurn();
        v16.receiveInfo(v20);
        v16.setInitialTicketChoice(v09);
        v16.updateState(v01, v12);
    }

    ch.epfl.tchu.game.PublicGameState v01;
    int v02;
    ch.epfl.tchu.game.PublicCardState v03;
    ch.epfl.tchu.game.PlayerId v04;
    java.util.Map<ch.epfl.tchu.game.PlayerId, ch.epfl.tchu.game.PublicPlayerState> v05;
    boolean v06;
    java.util.List<ch.epfl.tchu.game.Route> v07;
    ch.epfl.tchu.game.PublicPlayerState v08;
    ch.epfl.tchu.SortedBag<ch.epfl.tchu.game.Ticket> v09;
    java.util.Random v10;
    ch.epfl.tchu.game.GameState v11;
    ch.epfl.tchu.game.PlayerState v12;
    ch.epfl.tchu.game.Card v13;
    ch.epfl.tchu.game.Route v14;
    ch.epfl.tchu.SortedBag<ch.epfl.tchu.game.Card> v15;
    ch.epfl.tchu.game.Player v16;
    java.util.List<ch.epfl.tchu.SortedBag<ch.epfl.tchu.game.Card>> v17;
    java.util.Map<ch.epfl.tchu.game.PlayerId, java.lang.String> v18;
    ch.epfl.tchu.game.Player.TurnKind v19;
    java.lang.String v20;
}
