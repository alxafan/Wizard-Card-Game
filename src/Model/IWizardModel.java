package Model;

import java.util.List;

public interface IWizardModel {
    IWizardModel newGame();
    IWizardModel dealCards();
    IWizardModel addPlayer();
    IWizardModel setTricksCalled(int tricksCalled);
    IWizardModel playCard(byte card);
//    IWizardModel undoPlayCard();
    IWizardModel endTrick();
    IWizardModel endRound();
    boolean isGameOver();
    boolean isTrickOver();
    boolean isRoundOver();
    List<Player> players();
    List<Byte> trick();
    byte trump();
    int getCurrentPlayerNum();
    int round();

}
