package Controller;

import Model.IWizardModel;
import View.IWizardView;

import java.util.ArrayList;
import java.util.Objects;

public class WizardController implements IWizardController{
    GameState gameState;
    IWizardModel model;
    IWizardView view;

    // ?
    ArrayList<IWizardModel> modelHistory = new ArrayList<>();
    int assignedPlayerNum;

    public WizardController(int playerNum) {
        this.gameState = GameState.START;
        this.assignedPlayerNum = playerNum;
    }

    public void setModel(IWizardModel model) {
        this.model = model;
    }

    public void setView(IWizardView view) {
        this.view = view;
    }

    /**
     * Allows the server/ client to add a player to the game
     * @return the index of the added player or -1 if adding a player failed
     */
    public int addPlayer() {
        if (gameState == GameState.START && model.getPlayers().size() <= 6) {
            model = model.addPlayer();
            return model.getPlayers().size();
        }
        return -1;
    }

    @Override
    public void nextFrame() {
        switch (gameState) {
            case START:
                view.drawStartScreen();
                break;
            case CALLING_TRICKS:
                view.drawCallingTricksScreen(model.getPlayers(), model.getRound(), model.getTrump(), model.getCurrentPlayerNum(), assignedPlayerNum);
                break;
            case PLAYING_TRICK:
                if (model.isGameOver()) gameState = GameState.GAME_OVER;
                if (model.isTrickOver()) model = model.endTrick();
                if (model.isRoundOver()) {
                    model = model.endRound();
                    if (model.isGameOver()) gameState = GameState.GAME_OVER;
                    else model = model.dealCards();
                }
                view.drawPlayingScreen(model.getPlayers(), model.getTrick(), model.getTrump(), model.getRound(), model.getCurrentPlayerNum(), assignedPlayerNum);
                break;
            case GAME_OVER:
                view.drawEndScreen();
                break;
            default:
                break;
        }
    }

    @Override
    public void cardInput(int cardIndex) {
        if (Objects.requireNonNull(gameState) == GameState.PLAYING_TRICK) {// use this later with and give feedback (view method) cardIndex >= 0 && assignedPlayerNum == model.getCurrentPlayerNum()
            if (cardIndex >= 0) {
                modelHistory.add(model);
                model = model.playCard(model.getPlayers().get(model.getCurrentPlayerNum()).hand().get(cardIndex));
            }
        }
    }
    @Override
    public void functionInput(int functionNum) {
        switch (functionNum) {
            case 0: {
                if (gameState == GameState.START) {
                    addPlayer();
                    addPlayer();
                    addPlayer();
                    model = model.dealCards();
                    gameState = GameState.PLAYING_TRICK;
                }
                break;
            }
            case 1:
                if(gameState == GameState.GAME_OVER) {
                    model = model.newGame();
                }
                break;
            case 2:
                if (gameState == GameState.PLAYING_TRICK)
                    model = modelHistory.remove(modelHistory.size() - 1);
                break;
        }
    }

    @Override
    public void setTrickAmount(int amount) {
        
    }
}
