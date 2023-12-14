package View;

import processing.core.PApplet;

public class WizardView extends PApplet {

    @Override
    public void settings() {
        setSize(600, 400);
        pixelDensity(2);
    }

    @Override
    public void setup() {
    }

    @Override
    public void draw() {
        background(255);
        strokeWeight(5);
        fill(color(128, 186,  36));
        circle((float) (width/2.0), (float) (height/2.0), 100);
    }
}
