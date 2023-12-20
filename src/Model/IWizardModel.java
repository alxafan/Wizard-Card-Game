package Model;

public interface IWizardModel {
    public IWizardModel newGame();
    public IWizardModel dealCards();
    public IWizardModel playCard(byte card);
    public IWizardModel undoPlayCard();
    public IWizardModel endTrick();
    public IWizardModel endRound();
    public IWizardModel setTricksCalled(int playerNumber, int tricksCalled);
    public boolean isGameOver();
    public boolean isTrickOver();
    public boolean isRoundOver();
    public int getCurrentPlayerNum();
    public IWizardModel addPlayer();
}
