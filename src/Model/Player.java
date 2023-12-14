package Model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class Player {
    /**
     * The hand of the player, max Cards with 2 players are 30. Can be changed depending on memory constraints.
     */
    ArrayList<Byte> hand = new ArrayList<>(30);

    /**
     * The number of the player, from 0 to 5.
     */

    private int tricksCalled = 0;
    private int tricksWon = 0;
    private int score;
    private boolean isHuman;
    private final UnaryOperator<Byte> valueMask = n -> (byte) (n & 0b00111111);
    private final UnaryOperator<Byte> colorMask = n -> (byte) (n & 0b11000000);


    public void addCard(byte card) {
        hand.add(card);
    }
    public void removeCard(byte card) {
        hand.remove((Byte)card);
    }
    public void addWonTrick() {
        tricksWon++;
    }
    public void addScore(int score) {
        this.score += score;
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

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Player %d: ");
        for (byte card : hand) {
            String color = "";
            if (colorMask.apply(card) == (byte) 0b00000000) color = "Red";
            if (colorMask.apply(card) == (byte) 0b01000000) color = "Yellow";
            if (colorMask.apply(card) == (byte) 0b10000000) color = "Green";
            if (colorMask.apply(card) == (byte) 0b11000000) color = "Blue";
            /*
            switch (colorMask.apply(card)) {
                case (byte) 0b00000000: color = "Red";
                case (byte) 0b01000000: color = "Green";
                case (byte) 0b10000000: color = "Blue";
                case (byte) 0b11000000: color = "Yellow";
            }
            */
            result.append((valueMask.apply(card) % 15)).append(" ").append(color).append("\n");
        }
        return result.toString();
    }
}
