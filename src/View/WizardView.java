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

/**
 * Class to display the game to the user, receive and transfer inputs from the user to the controller.
 * <p></p>
 * Use example in controller: "view.drawStartScreen(0)" to display the start screen
 */
public class WizardView extends PApplet implements IWizardView {
    private IWizardController controller;
    private int selectedCardIndex = 0;
    private int cardsInHand = 1;
    private String message = "";
    private final PImage[] cardImages = new PImage[15];

    private ControlP5 cp5;
    private Textfield tricksCallField;
    private Button enterTricksCalled;

    /**
     * Sets the controller to which inputs are given
     * @param controller model in question
     */
    public void setController(IWizardController controller) {
        this.controller = controller;
    }

    /**
     * Sets the game's window-size
     */
    @Override
    public void settings() {
        setSize(900, 600);
    }

    /**
     * preloads the images used by the game and creates the button and text field used.
     */
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

    /**
     * Gets called a certain amount of times per second and tells the controller to decide what to draw.
     */
    @Override
    public void draw() {
        controller.nextFrame();
    }

    /**
     * Draws the start screen displaying the assigned player number
     * @param assignedPlayerNum the assigned player number
     */
    @Override
    public void drawStartScreen(int assignedPlayerNum) {
        // Screen displaying that players may join the game
        background(0);
        textSize(40);
        text("You are Player " + (assignedPlayerNum+1), 310,260);
    }

    /**
     * Draws the trick calling screen and displays the button and text field
     * @param players player data used to draw cards and score etc.
     * @param round current round
     * @param trump current trump card
     * @param currentPlayerNum current player number
     * @param assignedPlayerNum assigned player number
     */
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
            drawCards(card,20+i*42,400);
        }

        text(message, 150, 370);

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (!player.hasCalledTrick()) fill(200,30,20);
            else fill(255);
            text("Player " + (i+1) + " score: " + player.score()
                            + "   called-tricks: " + player.tricksCalled()
                    , 550,50+20*i);
            noFill();
        }
    }
    /**
     * Draws the playing cards screen and hides the button and text field
     * @param players player data used to draw cards and score etc.
     * @param trick cards in the current trick
     * @param round current round
     * @param trump current trump card
     * @param currentPlayerNum current player number
     * @param assignedPlayerNum assigned player number
     */
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

        for(int i = 0; i < players.get(assignedPlayerNum).hand().size(); i++) {
            if (i == selectedCardIndex) continue;;
            byte card = players.get(assignedPlayerNum).hand().get(i);
            drawCards(card,20+i*42,400);
        }
        fill(255);
        rect(23+selectedCardIndex*42-5,400-2,63,93);
        noFill();
        drawCards(players.get(assignedPlayerNum).hand().get(selectedCardIndex),20+selectedCardIndex*42,400);
        text(message, 150, 370);
    }

    /**
     * Draws the end screen displaying the winner(s) and their score
     *
     * @param currentGameWinner List of the winner(s)
     * @param score winning score
     */
    @Override
    public void drawEndScreen(List<Integer> currentGameWinner, int score) {
        tricksCallField.hide();
        enterTricksCalled.hide();
        background(0);
        StringBuilder result = new StringBuilder();
        result.append("Players: ");
        currentGameWinner.forEach(w -> result.append(w).append(", "));
        result.append("won with the score: ").append(score);
        textSize(40);
        text(result.toString(), 180,260);
    }

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

    /**
     * saves a text message to be displayed on screen
     * @param text text message
     */
    @Override
    public void displayText(String text) {
        message = text;
    }

    /**
     * Records user inputs and transfers them to the controller
     * @param event user input in question, here it is keys on the keyboard
     */
    @Override
    public void keyPressed(KeyEvent event) {
        // Use switch here

        if(event.getKeyCode() == RIGHT) {
            selectedCardIndex = ++selectedCardIndex % cardsInHand;
        }
        if(event.getKeyCode() == LEFT) {
            selectedCardIndex = Math.abs(--selectedCardIndex % cardsInHand);
            //TODO: fix if there is time
            // selectedCardIndex = (selectedCardIndex-1) % cardsInHand < 0? selectedCardIndex-1+cardsInHand:selectedCardIndex-1
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
        /*
        if (event.getKeyCode() == BACKSPACE) {
            controller.functionInput(1);
        }
        if (event.getKey() == 'r') {
            controller.functionInput(2);
        }*/
    }

    // bit-masks to decode the cards to be drawn
    private final UnaryOperator<Byte> valueMask = n -> (byte) (n & 0b00001111);
    private final UnaryOperator<Byte> colorMask = n -> (byte) (n & 0b11000000);

}
