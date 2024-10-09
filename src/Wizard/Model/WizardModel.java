package Wizard.Model;

import java.io.Serializable;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.*;

/**
 * Record handling the storage and processing of game data and logic.
 * <p></p>
 * Any time something is changed about the model, it should be replaced by a new one wherever it is used,
 * as all fields are immutable and a new model is created with moves such as playCard()
 * <p></p>
 * Cards are stored as bytes with the highest 2 bits representing the color, the next ones certain flags
 * and the last 4 the card value
 *<p></p>
 * Use examples in ClientServerThread:
 * <p></p>
 * "model = model.dealCards()" to deal out cards to the players.
 * <p></p>
 * "model.isRoundOver()" to see if the round is over (task from the controller)
 * @param players List of a record, which stores information relevant to each Player, see more under <a href="Player.html">Player</a>.
 * @param trick List of the played cards in the current trick
 * @param round current round
 * @param startingPlayer determines the first player to play a card in the current trick
 * @param trump trump card this round
 * @param totalTricksCalled total amount of Tricks called this round
 * @param trickWinner is always -1, unless a trick winner was determined recently through endTrick
 */
public record WizardModel(List<Player> players, List<Byte> trick, int round, int startingPlayer, byte trump, int totalTricksCalled, int trickWinner) implements Serializable {

    private static final UnaryOperator<Byte> valueMask = n -> (byte) (n & 0b00111111);
    private static final UnaryOperator<Byte> colorMask = n -> (byte) (n & 0b11000000);
    private static final byte wizard = 0b00001110;
    private static final byte fool = 0b00000000;

    public WizardModel() {this(List.of(), List.of(), 1, 0, (byte) 0, 0, -1);}

    // use this to reset a game in the future
    WizardModel newGame() {return new WizardModel();}
    /**
     * Deals out cards to the players and determines a trump card
     */
    public WizardModel dealCards() {
        ArrayList<Byte> deck = new ArrayList<>(List.of((byte) 0,(byte) 1,(byte) 2,(byte) 3,(byte) 4,(byte) 5,(byte) 6,(byte) 7,(byte) 8,(byte) 9,(byte) 10,(byte) 11,(byte) 12,(byte) 13,(byte) 14,(byte) 64,(byte) 65,(byte) 66,(byte) 67,(byte) 68,(byte) 69,(byte) 70,(byte) 71,(byte) 72,(byte) 73,(byte) 74,(byte) 75,(byte) 76,(byte) 77,(byte) 78,(byte) 128,(byte) 129,(byte) 130,(byte) 131,(byte) 132,(byte) 133,(byte) 134,(byte) 135,(byte) 136,(byte) 137,(byte) 138,(byte) 139,(byte) 140,(byte) 141,(byte) 142,(byte) 192,(byte) 193,(byte) 194,(byte) 195,(byte) 196,(byte) 197,(byte) 198,(byte) 199,(byte) 200,(byte) 201,(byte) 202,(byte) 203,(byte) 204,(byte) 205,(byte) 206));
        List<Player> p = new ArrayList<>(players);
        for (int i = 0; i < round; i++) p.replaceAll(player -> player.addCard(deck.remove((int) (Math.random()*deck.size()))));
        return new WizardModel(List.copyOf(p), trick, round, startingPlayer, deck.remove((int) (Math.random()*deck.size())), totalTricksCalled, trickWinner);
    }

