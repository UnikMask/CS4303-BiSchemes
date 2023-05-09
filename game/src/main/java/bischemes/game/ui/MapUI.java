package bischemes.game.ui;

import bischemes.level.Level;
import bischemes.level.Levels;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.MouseEvent;

import static java.lang.Math.max;
import static java.lang.Math.min;

/** MapUI works with a PApplet and PGraphics to draw and update a LevelMap */
public class MapUI {

    /** LevelMap that this MapUI is using */
    private final LevelMap map;
    /** Current position of the camera which is viewing the LevelMap */
    private final PVector cameraPosition = new PVector();
    /** Current scale of the LevelMap */
    private float scale = 1f;
    /** Used to hold the original camera position when moving the camera view */
    private final PVector cameraAnchor = new PVector();
    /** Used to hold the location of a mouse press */
    private final PVector pressLocation = new PVector();
    /** Whether the mouse/cursor is currently hovering over a LevelNode in the LevelMap */
    private boolean hasSelection = false;
    /** Level which has been clicked/pressed by the user */
    private Level selection = null;
    /** Whether the user is currently moving the camera view of the LevelMap */
    private boolean moving = false;

    /** Creates a new MapUI, calling loadLevels of the Levels class and using them to build a LevelMap */
    public MapUI() {
        Levels.loadLevels(true);
        map = new LevelMap(Levels.getLevels());
        System.out.println(map); // Debug usage
    }

    /**
     * Gets the Level the user has clicked/pressed on in the MapUI
     * @return the selected Level or null if the user has not chosen a Level
     */
    public Level getSelection() {
        return selection;
    }

    /**
     * Clears the user's Level selection, might be useful to run when returning to the MapUI after Level completion
     */
    public void clearSelection() {
        selection = null;
    }

    /**
     * Constrains the camera position based off the size of the screen, current scale, and LevelMap size
     * @param screenWidth width of the screen
     * @param screenHeight height of the screen
     */
    private void bindCameraPosition(int screenWidth, int screenHeight) {
        scale = min(3.0f, max(0.2f, min(
                ((float) screenWidth / (float) map.width),
                ((float) screenHeight / (float) map.height))));
        cameraPosition.x = min(0, max(cameraPosition.x, (screenWidth / scale) - map.width));
        cameraPosition.y = min(0, max(cameraPosition.y, (screenHeight / scale) - map.height));
    }

    /**
     * Updates camera positions (if the user is currently moving the camera view) and draws the LevelMap
     * @param a PApplet needed to get screen dimensions and mouse position
     * @param g PGraphics to draw the LevelMap with
     */
    public void draw(PApplet a, PGraphics g) {
        // If the user is pressing and dragging the mouse/cursor, update camera view
        if (moving) {
            cameraPosition.x = cameraAnchor.x + (a.mouseX - pressLocation.x) / scale;
            cameraPosition.y = cameraAnchor.y + (a.mouseY - pressLocation.y) / scale;
            bindCameraPosition(a.width, a.height);
        }
        // Calculate if cursor/mouse is currently hovering over a LevelNode of LevelMap
        hasSelection = map.hasSelection(cameraPosition, scale, a.mouseX, a.mouseY);
        g.noStroke();
        g.rectMode(PGraphics.CENTER);
        g.textSize(50); // This is needed to prevent LevelNode text scaling up a small text size
        g.background(255);
        map.draw(g, cameraPosition, scale); // Draw LevelMap
    }

    /**
     * Handles a mousePressed, if the mouse isn't selecting a LevelNode, allows for camera view to start moving
     * @param a PApplet to get mouse position
     */
    public void mousePressed(PApplet a) {
        moving = !hasSelection;
        pressLocation.x = a.mouseX;
        pressLocation.y = a.mouseY;
        cameraAnchor.x = cameraPosition.x;
        cameraAnchor.y = cameraPosition.y;
    }

    /**
     * Handles a mouse button being replaced
     */
    public void mouseReleased() {
        //if (!hasSelection) System.out.println(map); // Debug use
        if (!moving && hasSelection) selection = map.getSelection().level;
        if (moving || !hasSelection) selection = null;
        moving = false;
    }

    /**
     * Adjusts scale based on direction of mouse wheel scroll
     * @param a PApplet as bindCameraPosition will need screen dimensions
     * @param event Mousewheel event, whether direction of mouse wheel is positive or negative
     */
    public void mouseWheel(PApplet a, MouseEvent event) {
        if (event.getCount() > 0) scale -= 0.2f;
        else scale += 0.2f;
        bindCameraPosition(a.width, a.height);
    }
}
