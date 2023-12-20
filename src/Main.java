
import Model.WizardModel;
import Controller.WizardController;
import processing.core.PApplet;
import View.WizardView;

public class Main {
    public static void main(String[] args) {
        var model = new WizardModel();
        var controller = new WizardController();
        var view = new WizardView();

        // Connect M, V and C
        controller.setModel(model);
        controller.setView(view);
        view.setController(controller);

        PApplet.runSketch(new String[]{"Wizard"}, view);
    }
}
