package Model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class Player {
    /**
     * The hand of the player, max Cards with 2 players are 30. Can be changed depending on memory constraints.
     */
    private final List<Byte> hand;
    private final int tricksCalled;
    private final int tricksWon;
    private final int score;
    private final boolean isHuman;
    private final UnaryOperator<Byte> valueMask = n -> (byte) (n & 0b00111111);
    private final UnaryOperator<Byte> colorMask = n -> (byte) (n & 0b11000000);


    private Player(List<Byte> hand, int tricksCalled, int tricksWon, int score, boolean isHuman) {
        this.hand = hand;
        this.tricksCalled = tricksCalled;
        this.tricksWon = tricksWon;
        this.score = score;
        this.isHuman = isHuman;

    }
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
        return new Player(hand, tricksCalled, tricksWon+1, score, isHuman);
    }
    public Player addToScore(int points) {
        return new Player(hand, tricksCalled, tricksWon+1, score+points, isHuman);
    }
    public int getCalledTricks() {
        return tricksCalled;
    }
    public int getWonTricks() {
        return tricksWon;
    }
    /**
     * returns the hand of the player. It is a copy of the hand, so it cannot be modified.
     * @return the hand of the player.
     */

    public List<Byte> getHand() {
        return List.copyOf(hand);
    }

    // for testing purposes
    public byte getCard(int index) {
        return hand.get(index);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Player %d: ");
        for (byte card : hand) {
            String color = switch (colorMask.apply(card)) {
                case (byte) 0b00000000 -> "Red";
                case (byte) 0b01000000 -> "Green";
                case (byte) 0b10000000 -> "Blue";
                case (byte) 0b11000000 -> "Yellow";
                default -> "";
            };
            result.append((valueMask.apply(card) % 15)).append(" ").append(color).append("\n");
        }
        return result.toString();
    }
}
