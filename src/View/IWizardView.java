package View;

import Model.Player;
import java.util.List;

public interface IWizardView {
    void drawStartScreen();
    void drawCallingTricksScreen(List<Player> players, int round, byte trump, int currentPlayerNum, int assignedPlayerNum);
    void drawPlayingScreen(List<Player> players, List<Byte> trick, byte trump, int round, int currentPlayerNum, int assignedPlayerNum);
    void displayText(String text);
    void drawEndScreen();
}
