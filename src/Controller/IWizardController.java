package Controller;

public interface IWizardController {
    void nextFrame();
    // separate method for handling input (cards vs actions)
    void cardInput(int cardIndex);
    void functionInput(int functionNum);
    void setTrickAmount(int amount);

}
