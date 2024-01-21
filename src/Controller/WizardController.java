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
        if (gameState == GameState.START && model.players().size() <= 6) {
            model = model.addPlayer();
            return model.players().size();
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
                if (model.allPlayersCalledTricks()) gameState = GameState.PLAYING_TRICK;
                view.drawCallingTricksScreen(model.players(), model.round(), model.trump(), model.getCurrentPlayerNum(), assignedPlayerNum);
                break;
            case PLAYING_TRICK:
                if (model.isGameOver()) gameState = GameState.GAME_OVER;
                if (model.isTrickOver()) {
                    model = model.endTrick();
                    gameState = GameState.CALLING_TRICKS;
                    view.displayText("Player " + model.winner() + " won this trick.");
                }
                if (model.isRoundOver()) {
                    model = model.endRound();
                    if (model.isGameOver()) gameState = GameState.GAME_OVER;
                    else {
                        model = model.dealCards();
                        //TESTING
                        assignedPlayerNum = (model.round()-1)%model.players().size();
                        gameState = GameState.CALLING_TRICKS;
                        // TODO: Display the scores being updated, maybe store the points before model.endRound()?
                    }
                }
                view.drawPlayingScreen(model.players(), model.trick(), model.trump(), model.round(), model.getCurrentPlayerNum(), assignedPlayerNum);
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
        if (gameState != GameState.PLAYING_TRICK) return; // use this later with and give feedback (view method) cardIndex >= 0 && assignedPlayerNum == model.getCurrentPlayerNum()
        byte card = model.players().get(model.getCurrentPlayerNum()).hand().get(cardIndex);

        switch (model.isLegalMove(card)) {
            case 0:
                modelHistory.add(model);
                model = model.playCard(card);
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
                    addPlayer();
                    addPlayer();
                    addPlayer();
                    model = model.dealCards();
                    gameState = GameState.CALLING_TRICKS;
                }
                break;
            }
            case 1:
                if(gameState == GameState.GAME_OVER) {
                    model = model.newGame();
                }
                break;
            case 2:
                if (gameState == GameState.PLAYING_TRICK) {
                    if (model.trick().isEmpty()) gameState = GameState.CALLING_TRICKS;
                    model = modelHistory.remove(modelHistory.size() - 1);
                }
                break;
        }
    }

    @Override
    public void setTrickAmount(int amount) {
        switch (model.isLegalTrickCall(amount, assignedPlayerNum)) {
            case 0:
                model = model.setTricksCalled(amount, assignedPlayerNum);
                assignedPlayerNum++;
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
                throw new IllegalStateException("Unexpected error-value: " + model.isLegalTrickCall(amount, assignedPlayerNum));
        }
    }
}
