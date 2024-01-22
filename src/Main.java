
import Model.WizardModel;
import Controller.WizardController;
import processing.core.PApplet;
import View.WizardView;

public final class Main {
    public static void main(String[] args) {
        var model = new WizardModel();
        // Change this to the player number assigned by the server
        var controller = new WizardController(0);
        var view = new WizardView();

        // Connect M, V and C
        controller.setModel(model);
        controller.setView(view);
        view.setController(controller);

        PApplet.runSketch(new String[]{"Wizard"}, view);
    }
}
