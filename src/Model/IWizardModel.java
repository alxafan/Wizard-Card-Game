package Model;

import java.util.List;

public interface IWizardModel {
    IWizardModel newGame();
    IWizardModel dealCards();
    IWizardModel addPlayer();
    IWizardModel setTricksCalled(int tricksCalled, int playerNum);
    IWizardModel playCard(byte card);
//    IWizardModel undoPlayCard();
    IWizardModel endTrick();
    IWizardModel endRound();
    int isLegalTrickCall(int tricksCalled, int playerNum);
    int isLegalMove(byte card);
    boolean isGameOver();
    boolean isTrickOver();
    boolean isRoundOver();
    boolean allPlayersCalledTricks();
    List<Player> players();
    List<Byte> trick();
    byte trump();
    int getCurrentPlayerNum();
    List<Integer> getCurrentGameWinner();
    int round();
    int winner();

}
