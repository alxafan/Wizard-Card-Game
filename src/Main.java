
import Model.ClientServerThread;
import Controller.WizardController;
import processing.core.PApplet;
import View.WizardView;

public final class Main {
    public static void main(String[] args) {
        var model = ClientServerThread.newAny("localhost", 5555, 3);
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
