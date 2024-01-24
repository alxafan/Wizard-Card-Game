package Controller;

public interface IWizardController {
    /**
     * the interface used by the view to send user inputs to the controller
     */
    void nextFrame();
    // separate method for handling input (cards vs actions)
    void cardInput(int cardIndex);
    void functionInput(int functionNum);
    void setTrickAmount(int amount);

}
