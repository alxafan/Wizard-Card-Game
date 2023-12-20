package Model;

import java.util.ArrayList;
import java.util.List;

public record Player(
        List<Byte> hand,
        int tricksCalled,
        int tricksWon,
        int score,
        boolean isHuman

) {
    public Player() {
        this(List.of(), 0, 0, 0, true);
    }

    public Player addCard(byte card) {
        ArrayList<Byte> newHand = new ArrayList<>(hand);
        newHand.add(card);
        return new Player(List.copyOf(newHand), tricksCalled, tricksWon, score, isHuman);
    }

    public Player removeCard(byte card) {
        ArrayList<Byte> newHand = new ArrayList<>(hand);
        newHand.remove((Byte) card);
        return new Player(List.copyOf(newHand), tricksCalled, tricksWon, score, isHuman);
    }

    public Player addWonTrick() {
        return new Player(hand, tricksCalled, tricksWon + 1, score, isHuman);
    }

    public Player addToScore(int points) {
        return new Player(hand, tricksCalled, tricksWon, score + points, isHuman);
    }

    public Player setTricksCalled(int tricksCalled) {
        return new Player(hand, tricksCalled, tricksWon, score, isHuman);
    }

    // testing
    public byte getCard(int index) {
        return hand.get(index);
    }
}