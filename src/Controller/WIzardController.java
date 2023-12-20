package Controller;

import Model.IWizardModel;
import Model.Player;
import View.IWizardView;

public class WIzardController implements IWizardController{
    GameState gameState;
    IWizardModel model;
    IWizardView view;

    public WIzardController(IWizardModel model, IWizardView view) {
        this.model = model;
        this.view = view;
        this.gameState = GameState.START;
    }


    @Override
    public void nextFrame() {

    }

    @Override
    public void handleInput(int cardIndex) {
        switch (gameState) {
            case START:
                if (cardIndex == -2) gameState = GameState.PLAYING;
                break;
            case PLAYING:
                // TODO: model = model.playCard();
                break;
            case GAME_OVER:
                model = model.newGame();
                break;
            default:
                break;
        }
    }
}
