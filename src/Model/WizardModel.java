package Model;

/* Jshell Testing:
WizardModel w = new WizardModel();
for (int i = 0; i < 4; i++) w = w.addPlayer(new Player());
System.out.println(w = w.dealCards());
System.out.println(w = w.playCard(w.getPlayer(0).hand.get(0)));
System.out.println(w = w.playCard(w.getPlayer(1).hand.get(0)));
System.out.println(w = w.playCard(w.getPlayer(2).hand.get(0)));
System.out.println(w.endRound()); // Änderung wird nicht gespeichert
System.out.println(w = w.playCard(w.getPlayer(3).hand.get(0)));
System.out.println(w.undoPlayCard()); // Änderung wird nicht gespeichert
System.out.println(w = w.endTrick());
System.out.println(w = w.endRound());
System.out.println(w = w.dealCards());
 */

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class WizardModel implements IWizardModel{
    // Maybe change this to an Array and make the controller handle the players adding?
    private final List<Player> players;
    // The cards in the trick are stored in the order they are played, sadly has to be HashMap, because there is no way of knowing which player played which card otherwise
    private final List<Byte> trick;
    // Stores the current round and the starting player
    private final int round;
    // Player that is making the first move in the current trick, sadly necessary as the winner of a trick begins the next round and this can't be stored without a variable
    private final int startingPlayer;
    // Stores the trump-card, only the color is necessary, but a byte is not expensive memory-wise
    private final byte trump;
    // Lambda-Expressions to apply according byte-masks
    private final int totalTricksCalled;
    private final UnaryOperator<Byte> valueMask = n -> (byte) (n & 0b00111111);
    private final UnaryOperator<Byte> colorMask = n -> (byte) (n & 0b11000000);
    // Card value 14, no need for a fool version, because it's equal to 0
    private final byte wizard = 0b00001110;
    private final byte fool = 0b00000000;

    private WizardModel(List<Player> players, List<Byte> trick, int round, int startingPlayer, byte trump, int totalTricksCalled) {
        this.players = players;
        this.trick = trick;
        this.round = round;
        this.startingPlayer = startingPlayer;
        this.trump = trump;
        this.totalTricksCalled = totalTricksCalled;
    }
    public WizardModel() {
        this(List.of(), List.of(), 0, 0, (byte) 0, 0);
    }
    @Override
    public WizardModel newGame() {
        return new WizardModel();
    }
    /**
     * Method that deals out cards to the players.
     */
    @Override
    public WizardModel dealCards() {
        ArrayList<Byte> deck = new ArrayList<>(List.of((byte) 0,(byte) 1,(byte) 2,(byte) 3,(byte) 4,(byte) 5,(byte) 6,(byte) 7,(byte) 8,(byte) 9,(byte) 10,(byte) 11,(byte) 12,(byte) 13,(byte) 14,(byte) 64,(byte) 65,(byte) 66,(byte) 67,(byte) 68,(byte) 69,(byte) 70,(byte) 71,(byte) 72,(byte) 73,(byte) 74,(byte) 75,(byte) 76,(byte) 77,(byte) 78,(byte) 128,(byte) 129,(byte) 130,(byte) 131,(byte) 132,(byte) 133,(byte) 134,(byte) 135,(byte) 136,(byte) 137,(byte) 138,(byte) 139,(byte) 140,(byte) 141,(byte) 142,(byte) 192,(byte) 193,(byte) 194,(byte) 195,(byte) 196,(byte) 197,(byte) 198,(byte) 199,(byte) 200,(byte) 201,(byte) 202,(byte) 203,(byte) 204,(byte) 205,(byte) 206));
        List<Player> p = new ArrayList<>(players);
        for (int i = 0; i < round+1; i++) {
            p.replaceAll(player -> player.addCard(deck.remove((int) (Math.random()*deck.size()))));
        }
        return new WizardModel(List.copyOf(p), trick, round, startingPlayer, deck.remove((int) (Math.random()*deck.size())), totalTricksCalled);
    }
    /**
     * Method which plays a card into the trick, while making sure that no wizard rules are broken. Keep in mind that this only works if the controller makes sure that the player who's turn it is plays
     * @param card the card to be played
     */
    @Override
    public WizardModel playCard(byte card) {
        int currentPlayer = (startingPlayer + trick.size()) % players.size();

        boolean wizardPlayedFirst = (!trick.isEmpty() && trick.stream().filter(c -> valueMask.apply(c) != (byte) 0).findFirst().orElse((byte) 15).equals(wizard));

        byte firstNonFoolCard = trick.stream().filter(c -> valueMask.apply(c) != (byte) 0).findFirst().orElse(card);


        if (trick.size() == players.size()) {
            System.out.println("All players have already played a card this trick.");
            return this;
        }

        // also prevents players whose turn it is not from playing a card, as all cards only occur once and only the current player has the playable cards this trick
        // also allows for only valid cards to be played, as only valid cards are dealt out
        if (!players.get(currentPlayer).hand().contains(card)) {
            System.out.println("Player " + trick.size() + " does not have the card he is trying to play.");
            return this;
        }

        if (!wizardPlayedFirst &&
                valueMask.apply(card) != wizard  &&
                valueMask.apply(card) != fool &&
                firstNonFoolCard != fool &&
                !colorMask.apply(firstNonFoolCard).equals(colorMask.apply(card)) &&
                // Move this to the controller?
                players.get(currentPlayer).hand().stream().anyMatch(c -> colorMask.apply(c).equals(colorMask.apply(firstNonFoolCard))) // this part is necessary, because it is possible that a player does not have a matching color on his hand
        ) {
            System.out.println("Player " + currentPlayer + " must play a card of the same color as the first card played this trick.");
            return this;
        }

        // Set required color and trump flags, if they apply
        List<Player> p = new ArrayList<>(players);
        List<Byte> t = new ArrayList<>(trick);

        p = replaceAtIndex(p,currentPlayer,p.get(currentPlayer).removeCard(card));
        // Do this when evaluating the trick???
        if (valueMask.apply(card) != wizard && valueMask.apply(card) != fool){
            if (colorMask.apply(card).equals(colorMask.apply(firstNonFoolCard))) card |= 0b00010000;
            if (colorMask.apply(card).equals(colorMask.apply(trump))) card |= 0b00100000;
        }
        t.add(card);
        return new WizardModel(List.copyOf(p), List.copyOf(t), round, startingPlayer, trump, totalTricksCalled);
    }
    // Only works during the current trick, can't undo a round
    @Override
    public WizardModel undoPlayCard() {
        if (trick.isEmpty()) {
            System.out.println("No cards have been played yet.");
            return this;
        }
        List<Player> p = new ArrayList<>(players);
        List<Byte> t = new ArrayList<>(trick);
        p = replaceAtIndex(p,trick.size()-1, p.get(trick.size()-1).addCard(t.remove(t.size()-1)));
        return new WizardModel(List.copyOf(p), List.copyOf(t), round, startingPlayer, trump, totalTricksCalled);
    }
    @Override
    public WizardModel endTrick() {
        if (!isTrickOver()) {
            System.out.println("Not all players have played a card yet.");
            return this;
        }
        List<Player> p = new ArrayList<>(players);
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
        // Testing purposes
        System.out.println("Player " + winner + " won the trick.");

        p = replaceAtIndex(p,winner,p.get(winner).setTricksWon(p.get(winner).tricksWon()+1));
        // clears the trick by creating a new empty list
        return new WizardModel(List.copyOf(p), List.of(), round, winner, (byte) 0, totalTricksCalled);
    }
    @Override
    public WizardModel endRound() {
        if (!isRoundOver()) {
            System.out.println("Not all players have played all their cards yet, or the trick still needs to be ended.");
            return this;
        }
        List<Player> p = new ArrayList<>(players);
        p.replaceAll(player -> player
                .addToScore(player.tricksCalled()-player.tricksWon() == 0 ? 20+player.tricksWon()*10 : -Math.abs(player.tricksCalled()-player.tricksWon())*10)
                .setTricksCalled(0)
                .setTricksWon(0));
        return new WizardModel(List.copyOf(p), List.of(), round+1, round+1, (byte) 0, totalTricksCalled);
    }
    @Override
    public WizardModel addPlayer() {
        Player player = new Player();
        List<Player> p = new ArrayList<>(players);
        p.add(player);
        return new WizardModel(List.copyOf(p), trick, round, startingPlayer, trump, totalTricksCalled);
    }
    @Override
    public boolean isGameOver() {
        return round == (60/players.size())+1;
    }
    @Override
    public boolean isTrickOver() {
        return trick.size() == players.size();
    }
    @Override
    public boolean isRoundOver() {
        return players.stream().allMatch(player -> player.hand().isEmpty()) && trick.isEmpty();
    }
    @Override
    public WizardModel setTricksCalled(int tricksCalled) {
        int playerIndex = getCurrentPlayerNum();
        if (tricksCalled < 0 || playerIndex < 0 || playerIndex >= players.size()) {
            System.out.println("Can't call a negative amount of tricks.");
            return this;
        }
        if (playerIndex == (startingPlayer + getCurrentPlayerNum()) % players.size()
                && totalTricksCalled+tricksCalled == round) {
            System.out.println("Total amount of tricks can't be equal to the amount of tricks played this round.");
            return this;
        }
        List<Player> p = new ArrayList<>(players);
        p = replaceAtIndex(p,playerIndex, p.get(playerIndex).setTricksCalled(tricksCalled));
        return new WizardModel(List.copyOf(p), trick, round, startingPlayer, trump, totalTricksCalled + tricksCalled);
    }
    @Override
    public List<Player> getPlayers() {
        return players;
    }
    @Override
    public List<Byte> getTrick() {
        return trick;
    }
    @Override
    public int getCurrentPlayerNum() {
        return (trick.size()+startingPlayer)%players.size();
    }
    @Override
    public byte getTrump() {
        return trump;
    }
    @Override
    public int getRound() {
        return round;
    }
    // function to replace an element in a list at a given index, only way to keep the list immutable
    private <T> List<T> replaceAtIndex(List<T> list, int index, T element) {
        List<T> l = new ArrayList<>(list);
        l.set(index, element);
        return List.copyOf(l);
    }
    private String cardToString(byte card) {
        return valueMask.apply(card)%15 + " " + switch (colorMask.apply(card)) {
            case (byte) 0b00000000 -> "Red";
            case (byte) 0b01000000 -> "Green";
            case (byte) 0b10000000 -> "Blue";
            case (byte) 0b11000000 -> "Yellow";
            default -> "";
        };
    }

    // for testing purposes
    public Player getPlayer(int index) {
        return players.get(index);
    }

    // TODO: fix toString methods here and in Player, Cards change VALUE due to value flags being set
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Round: ").append(round).append("\n")
                .append("Trump card: ").append(cardToString(trump)).append("\n")
                .append("Cards in trick: ").append("\n");
        trick.forEach(c -> result.append(cardToString(c)).append("\n"));
        result.append("\n")
                .append("Players hands: ").append("\n");
        players.forEach(player -> result.append(player.toString().formatted(players.indexOf(player))).append("\n"));
        return result.toString();
    }
}
