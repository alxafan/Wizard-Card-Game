package Model;

import java.io.Serializable;
import java.util.*;

public record Player(List<Byte> hand, int tricksCalled, int tricksWon, int score, boolean hasCalledTrick) implements Serializable {
    Player() {this(List.of(), 0, 0, 0, false);}
    Player addCard(byte card) {
        ArrayList<Byte> newHand = new ArrayList<>(hand);
        newHand.add(card);
        return new Player(List.copyOf(newHand), tricksCalled, tricksWon, score, hasCalledTrick);
    }
    Player removeCard(byte card) {
        ArrayList<Byte> newHand = new ArrayList<>(hand);
        newHand.remove((Byte) card);
        return new Player(List.copyOf(newHand), tricksCalled, tricksWon, score, hasCalledTrick);
    }
    Player setTricksWon(int amount) {return new Player(hand, tricksCalled, amount, score, hasCalledTrick);}
    Player addToScore(int points) {return new Player(hand, tricksCalled, tricksWon, score + points, hasCalledTrick);}
    Player setTricksCalled(int amount) {return new Player(hand, amount, tricksWon, score, true);}
    Player resetTricks(){return new Player(hand, 0, 0, score, false);}

}