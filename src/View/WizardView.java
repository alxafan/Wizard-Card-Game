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
                .setPosition(20, 350)
                .setSize(40, 18)
                .setText("0")
                .hide();


        enterTricksCalled = cp5.addButton("Enter");
        enterTricksCalled.setPosition(70, 350)
                .setSize(25, 18)
                .hide();

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
    public void drawStartScreen(int assignedPlayerNum) {
        // Screen displaying that players may join the game
        background(0);
        textSize(40);
        text("You are Player " + (assignedPlayerNum+1), 310,260);
    }

    @Override
    public void drawCallingTricksScreen(List<Player> players, int round, byte trump, int currentPlayerNum, int assignedPlayerNum) {
        tricksCallField.show();
        enterTricksCalled.show();
        background(0);
        textSize(16);

        text("Round: " + round, 10, 20);
        text("Trump card: ",10,40);
        drawCards(trump, 10,50);

        for(int i = 0; i < players.get(assignedPlayerNum).hand().size(); i++) {
            byte card = players.get(assignedPlayerNum).hand().get(i);
            drawCards(card,20+i*65,400);
        }

        text(message, 150, 370);

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (!player.hasCalledTrick()) fill(200,30,20);
            text("Player " + (i+1) + " score: " + player.score()
                            + "   called-tricks: " + player.tricksCalled()
                    , 550,50+20*i);
            noFill();
        }
    }

    @Override
    public void drawPlayingScreen(List<Player> players, List<Byte> trick, byte trump, int round, int currentPlayerNum, int assignedPlayerNum) {
        tricksCallField.hide();
        enterTricksCalled.hide();
        background(0);
        textSize(16);
        // used for selectedCard calculation
        cardsInHand = players.get(assignedPlayerNum).hand().size();

        for (int i = 0; i < players.size(); i++) {
            text("Player " + (i+1) + " score: " + players.get(i).score()
                            + "   called-tricks: " + players.get(i).tricksCalled()
                            + "   won-tricks: " + players.get(i).tricksWon()
                    , 550,50+20*i);
        }

        text("Round: " + round, 10, 20);
        text("Trump card: ",10,40);
        drawCards(trump, 10,50);

        for(int i = 0; i < trick.size(); i++) {
            byte card = trick.get(i);
            drawCards(card,350+i*40,230);
        }

        if (cardsInHand <= 0) return;
        fill(255);
        rect(20+selectedCardIndex*70-5,400-5,70,100);
        noFill();
        for(int i = 0; i < players.get(assignedPlayerNum).hand().size(); i++) {
            byte card = players.get(assignedPlayerNum).hand().get(i);
            drawCards(card,20+i*70,400);
        }
        text(message, 150, 370);
    }
    @Override
    public void drawEndScreen(List<Integer> currentGameWinner, List<Player> players) {
        background(255);
        StringBuilder result = new StringBuilder();
        result.append("Players: ");
        currentGameWinner.forEach(w -> result.append(w).append(", "));
        result.append("won with the score: " + players.get(currentGameWinner.get(0)).score());
    }

    //TODO: add position
    private void drawCards(byte card, float x, float y) {
        switch (colorMask.apply(card)) {
            case (byte) 0b00000000 -> tint(255,0,0);
            case (byte) 0b01000000 -> tint(0,255,0);
            case (byte) 0b10000000 -> tint(10,100,200);
            case (byte) 0b11000000 -> tint(255,255,0);
            default -> tint(255);
        }
        image(cardImages[valueMask.apply(card)],x,y);
        noTint();
    }

    @Override
    public void displayText(String text) {
        message = text;
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

    // bit-masks to decode the cards to be drawn
    private final UnaryOperator<Byte> valueMask = n -> (byte) (n & 0b00001111);
    private final UnaryOperator<Byte> colorMask = n -> (byte) (n & 0b11000000);

}
