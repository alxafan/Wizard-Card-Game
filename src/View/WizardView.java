package View;

import Controller.IWizardController;
import processing.core.PApplet;
import processing.core.PImage;
import processing.event.KeyEvent;

import java.util.List;

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
    }

    @Override
    public void drawPlayingScreen(List<Byte> cards, int numOfPlayers, int numOfPlayedCards) {
        cardsInHand = cards.size();

    }

    @Override
    public void drawEndScreen() {

    }

    @Override
    public void keyPressed(KeyEvent event) {
        // Used for beginning the game
        if(event.getKey() == ' ') {
            controller.handleInput(-2);
        }
        if(event.getKeyCode() == RIGHT) {
            selectedCardIndex = ++selectedCardIndex % cardsInHand;
        }
        if(event.getKeyCode() == LEFT) {
            selectedCardIndex = --selectedCardIndex % cardsInHand;
        }
        if(event.getKeyCode() == ENTER) {
            controller.handleInput(selectedCardIndex);
        }
        if (event.getKeyCode() == BACKSPACE) {
            controller.handleInput(-1);
        }
    }

}
