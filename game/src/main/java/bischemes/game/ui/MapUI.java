package bischemes.game.ui;

import bischemes.level.Levels;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.MouseEvent;

import static java.lang.Math.max;
import static java.lang.Math.min;


public class MapUI {

    private final LevelMap map;
    private final PVector cameraPosition = new PVector();
    private float scale = 1f;
    private final PVector cameraAnchor = new PVector();
    private final PVector pressLocation = new PVector();

    private boolean hasSelection = false;
    private boolean moving = false;

    public MapUI() {
        Levels.loadLevels(true);
        map = new LevelMap(Levels.getLevels());
        System.out.println(map);
    }

    private void bindCameraPosition(PApplet a) {
        scale = min(3.0f, max(0.2f, min(
                ((float) a.width / (float) map.width),
                ((float) a.height / (float) map.height))));
        cameraPosition.x = min(0, max(cameraPosition.x, (a.width / scale) - map.width));
        cameraPosition.y = min(0, max(cameraPosition.y, (a.height / scale) - map.height));
    }


    public void draw(PApplet a, PGraphics g) {
        g.noStroke();
        g.rectMode(PGraphics.CENTER);
        if (moving) {
            cameraPosition.x = cameraAnchor.x + (a.mouseX - pressLocation.x) / scale;
            cameraPosition.y = cameraAnchor.y + (a.mouseY - pressLocation.y) / scale;
            bindCameraPosition(a);
        }

        hasSelection = map.hasSelection(cameraPosition, scale, a.mouseX, a.mouseY);

        g.background(255);

        map.draw(g, cameraPosition, scale);
    }
    public void mousePressed(PApplet a) {
        moving = !hasSelection;
        pressLocation.x = a.mouseX;
        pressLocation.y = a.mouseY;
        cameraAnchor.x = cameraPosition.x;
        cameraAnchor.y = cameraPosition.y;
    }

    public void mouseReleased(PApplet a) {
        if (!hasSelection) System.out.println(map);
        if (!moving && hasSelection) System.out.println(map.getSelection().getId());
        moving = false;
    }

    public void mouseWheel(PApplet a, MouseEvent event) {
        if (event.getCount() > 0) scale -= 0.2f;
        else scale += 0.2f;
        bindCameraPosition(a);
    }
}
