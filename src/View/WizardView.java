package View;

import Controller.IWizardController;
import processing.core.PApplet;
import processing.event.KeyEvent;
import Model.Player;

import java.util.List;
import java.util.function.UnaryOperator;

public class WizardView extends PApplet implements IWizardView {
    IWizardController controller;
    int selectedCardIndex = 0;
    int cardsInHand = 0;

    public void setController(IWizardController controller) {
        this.controller = controller;
    }

    @Override
    public void settings() {
        setSize(900, 600);
    }

    @Override
    public void setup() {

    }

    @Override
    public void draw() {
        controller.nextFrame();
    }

    @Override
    public void drawStartScreen() {
        // Screen displaying that players may join the game
        background(0);
    }

    @Override
    public void drawCallingTricksScreen(List<Player> players, int round, byte trump, int currentPlayerNum, int assignedPlayerNum) {
        background(0);
        // TODO: Text-field allowing the assigned player to enter an amount of tricks, when it's their turn. Display other people's tricks called
    }

    @Override
    public void drawPlayingScreen(List<Player> players, List<Byte> trick, byte trump, int round, int currentPlayerNum, int assignedPlayerNum) {
        background(0);
        textSize(16);
        cardsInHand = players.get(currentPlayerNum).hand().size();

        // Only show assigned player's hand later

        StringBuilder result = new StringBuilder();
        result.append("Round: ").append(round).append("\n");
        result.append("Trump card: ").append(cardToString(trump)).append("\n");
        result.append("Cards in trick: ").append("\n");
        trick.forEach(c -> result.append(cardToString(c)).append("\n"));
        result.append("\n").append("Players hands: ").append("\n").append("\n");
        players.forEach(player -> result.append(player.toString().formatted(players.indexOf(player))).append("\n"));
        text(result.toString(), 10, 20);
        text(players.get(currentPlayerNum).toString().formatted(currentPlayerNum), 300, 20);
        text("Selected card: " + cardToString(players.get(currentPlayerNum).hand().get(selectedCardIndex)), 10, 550);
    }

    @Override
    public void drawEndScreen() {
        background(255);
    }

    @Override
    public void keyPressed(KeyEvent event) {
        // Use switch here

        if(event.getKeyCode() == RIGHT) {
            selectedCardIndex = ++selectedCardIndex % cardsInHand;
        }
        if(event.getKeyCode() == LEFT) {
            selectedCardIndex = Math.abs(--selectedCardIndex % cardsInHand);
        }
        if(event.getKeyCode() == ENTER) {
            controller.cardInput(selectedCardIndex--);
            selectedCardIndex = Math.max(selectedCardIndex, 0);
        }
        // Used for beginning the game
        if(event.getKey() == ' ') {
            controller.functionInput(0);
        }
        if (event.getKeyCode() == BACKSPACE) {
            controller.functionInput(1);
        }
        if (event.getKey() == 'r') {
            controller.functionInput(2);
        }
    }


    // REMOVE THIS WHEN DONE

    // valueMask modified to ignore flags
    private final UnaryOperator<Byte> valueMask = n -> (byte) (n & 0b00001111);
    private final UnaryOperator<Byte> colorMask = n -> (byte) (n & 0b11000000);
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
