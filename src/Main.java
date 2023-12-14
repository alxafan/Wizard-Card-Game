import Model.Player;
import Model.WizardModel;
import processing.core.PApplet;
import View.WizardView;

public class Main {
    public static void main(String[] args) {
        PApplet.main(WizardView.class);

        WizardModel w = new WizardModel();
        for (int i = 0; i < 4; i++) w.addPlayer(new Player());
        w.dealCards();
        System.out.println(w);
        w.playCard(w.players.get(0).getHand().get(0));
        System.out.println(w);
        w.playCard(w.players.get(1).getHand().get(0));
        System.out.println(w);
        w.playCard(w.players.get(2).getHand().get(0));
        System.out.println(w);
        w.playCard(w.players.get(3).getHand().get(0));
        System.out.println(w);
        w.endTrick();
        System.out.println(w);
    }
}
