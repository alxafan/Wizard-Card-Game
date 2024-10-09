package Wizard.Controller;

import Wizard.Model.IWizardModel;
import Wizard.View.IWizardView;

import java.util.ArrayList;

/**
 * Class to transfer and control inputs from the view and to determine the game's state
 * possible gameStates are START, CALLING_TRICKS, PLAYING_TRICKS and GAME_OVER
 * Use example in view: "controller.cardInput(cardIndex)" to process a user input from the view
 */
public class WizardController implements IWizardController{
    GameState gameState = GameState.START;
    IWizardModel model;
    IWizardView view;

    // Move to model
    //ArrayList<IWizardModel> modelHistory = new ArrayList<>();     vergessen in der Abgabe zu entfernen

    /**
     * Sets the model to which inputs are given
     * @param model model in question
     */
    public void setModel(IWizardModel model) {
        this.model = model;
    }
    /**
     * Sets the view to which commands are given
     * @param view view in question
     */
    public void setView(IWizardView view) {
        this.view = view;
    }

    /**
     * The logic method of the controller. Determines what gameState the model is currently in
     * and tells the view to draw the according screens.
     * <p></p>
     * gets called every frame by the view
     */
    @Override
    public void nextFrame() {
        if (model.hasGameEnded()) gameState = GameState.GAME_OVER;
        switch (gameState) {
            case START:

                // in case the client takes a bit of time to connect
                if (model.getAssignedPlayerNum() == -1) break;
                view.drawStartScreen(model.getAssignedPlayerNum());
                if (!model.players().isEmpty() && !model.players().get(0).hand().isEmpty()) gameState = GameState.CALLING_TRICKS;
                break;
            case CALLING_TRICKS:

                if (model.getCurrentTrickCaller() == model.getAssignedPlayerNum()) view.displayText2("Your turn to call a trick");
                else view.displayText2("");
                if (model.allPlayersCalledTricks()) {
                    gameState = GameState.PLAYING_TRICK;
                    view.displayText("");
                    break;
                }
                view.drawCallingTricksScreen(model.players(), model.round(), model.trump(), model.getCurrentTrickCaller(), model.getAssignedPlayerNum());
                break;
            case PLAYING_TRICK:

                if (model.getCurrentPlayerNum() == model.getAssignedPlayerNum()) view.displayText2("Your turn to play a card");
                else view.displayText2("");
                // needed to fix synchronization for clients, otherwise they get stuck in a wrong gameState
                if (!model.allPlayersCalledTricks()){
                    gameState = GameState.CALLING_TRICKS;
                    view.displayText("");
                }

                if (model.isTrickOver()) {
                    model.endTrick();
                    view.displayText("");
                }
                if (model.isRoundOver()) {
                    model.endRound();
                    gameState = GameState.CALLING_TRICKS;
                    view.displayText("");
                }
                if (model.isGameOver()) {
                    gameState = GameState.GAME_OVER;
                    model.endGame();
                    view.displayText("");
                    break;
                }
                view.drawPlayingScreen(model.players(), model.trick(), model.trump(), model.round(), model.getCurrentPlayerNum(), model.getAssignedPlayerNum());
                break;
            case GAME_OVER:
                view.drawEndScreen(model.getCurrentGameWinner(), model.players().get(model.getCurrentGameWinner().get(0)).score());
                break;
            default:
                break;
        }
    }

    /**
     * Sends the model a card-input, if the chosen card is allowed to be played, otherwise gives feedback to the view on why it is not.
     * @param cardIndex index of a card in the assigned player's hand
     */
    @Override
    public void cardInput(int cardIndex) {
        if (gameState != GameState.PLAYING_TRICK) {
            view.displayText("You can't play a card in this phase of the game");
            return;
        }
        if (model.getAssignedPlayerNum() != model.getCurrentPlayerNum()) {
            view.displayText("Not currently your turn to play a card");
            return;
        }
        byte card = model.players().get(model.getAssignedPlayerNum()).hand().get(cardIndex);
        switch (model.isLegalMove(card)) {
            case 0:
                //modelHistory.add(model);          Vergessen in der Abgabe zu entfernen
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

    /**
     * Sends the model a command, if the gameState allows for it.
     * @param functionNum 0 -> dealCards, 1 -> end Game, 2 -> new Game
     */
    @Override
    public void functionInput(int functionNum) {
        switch (functionNum) {
            case 0:
                if (gameState == GameState.START) model.dealCards();
                break;
            case 1:
                if(!model.players().isEmpty()) model.endGame();
                break;
            case 2:
                if(model.isServer()) {
                    model.newGame();
                    model.dealCards();
                }
                break;
        }
    }

    /**
     * Sends the model a trick-call-input, if the input is allowed to be made, otherwise gives feedback to the view on why it is not.
     * @param amount trick amount predicted by the assignedPlayer
     */
    @Override
    public void setTrickAmount(int amount) {
        switch (model.isLegalTrickCall(amount, model.getAssignedPlayerNum())) {
            case 0:
                model.setTricksCalled(amount, model.getAssignedPlayerNum());
                view.displayText("");
                break;
            case 1:
                view.displayText("All players have called their tricks");
                break;
            case 2:
                view.displayText("Player index out of bounds.");
                break;
            case 3:
                view.displayText("Not currently this players turn to call a trick");
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
