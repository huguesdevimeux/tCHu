package ch.epfl.sigcheck;

// Attention : cette classe n'est *pas* un test JUnit, et son code n'est
// pas destiné à être exécuté. Son seul but est de vérifier, autant que
// possible, que les noms et les types des différentes entités à définir
// pour cette étape du projet sont corrects.

final class SignatureChecks_6 {
    private SignatureChecks_6() {}

    void checkGame() {
        ch.epfl.tchu.game.Game.play(v01, v02, v03, v04);
    }

    java.util.Map<ch.epfl.tchu.game.PlayerId, ch.epfl.tchu.game.Player> v01;
    java.util.Map<ch.epfl.tchu.game.PlayerId, java.lang.String> v02;
    ch.epfl.tchu.SortedBag<ch.epfl.tchu.game.Ticket> v03;
    java.util.Random v04;
}
