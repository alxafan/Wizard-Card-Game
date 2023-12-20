package Model;

import java.util.ArrayList;
import java.util.List;

public class Player {
    /**
     * The hand of the player, max Cards with 2 players are 30. Can be changed depending on memory constraints.
     */
    private final List<Byte> hand;
    private final int tricksCalled;
    private final int tricksWon;
    private final int score;
    private final boolean isHuman;
    private final int playerNumber;

    private Player(List<Byte> hand, int tricksCalled, int tricksWon, int score, boolean isHuman, int playerNumber) {
        this.hand = hand;
        this.tricksCalled = tricksCalled;
        this.tricksWon = tricksWon;
        this.score = score;
        this.isHuman = isHuman;
        this.playerNumber = playerNumber;
    }
    Player(int playerNumber) {
        this(List.of(), 0, 0, 0, true, playerNumber);
    }

    Player addCard(byte card) {
        ArrayList<Byte> newHand = new ArrayList<>(hand);
        newHand.add(card);
        return new Player(List.copyOf(newHand), tricksCalled, tricksWon, score, isHuman, playerNumber);
    }
    Player removeCard(byte card) {
        ArrayList<Byte> newHand = new ArrayList<>(hand);
        newHand.remove((Byte) card);
        return new Player(List.copyOf(newHand), tricksCalled, tricksWon, score, isHuman, playerNumber);
    }
    Player addWonTrick() {
        return new Player(hand, tricksCalled, tricksWon+1, score, isHuman, playerNumber);
    }
    Player addToScore(int points) {
        return new Player(hand, tricksCalled, tricksWon+1, score+points, isHuman, playerNumber);
    }
    /**
     * returns the hand of the player. It is a copy of the hand, so it cannot be modified.
     * @return the hand of the player.
     */
    List<Byte> getHand() {
        return List.copyOf(hand);
    }
    Player setTricksCalled(int tricksCalled) {
        return new Player(hand, tricksCalled, tricksWon, score, isHuman, playerNumber);
    }
    public int getCalledTricks() {
        return tricksCalled;
    }
    public int getWonTricks() {
        return tricksWon;
    }
    public int getScore() {
        return score;
    }
    public byte getCard(int index) {
        return hand.get(index);
    }


    /*
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Player %d: ");
        hand.forEach(card -> result.append(WizardModel.cardToString(card)).append("\n"));
        return result.toString();
    }
    */
}