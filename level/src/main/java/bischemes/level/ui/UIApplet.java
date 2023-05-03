package bischemes.level.ui;

import bischemes.level.Level;
import bischemes.level.Levels;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.MouseEvent;

import java.util.HashMap;


public class UIApplet extends PApplet{

    public static final int DEFAULT_WIDTH  = 1600;
    public static final int DEFAULT_HEIGHT = 900;

    private PGraphics g;

    private final LevelMap map;
    private final PVector cameraPosition = new PVector();
    private float scale = 1f;
    private final PVector cameraAnchor = new PVector();
    private final PVector pressLocation = new PVector();

    private boolean hasSelection = false;
    private boolean moving = false;

    public static void main(String[] args) {
        Levels.loadLevels(true);
        UIApplet applet = new UIApplet(Levels.getLevels());

        applet.setup();
        applet.start();
        applet.loop();
    }


    public UIApplet(HashMap<Integer, Level> levels) {
        super();
        map = new LevelMap(levels);
    }

    private void bindCameraPosition() {
        scale = min(3.0f, max(0.2f, min(
                ((float) width / (float) map.width),
                ((float)height / (float) map.height))));
        cameraPosition.x = min(0, max(cameraPosition.x, (width / scale) - map.width));
        cameraPosition.y = min(0, max(cameraPosition.y, (height / scale) - map.height));
    }

    @Override
    public void settings() {
        super.settings();
        size(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    public void setup() {
        super.setup();

        initSurface();
        getSurface().setResizable(true);
        getSurface().setLocation(0, 0);

        g = createPrimaryGraphics();
        g.noStroke();
        g.rectMode(PGraphics.CENTER);
    }

    @Override
    public void draw() {
        super.draw();
        if (moving) {
            cameraPosition.x = cameraAnchor.x + (mouseX - pressLocation.x) / scale;
            cameraPosition.y = cameraAnchor.y + (mouseY - pressLocation.y) / scale;
            bindCameraPosition();
        }

        hasSelection = map.hasSelection(cameraPosition, scale, mouseX, mouseY);

        background(255);
        map.draw(g, cameraPosition, scale);

    }

    @Override
    public void mousePressed() {
        super.mousePressed();
        moving = !hasSelection;
        pressLocation.x = mouseX;
        pressLocation.y = mouseY;
        cameraAnchor.x = cameraPosition.x;
        cameraAnchor.y = cameraPosition.y;
    }

    @Override
    public void mouseReleased() {
        super.mouseReleased();
        if (!moving && hasSelection) println(map.getSelection().getId());
        moving = false;
    }

    @Override
    public void mouseWheel(MouseEvent event) {
        super.mouseWheel(event);
        if (event.getCount() > 0) scale -= 0.2f;
        else scale += 0.2f;
        bindCameraPosition();
    }
}
