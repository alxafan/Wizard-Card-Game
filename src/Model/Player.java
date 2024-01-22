package Model;

import java.util.ArrayList;
import java.util.List;

public record Player(List<Byte> hand, int tricksCalled, int tricksWon, int score, boolean hasCalledTrick) {
    public Player() {this(List.of(), 0, 0, 0, false);}
    public Player addCard(byte card) {
        ArrayList<Byte> newHand = new ArrayList<>(hand);
        newHand.add(card);
        return new Player(List.copyOf(newHand), tricksCalled, tricksWon, score, hasCalledTrick);
    }
    public Player removeCard(byte card) {
        ArrayList<Byte> newHand = new ArrayList<>(hand);
        newHand.remove((Byte) card);
        return new Player(List.copyOf(newHand), tricksCalled, tricksWon, score, hasCalledTrick);
    }
    public Player setTricksWon(int amount) {return new Player(hand, tricksCalled, amount, score, hasCalledTrick);}
    public Player addToScore(int points) {return new Player(hand, tricksCalled, tricksWon, score + points, hasCalledTrick);}
    public Player setTricksCalled(int amount) {return new Player(hand, amount, tricksWon, score, true);}
    public Player resetCalledTricks(){return new Player(hand, tricksCalled, tricksWon, score, false);}


    // toString for Testing
    public String toString() {
        StringBuilder result = new StringBuilder("Player %d: \n");
        hand.forEach(card -> result.append(WizardModel.cardToString(card)).append("\n"));
        result.append("Tricks called: ").append(tricksCalled).append("\n").append("Tricks won: ").append(tricksWon).append("\n");
        return result.toString();
    }

}