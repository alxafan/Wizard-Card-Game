package View;

import Model.Player;
import java.util.List;
/**
 * the interface used by the controller to send information to the view
 */
public interface IWizardView {
    void drawStartScreen(int assignedPlayerNum);
    void drawCallingTricksScreen(List<Player> players, int round, byte trump, int currentPlayerNum, int assignedPlayerNum);
    void drawPlayingScreen(List<Player> players, List<Byte> trick, byte trump, int round, int currentPlayerNum, int assignedPlayerNum);
    void drawEndScreen(List<Integer> currentGameWinner, int score);
    void displayText(String text);
    void displayText2(String text);

}
