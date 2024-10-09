package Wizard;

import Wizard.Model.ClientServerThread;
import Wizard.Controller.WizardController;
import processing.core.PApplet;
import Wizard.View.WizardView;

public final class Main {
    public static void main(String[] args) {

        String ip = "localhost";
        int SpielerAnzahl = 2;

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
