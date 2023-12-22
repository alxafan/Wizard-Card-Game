package Controller;

public interface IWizardController {
    void nextFrame();
    // separate method for handling input (cards vs actions)
    void handleInput(int cardIndex);
}