    /**
     * Sets an amount of tricks called for a player
     * <p></p>
     * Does not check for legitimacy of the Call unless assertions are enabled
     * @param tricksCalled amount of tricks the player predicts he will win this round
     * @param playerNum player's number
     * @return updated model
     */
    public WizardModel callTricks(int tricksCalled, int playerNum) {
        assert !haveAllPlayersCalledTricks(): "All players have called their tricks";
        assert ((startingPlayer + players.stream().filter(Player::hasCalledTrick).count()) % players.size() == playerNum): "Not currently this players turn to call a trick";
        assert playerNum >= 0 && playerNum < players.size(): "Player index out of bounds.";
        assert tricksCalled >= 0 && tricksCalled <= round: "Can't call a negative amount of tricks or more tricks than there are in the round.";
        assert !(players.stream().filter(Player::hasCalledTrick).count() == players.size()-1 && totalTricksCalled+tricksCalled == round): "Total amount of tricks can't be greater than the amount of tricks in the round.";

        List<Player> p = new ArrayList<>(players);
        p = replaceAtIndex(p,playerNum, p.get(playerNum).setTricksCalled(tricksCalled));
        return new WizardModel(List.copyOf(p), trick, round, startingPlayer, trump, totalTricksCalled + tricksCalled, trickWinner);
    }
    /**
     * Plays a card into the trick
     * <p></p>
     * Does not check for legitimacy of the move, unless assertions are enabled
     * @param card card to be played
     * @return updated model
     */
    public WizardModel playCard(byte card) {
        int currentPlayer = (startingPlayer + trick.size()) % players.size();
        byte firstNonFoolCard = trick.stream().filter(c -> valueMask.apply(c) != fool).findFirst().orElse(card);
        assert isLegalMove(card) == 0: "Can't play this card right now";

        List<Player> p = new ArrayList<>(players);
        List<Byte> t = new ArrayList<>(trick);
        p = replaceAtIndex(p,currentPlayer,p.get(currentPlayer).removeCard(card));
        // Set required color and trump flags, if they apply
        if (valueMask.apply(card) != wizard && valueMask.apply(card) != fool){
            if (colorMask.apply(card).equals(colorMask.apply(firstNonFoolCard))) card |= 0b00010000;
            if (colorMask.apply(card).equals(colorMask.apply(trump))) card |= 0b00100000;
        }
        t.add(card);
        return new WizardModel(List.copyOf(p), List.copyOf(t), round, startingPlayer, trump, totalTricksCalled, trickWinner);
    }

    /**
     * Sets the trump to a color of choice
     * @param color 0 red, 1 green, 2 blue, 3 yellow
     * @return updated model
     */
    public WizardModel callTrump(int color, int playerNum) {
        assert color < 4 && color >= 0;
        return new WizardModel(players, trick, round, startingPlayer, (byte) color, totalTricksCalled, trickWinner);
    }

    /**
     * Ends the current trick and determines a trick-winner, updates won-tricks, starting-player and resets the trick
     * <p></p>
     * Does not check for legitimacy of action, unless assertions are enabled.
     * @return updated model
     */
    public WizardModel endTrick() {
        assert isTrickOver(): "Not all players have played a card yet.";
        List<Player> p = new ArrayList<>(players);
        int winning;
        byte winningValue;

        winningValue = valueMask.apply(trick.stream().filter(n -> (n & wizard) == wizard).findFirst().orElse(trick.stream().map(valueMask).reduce((byte) 0, (a, b) -> (a > b ? a : b))));
        winning = (IntStream.range(0, trick.size()).filter(i -> valueMask.apply(trick.get(i)) == winningValue).findAny().orElse(-1) + startingPlayer) % players.size();

        p = replaceAtIndex(p,winning,p.get(winning).setTricksWon(p.get(winning).tricksWon()+1));
        // clears the trick by creating a new empty list
        return new WizardModel(List.copyOf(p), List.of(), round, winning, trump, totalTricksCalled, winning);
    }

    /**
     * Ends the current round, calculates and updates the player scores, increases the round number,
     * resets the total-tricks-called and determines the startingPlayer
     * <p></p>
     * See the game rules for score calculation
     * <p></p>
     * Does not check for legitimacy of action, unless assertions are enabled.
     * @return updated model
     */
    public WizardModel endRound() {
        assert isRoundOver(): "Not all players have played all their cards yet, or the trick still needs to be ended.";
        List<Player> p = new ArrayList<>(players);
        p.replaceAll(player -> player.addToScore(player.tricksCalled()-player.tricksWon() == 0 ? 20+player.tricksWon()*10 : -Math.abs(player.tricksCalled()-player.tricksWon())*10).resetTricks());
        // clears the trick by creating a new empty list
        return new WizardModel(List.copyOf(p), List.of(), round+1, round%players.size(), (byte) 0, 0, trickWinner);
    }

    /**
     * Adds a new player to the game
     * @return updated model
     */
    public WizardModel addPlayer() {
        List<Player> p = new ArrayList<>(players);
        p.add(new Player());
        return new WizardModel(List.copyOf(p), trick, round, startingPlayer, trump, totalTricksCalled, trickWinner);
    }

    /**
     * method that checks if a trick-call is legal or not.
     * @param tricksCalled amount of tricks predicted by player
     * @param playerNum player's number
     * @return a number besides 0 represents the move being illegal, this can be decoded later on
     */
    int isLegalTrickCall(int tricksCalled, int playerNum) {
        if (haveAllPlayersCalledTricks()) return 1;
        if (playerNum < 0 || playerNum >= players.size()) return 2;
        if (! (((startingPlayer + players.stream().filter(Player::hasCalledTrick).count()) % players.size()) == playerNum)) return 3;
        if (tricksCalled < 0 || tricksCalled > round) return 4;
        if (players.stream().filter(Player::hasCalledTrick).count() == players.size()-1 && totalTricksCalled+tricksCalled == round) return 5;
        return 0;
    }

