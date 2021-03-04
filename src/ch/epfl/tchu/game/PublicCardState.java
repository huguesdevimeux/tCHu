package ch.epfl.tchu.game;

import java.util.List;

/**
 * @author Hugues Devimeux (327282)
 */
public class PublicCardState {

    public PublicCardState(List<Card> faceUpCards, int deckSize, int discardsSize) {

    }

    /* qui retourne le nombre total de cartes qui ne sont pas en main des joueurs, à savoir les 5 dont la face est visible, celles de la pioche et celles de la défausse,*/
    public int totalSize(){throw new UnsupportedOperationException();}
    /* qui retourne les 5 cartes face visible, sous la forme d'une liste comportant exactement 5 éléments,*/
    public List<Card> faceUpCards(){throw new UnsupportedOperationException(); }
    /* qui retourne la carte face visible à l'index donné, ou lève IndexOutOfBoundsException (!) si cet index n'est pas compris entre 0 (inclus) et 5 (exclus),*/
    public Card faceUpCard(int slot){throw new UnsupportedOperationException();}
    /* qui retourne la taille de la pioche,*/
    public int deckSize(){throw  new UnsupportedOperationException(); }
    /* qui retourne vrai ssi la pioche est vide,*/
    public boolean isDeckEmpty(){throw  new UnsupportedOperationException();}
    /* qui retourne la taille de la défausse.*/
    public int discardsSize(){throw new UnsupportedOperationException();}




}
