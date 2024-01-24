package Model;

import java.util.List;

public interface IWizardModel {
    void newGame();
    void dealCards();
    void setTricksCalled(int tricksCalled, int playerNum);
    void playCard(byte card);
//    IWizardModel undoPlayCard();
    void endTrick();
    void endRound();
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
    int getCurrentTrickCaller();
    List<Integer> getCurrentGameWinner();
    int getAssignedPlayerNum();
    int round();
    int winner();

}