    /**
     * Method to check whether a given card can be played or not
     * @param card desired card to be checked
     * @return a number besides 0 represents the move being illegal, this can be decoded later on
     */
    int isLegalMove(byte card) {
        int currentPlayer = (startingPlayer + trick.size()) % players.size();
        byte firstNonFoolCard = trick.stream().filter(c -> valueMask.apply(c) != fool).findFirst().orElse(card);

        boolean wizardPlayedFirst = (!trick.isEmpty() && trick.stream().filter(c -> valueMask.apply(c) != (byte) 0).findFirst().orElse((byte) 15).equals(wizard));
        // makes sure that if only fools were played the other checks still work as intended
        if (trick.size() == players.size()) return 1;
        // also prevents players whose turn it is not from playing a card, as all cards only occur once and only the current player has the playable cards this trick
        // also allows for only valid cards to be played, as only valid cards are dealt out
        if (!players.get(currentPlayer).hand().contains(card)) return 2;
        // this part is necessary, because it is possible that a player does not have a matching color on his hand;
        if (!wizardPlayedFirst && (valueMask.apply(card) != wizard) && (valueMask.apply(card) != fool) && (firstNonFoolCard != fool) && (!colorMask.apply(firstNonFoolCard).equals(colorMask.apply(card))) && (players.get(currentPlayer).hand().stream().anyMatch(c -> colorMask.apply(c).equals(colorMask.apply(firstNonFoolCard))))) return 3;

        return 0;
    }

    /**
     * checks if calling a Trump is possible
     * @param color
     * @return
     */
    int isLegalTrumpCall(int color, int playerNum) {
        //TODO implement this
        return 0;
    }

    /**
     * determines the current player to call a trick
     * @return player's number to call a trick
     */
    public int getCurrentTrickCaller() {return (int) (startingPlayer + players.stream().filter(Player::hasCalledTrick).count()) % players.size();}

    /**
     * determines player(s) with the highest score
     * @return List with the numbers of winning players
     */
    public List<Integer> getCurrentGameWinner(){
        int winningScore = players.stream().map(Player::score).reduce(Math::max).orElse(-1);
        return IntStream.range(0, players.size()).filter(i -> players.get(i).score() == winningScore).boxed().collect(Collectors.toList());
    }

    /**
     * determines if the game has reached an end
     * @return true if the game has ended, otherwise false
     */
    public boolean isGameOver() {return round == 60/(players.size()+1);}
    boolean isTrickOver() {return trick.size() == players.size();}
    boolean isRoundOver() {return players.stream().allMatch(player -> player.hand().isEmpty()) && trick.isEmpty();}
    boolean haveAllPlayersCalledTricks() {return players.stream().allMatch(Player::hasCalledTrick);}
    int getCurrentPlayerNum() {return (trick.size()+startingPlayer)%players.size();}

    // function to replace an element in a list at a given index, only way to keep the list immutable
    private <T> List<T> replaceAtIndex(List<T> list, int index, T element) {
        List<T> l = new ArrayList<>(list);
        l.set(index, element);
        return List.copyOf(l);
    }
    String cardToString(byte card) {
        return (card & 0b00001111)%15 + " " + switch (colorMask.apply(card)) {
            case (byte) 0b00000000 -> "Red";
            case (byte) 0b01000000 -> "Green";
            case (byte) 0b10000000 -> "Blue";
            case (byte) 0b11000000 -> "Yellow";
            default -> "";};
    }

    /**
     * packs all data relevant to the current game's state into a String
     * @return String specified above
     */
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Round: ").append(round).append("\n").append("Trump card: ").append(cardToString(trump)).append("\n").append("Cards in trick: ").append("\n");
        trick.forEach(c -> result.append(cardToString(c)).append("\n"));
        result.append("\n").append("Players hands: ").append("\n");
        players.forEach(p -> p.hand().forEach(c -> result.append(cardToString(c)).append("\n")));
        result.append("Player-data: ");
        players.forEach(result::append);
        result.append("\n").append("Current players turn: ").append(getCurrentPlayerNum());
        return result.toString();
    }
}
