
import Model.ClientServerThread;
import Controller.WizardController;
import processing.core.PApplet;
import View.WizardView;

public final class Main {
    public static void main(String[] args) {

        String ip = "localhost";
        int SpielerAnzahl = 3;

        var model = ClientServerThread.newAny(ip, 5555, SpielerAnzahl); //1<PlayerCount<7
        model.start();
        var controller = new WizardController();
        var view = new WizardView();

        // Connect M, V and C
        controller.setModel(model);
        controller.setView(view);
        view.setController(controller);

        PApplet.runSketch(new String[]{"Wizard"}, view);
    }
}
