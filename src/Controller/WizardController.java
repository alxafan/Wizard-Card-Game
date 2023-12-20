package Controller;

import Model.IWizardModel;
import View.IWizardView;

public class WizardController implements IWizardController{
    GameState gameState;
    IWizardModel model;
    IWizardView view;

    public WizardController() {
        this.gameState = GameState.START;
    }

    public void setModel(IWizardModel model) {
        this.model = model;
    }

    public void setView(IWizardView view) {
        this.view = view;
    }

    @Override
    public void nextFrame() {
        switch (gameState) {
            case START:
                view.drawStartScreen();
                break;
            case PLAYING:
                if (model.isGameOver()) gameState = GameState.GAME_OVER;
                if (model.isTrickOver()) model = model.endTrick();
                if (model.isRoundOver()) {
                    model = model.endRound();
                    if (model.isGameOver()) gameState = GameState.GAME_OVER;
                    else model = model.dealCards();
                }

                break;
            case GAME_OVER:
                view.drawEndScreen();
                break;
            default:
                break;
        }
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
