package View;

import Controller.IWizardController;
import controlP5.*;
import processing.core.PApplet;
import processing.core.PImage;
import processing.event.KeyEvent;
import Model.Player;

import java.util.List;
import java.util.function.UnaryOperator;

import static controlP5.ControlP5Constants.ACTION_RELEASE;

public class WizardView extends PApplet implements IWizardView {
    private IWizardController controller;
    private int selectedCardIndex = 0;
    private int cardsInHand = 0;
    private String message = "";
    private final PImage[] cardImages = new PImage[15];

    private ControlP5 cp5;
    private Textfield tricksCallField;
    private Button enterTricksCalled;

    public void setController(IWizardController controller) {
        this.controller = controller;
    }

    @Override
    public void settings() {
        setSize(900, 600);
    }

    @Override
    public void setup() {
        for (int i = 0; i < cardImages.length; i++) cardImages[i] = loadImage("Card_%d.png".formatted(i));

        cp5 = new ControlP5(this);

        tricksCallField = cp5.addTextfield("Amount");
        tricksCallField.setLabel("")
                .setPosition(150, 30)
                .setSize(40, 18)
                .setText("0");


        enterTricksCalled = cp5.addButton("Enter");
        enterTricksCalled.setPosition(200, 30)
                .setSize(25, 18);

        enterTricksCalled.addListenerFor(ACTION_RELEASE, c -> {
            try {
                controller.setTrickAmount(Integer.parseUnsignedInt(tricksCallField.getText()));
                tricksCallField.setText("0");
            } catch (NumberFormatException e){
                displayText("Input Number is invalid, check for spaces in your input and don't leave the input empty");
            }
        }
        );
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
        textSize(16);
        StringBuilder result = new StringBuilder();
        result.append("Round: ").append(round).append("\n");
        result.append("Trump card: ").append(cardToString(trump)).append("\n");
        text(result.toString(), 10, 20);
        text(players.get(currentPlayerNum).toString().formatted(currentPlayerNum), 300, 20);
        for(int i = 0; i < players.get(assignedPlayerNum).hand().size(); i++) {
            image(cardImages[i],i*10,i*10);
        }
        text(message, 150, 500);

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

        text(players.get(currentPlayerNum).toString().formatted(currentPlayerNum), 300, 20);
        text("Selected card: " + cardToString(players.get(currentPlayerNum).hand().get(selectedCardIndex)), 10, 550);
        text(message, 150, 500);
    }

    @Override
    public void displayText(String text) {
        message = text;
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
        // TODO: Maybe add a method that increases the selected card index by 1, so that when an input fails, the index doesn't change
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
