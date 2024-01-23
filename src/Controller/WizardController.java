package Controller;

import Model.IWizardModel;
import View.IWizardView;

import java.util.ArrayList;

public class WizardController implements IWizardController{
    GameState gameState;
    IWizardModel model;
    IWizardView view;

    // Move to model
    ArrayList<IWizardModel> modelHistory = new ArrayList<>();
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
                if (!model.players().isEmpty() && !model.players().get(0).hand().isEmpty()) gameState = GameState.CALLING_TRICKS;
                break;
            case CALLING_TRICKS:
                if (model.allPlayersCalledTricks()) gameState = GameState.PLAYING_TRICK;
                view.drawCallingTricksScreen(model.players(), model.round(), model.trump(), model.getCurrentPlayerNum(), model.getAssignedPlayerNum());
                break;
            case PLAYING_TRICK:
                if (model.isTrickOver()) {
                    model.endTrick();
                }
                if (model.isRoundOver()) {
                    model.endRound();
                    gameState = GameState.CALLING_TRICKS;
                }
                if (model.isGameOver()) {
                    gameState = GameState.GAME_OVER;
                }
                if (model.winner() != -1) view.displayText("Player " + model.winner() + " won the trick");
                view.drawPlayingScreen(model.players(), model.trick(), model.trump(), model.round(), model.getCurrentPlayerNum(), model.getAssignedPlayerNum());
                break;
            case GAME_OVER:
                view.drawEndScreen(model.getCurrentGameWinner());
                break;
            default:
                break;
        }
    }
    /*
    if (model.isTrickOver()) {
                    model.endTrick();
                    gameState = GameState.CALLING_TRICKS;
                    view.displayText("");
                    view.displayText("Player " + model.winner() + " won this trick.");
                }
                if (model.isRoundOver()) {
                    model.endRound();
                    if (model.isGameOver()){
                        gameState = GameState.GAME_OVER;
                        view.displayText("");
                    }
                    else {
                        model.dealCards();
                        gameState = GameState.CALLING_TRICKS;
                        view.displayText("");
                        // TODO: Display the scores being updated, maybe store the points before model.endRound()?
                    }
                }
     */

    @Override
    public void cardInput(int cardIndex) {
        if (gameState != GameState.PLAYING_TRICK) return; // use this later with and give feedback (view method) cardIndex >= 0 && assignedPlayerNum == model.getCurrentPlayerNum()
        byte card = model.players().get(model.getAssignedPlayerNum()).hand().get(cardIndex);
        if (model.getAssignedPlayerNum() != model.getCurrentPlayerNum()) {
            view.displayText("Not currently your turn to play a card");
            return;
        }
        switch (model.isLegalMove(card)) {
            case 0:
                modelHistory.add(model);
                model.playCard(card);
                view.displayText("");
                break;
            case 1:
                view.displayText("All players have already played a card this trick.");
                break;
            case 2:
                view.displayText("Player " + model.getCurrentPlayerNum() + " does not have the card he is trying to play.");
                break;
            case 3:
                view.displayText("Player " + model.getCurrentPlayerNum() + " must play a card of the same color as the first card played this trick.");
                break;
            default:
                throw new IllegalStateException("Unexpected error-value: " + model.isLegalMove(card));
        }
    }
    @Override
    public void functionInput(int functionNum) {
        switch (functionNum) {
            case 0: {
                if (gameState == GameState.START) {
                    model.dealCards();
                }
                break;
            }
            case 1:
                if(gameState == GameState.GAME_OVER) {
                    model.newGame();
                }
                break;
            case 2: //TODO: implement a regular way to undo
                if (gameState == GameState.PLAYING_TRICK) {
                    if (model.trick().isEmpty()) gameState = GameState.CALLING_TRICKS;
                    model = modelHistory.remove(modelHistory.size() - 1);
                }
                break;
        }
    }

    @Override
    public void setTrickAmount(int amount) {
        switch (model.isLegalTrickCall(amount, model.getAssignedPlayerNum())) {
            case 0:
                model.setTricksCalled(amount, model.getAssignedPlayerNum());
                break;
            case 1:
                view.displayText("All players have called their tricks");
                break;
            case 2:
                view.displayText("Not currently this players turn to call a trick");
                break;
            case 3:
                view.displayText("Player index out of bounds.");
                break;
            case 4:
                view.displayText("Can't call a negative amount of tricks or more tricks than there are in the round.");
                break;
            case 5:
                view.displayText("Total amount of tricks can't be equal to the round number.");
                break;
            default:
                throw new IllegalStateException("Unexpected error-value: " + model.isLegalTrickCall(amount, model.getAssignedPlayerNum()));
        }
    }
}
