package Model;

public interface IWizardModel {
    public IWizardModel playCard(byte card);
    public IWizardModel undoPlayCard();
    public IWizardModel endTrick();
    public IWizardModel endRound();
    public IWizardModel setTricksCalled(int playerNumber, int tricksCalled);
    public boolean isGameOver();
    public boolean isTrickOver();
    public boolean isRoundOver();
    public Player getPlayer(int index);
    public IWizardModel addPlayer();
}
