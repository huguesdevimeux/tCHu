// Attention : cette classe n'est *pas* un test JUnit, et son code n'est
// pas destiné à être exécuté. Son seul but est de vérifier, autant que
// possible, que les noms et les types des différentes entités à définir
// pour cette étape du projet sont corrects.

final class SignatureChecks_2 {
    private SignatureChecks_2() {}

    void checkRoute() {
        v01 = new ch.epfl.tchu.game.Route(v02, v03, v03, v04, v05, v06);
        v04 = v01.additionalClaimCardsCount(v07, v07);
        v04 = v01.claimPoints();
        v06 = v01.color();
        v02 = v01.id();
        v04 = v01.length();
        v05 = v01.level();
        v08 = v01.possibleClaimCards();
        v03 = v01.station1();
        v03 = v01.station2();
        v03 = v01.stationOpposite(v03);
        v09 = v01.stations();
    }

    void checkLevel() {
        v05 = ch.epfl.tchu.game.Route.Level.OVERGROUND;
        v05 = ch.epfl.tchu.game.Route.Level.UNDERGROUND;
        v05 = ch.epfl.tchu.game.Route.Level.valueOf(v02);
        v10 = ch.epfl.tchu.game.Route.Level.values();
    }

    void checkTrail() {
        v12 = ch.epfl.tchu.game.Trail.longest(v11);
        v04 = v12.length();
        v03 = v12.station1();
        v03 = v12.station2();
        v02 = v12.toString();
    }

    ch.epfl.tchu.game.Route v01;
    java.lang.String v02;
    ch.epfl.tchu.game.Station v03;
    int v04;
    ch.epfl.tchu.game.Route.Level v05;
    ch.epfl.tchu.game.Color v06;
    ch.epfl.tchu.SortedBag<ch.epfl.tchu.game.Card> v07;
    java.util.List<ch.epfl.tchu.SortedBag<ch.epfl.tchu.game.Card>> v08;
    java.util.List<ch.epfl.tchu.game.Station> v09;
    ch.epfl.tchu.game.Route.Level[] v10;
    java.util.List<ch.epfl.tchu.game.Route> v11;
    ch.epfl.tchu.game.Trail v12;
}
