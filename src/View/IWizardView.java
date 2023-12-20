package View;

import java.util.List;

public interface IWizardView {
    void drawStartScreen();
    /**
     * draws the playing screen
     * @param cards the cards that the player has in his hand
     * @param numOfPlayers amount of players in the game
     * @param numOfPlayedCards the number of cards that have been played in the current round, needed to draw the right amount of cards
     */
    void drawPlayingScreen(List<Byte> cards, int numOfPlayers, int numOfPlayedCards);
    void drawEndScreen();
}
