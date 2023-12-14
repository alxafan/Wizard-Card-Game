package Model;

/* Jshell Testing:
WizardModel w = new WizardModel();
for (int i = 0; i < 4; i++) w.addPlayer(new Player());
w.dealCards();
w
w.playCard(w.players.get(0).getHand().get(0));
w.playCard(w.players.get(1).getHand().get(0));
w.playCard(w.players.get(2).getHand().get(0));
w.playCard(w.players.get(3).getHand().get(0));
w.endTrick();
w.dealCards();
w
w.playCard(w.players.get(0).getHand().get(0));
 */

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class WizardModel {
    // Maybe change this to an Array and make the controller handle the players adding?
    public ArrayList<Player> players = new ArrayList<>(6);
    // The cards in the trick are stored in the order they are played, sadly has to be HashMap, because there is no way of knowing which player played which card otherwise
    ArrayList<Byte> trick =  new ArrayList<>(6);
    // Stores the current round and the starting player
    private int round = 0;
    // Player that is making the first move in the current trick, sadly necessary as the winner of a trick begins the next round and this can't be stored without a variable
    private int startingPlayer = 0;
    // Stores the trump-card, only the color is necessary, but a byte is not expensive memory-wise
    private byte trump;
    // Lambda-Expressions to apply according byte-masks
    private final UnaryOperator<Byte> valueMask = n -> (byte) (n & 0b00111111);
    private final UnaryOperator<Byte> colorMask = n -> (byte) (n & 0b11000000);
    // Card value 14, no need for a fool version, because it's equal to 0
    private final byte wizard = 0b00001110;
    private final byte fool = 0b00000000;

    /**
     * Method that deals out cards to the players.
     */
    public void dealCards() {
        ArrayList<Byte> deck = new ArrayList<>(List.of((byte) 0,(byte) 1,(byte) 2,(byte) 3,(byte) 4,(byte) 5,(byte) 6,(byte) 7,(byte) 8,(byte) 9,(byte) 10,(byte) 11,(byte) 12,(byte) 13,(byte) 14,(byte) 64,(byte) 65,(byte) 66,(byte) 67,(byte) 68,(byte) 69,(byte) 70,(byte) 71,(byte) 72,(byte) 73,(byte) 74,(byte) 75,(byte) 76,(byte) 77,(byte) 78,(byte) 128,(byte) 129,(byte) 130,(byte) 131,(byte) 132,(byte) 133,(byte) 134,(byte) 135,(byte) 136,(byte) 137,(byte) 138,(byte) 139,(byte) 140,(byte) 141,(byte) 142,(byte) 192,(byte) 193,(byte) 194,(byte) 195,(byte) 196,(byte) 197,(byte) 198,(byte) 199,(byte) 200,(byte) 201,(byte) 202,(byte) 203,(byte) 204,(byte) 205,(byte) 206));
        for (int i = 0; i < round+1; i++) {
            players.forEach(p -> p.addCard(deck.remove((int) (Math.random()*deck.size()))));
        }
        trump = deck.remove((int) (Math.random()*deck.size()));
        System.out.println("Trump: " + colorMask.apply(trump));
        /* Function used to generate all the cards in the list
        for (int i = 0, j = -1; i < 60; i++) {
            if (i%15 == 0) j++;
            System.out.print("(byte) " + ((j<<6)+(i%15)) + ",");
        }
         */
    }

    /**
     * Method which plays a card into the trick, while making sure that no wizard rules are broken. Keep in mind that this only works if the controller makes sure that the player who's turn it is plays
     * @param card the card to be played
     */
    public void playCard(byte card) {
        // Has problems if a player whose turn it is not plays a card, won't be possible with the controller though

        boolean wizardPlayedFirst = (!trick.isEmpty() && trick.stream().filter(c -> valueMask.apply(c) != (byte) 0).findFirst().orElse((byte) 15).equals(wizard));
        // equals fool, if no non-fool card has been played yet
        byte firstNonFoolCard = trick.stream().filter(c -> valueMask.apply(c) != (byte) 0).findFirst().orElse(card);
        // maybe turn this into a separate function?

        if (!wizardPlayedFirst &&
                valueMask.apply(card) != wizard  &&
                valueMask.apply(card) != fool &&
                firstNonFoolCard != fool &&
                !colorMask.apply(firstNonFoolCard).equals(colorMask.apply(card)) &&
                // Move this to the controller?
                players.get(trick.size()).getHand().stream().anyMatch(c -> colorMask.apply(c).equals(colorMask.apply(firstNonFoolCard))) // this part is necessary, because it is possible that a player does not have a matching color on his hand
        ) {
            System.out.println("Player " + trick.size() + " must play a card of the same color as the first card played this trick.");
            return;
        }

        // Set required color and trump flags, if they apply
        players.get(trick.size()).removeCard(card);
        System.out.println(card);
        if (colorMask.apply(card).equals(colorMask.apply(firstNonFoolCard))) card |= 0b00010000;
        System.out.println(card);
        if (colorMask.apply(card).equals(colorMask.apply(trump))) card |= 0b00100000;
        System.out.println(card);
        trick.add(card);
    }

    public void endTrick() {
        if (trick.size() != players.size()) {
            System.out.println("Not all players have played a card yet.");
            return;
        }
        int winner = 0;
        byte winningCard;

        winningCard = valueMask.apply(trick.stream().
                                            filter(n -> (n & wizard) == wizard).
                                            findFirst().
                                            orElse(trick.stream().
                                                            map(valueMask).
                                                            reduce((byte) 0, (a, b) -> (a > b ? a : b)))
        );

        for(int i = 0; i < trick.size(); i++) {
            if (valueMask.apply(trick.get(i)) == winningCard) {
                winner = (i+startingPlayer) % players.size();
                break;
            }
        }

        System.out.println("Winner: " + winner);

        startingPlayer = winner;
        players.get(winner).addWonTrick();
        trick.clear();
        if (players.get(winner).getHand().isEmpty()) endRound();
    }

    private void endRound() {
        players.forEach(p -> p.addScore(p.getCalledTricks()-p.getWonTricks() == 0 ? 20+p.getWonTricks()*10 : -Math.abs(p.getCalledTricks()-p.getWonTricks())*10));
        round++;
        startingPlayer = round;
    }

    public boolean isGameOver() {
        return round == (60/players.size())+1;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        trick.forEach(c -> result.append(valueMask.apply(c)).append("\n"));
        for (Player player : players) {
            result.append(player.toString().formatted(players.indexOf(player))).append("\n");
        }
        return result.toString();
    }
}
