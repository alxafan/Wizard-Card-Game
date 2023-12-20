package Model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

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


    // Testing, remove later

    private static final UnaryOperator<Byte> valueMask = n -> (byte) (n & 0b00111111);
    private static final UnaryOperator<Byte> colorMask = n -> (byte) (n & 0b11000000);
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Player %d: \n");
        hand.forEach(card -> result.append(cardToString(card)).append("\n"));
        result.append("Tricks called: ").append(tricksCalled).append("\n");
        result.append("Tricks won: ").append(tricksWon).append("\n");
        return result.toString();
    }
    String cardToString(byte card) {
        return valueMask.apply(card)%15 + " " + switch (colorMask.apply(card)) {
            case (byte) 0b00000000 -> "Red";
            case (byte) 0b01000000 -> "Green";
            case (byte) 0b10000000 -> "Blue";
            case (byte) 0b11000000 -> "Yellow";
            default -> "";
        };
    }
}