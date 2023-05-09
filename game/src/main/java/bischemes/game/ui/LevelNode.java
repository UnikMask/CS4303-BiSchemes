package bischemes.game.ui;

import bischemes.level.Level;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

/** A class used to represent the individual Levels in a LevelMap/MapSlice and is used to draw them through PGraphics */
public final class LevelNode {

    private static final float SELECT_SIZE_FACTOR = 1.3f; // How much bigger a selected node appears
    private static final int   SELECT_SCALE_TIME  = 15; // How many draw() frames required to scale to full size
    private static final float SELECT_SCALE_RATE  = (SELECT_SIZE_FACTOR - 1f) / (float) SELECT_SCALE_TIME;
    private static final int   DEFAULT_TEXT_SIZE  = 20; // How large drawn text (level names) should be

    private static final int   RECT_CORNER_ROUND  = 20; // Radius value for corners of drawn nodes
    private static final int   GREY_OUTER_COLOUR  = 0x4F4F4F - 16777216; // Colour used for unavailable levels
    private static final int   GREY_INNER_COLOUR  = 0xB9B9B9 - 16777216; // Colour used for unavailable levels

    private static final boolean GREY_UNAVAILABLE = true;

    /** Level held by this LevelNode */
    public final Level level;

    /** LevelNodes which must be completed before this LevelNode is available (i.e. before the Level can be played) */
    private final LevelNode[] dependencies;
    /** Position, (with x and y in range 0.0 to 1.0), to represent LevelNode's position within a MapSlice's */
    private final PVector slicePosition;
    /** Actual position of LevelNode, calculated from slicePosition and MapSlice dimensions/positioning/scale */
    private final PVector realPosition;

    /** Whether this LevelNode is currently selected (whether the mouse cursor is hovering over LevelNode) */
    private boolean selected = false;

    /** Whether the LevelNode is currently scaled above its regular size */
    private boolean scaling  = false;
    /** Scale at which the LevelNode is currently scaled above its regular size */
    private float currentScale = 0;

    /**
     * Creates a new LevelNode
     * @param level Level which this LevelNode represents
     * @param dependencies Dependencies of this LevelNode (corresponds with level.getPrerequisites())
     * @param slicePosition position of LevelNode in its MapSlice
     */
    public LevelNode(Level level, LevelNode[] dependencies, PVector slicePosition) {
        this.level = level;
        this.dependencies = dependencies;
        this.slicePosition = slicePosition;
        this.realPosition = new PVector();
    }

    public int getId() {
        return level.getId();
    }

    public PVector getSlicePosition() {
        return slicePosition;
    }

    public PVector getRealPosition() {
        return realPosition;
    }

    public boolean isCompleted() {
        return level.isCompleted();
    }

    public void setSelected(boolean v) {
        selected = v;
    }

    public boolean isSelected() {
        return selected;
    }

    /**
     * Determines whether the held Level is available to play (if its prerequisites have been met)
     * @return true if the level has been completed already or if ALL its dependencies have been completed
     */
    public boolean isAvailable() {
        if (isCompleted()) return true;
        boolean available = true;
        for (LevelNode node : dependencies) available &= node.isCompleted();
        return available;
    }

    /**
     * Checks whether a position on a map is within the bounds of this LevelNode. If so, returns true and sets this
     * LevelNode as selected.
     * @param mapPosition The position on a map that is being checked as within this LevelNode's bounds
     * @param squareWidth The width bounds around the LevelNode's position
     * @return true if this LevelNode has been selected
     */
    public boolean trySelect(PVector mapPosition, float squareWidth) {
        if (GREY_UNAVAILABLE && !isAvailable()) return false;
        PVector distance = PVector.sub(mapPosition, realPosition);
        if (distance.x < 0) distance.x *= -1;
        if (distance.y < 0) distance.y *= -1;
        if (selected) distance.div(SELECT_SIZE_FACTOR);
        selected = distance.x < squareWidth && distance.y < squareWidth;
        if (selected) scaling = true;
        return selected;
    }

    /**
     * Calculates realPosition for the LevelNode. This should be called whenever the view of the MapUI is changed.
     * @param offset Added to realPosition. Represents the sum of the camera position and cumulative widths of MapSlices
     * @param xTransScale The width of the MapSlice, how slicePosition.x corresponds to a real position
     * @param yTransScale The height of the MapSlice, how slicePosition.y corresponds to a real position
     * @param squareWidth The width of the square to draw for this LevelNode
     */
    public void calcPosition(PVector offset, int xTransScale, int yTransScale, float squareWidth) {
        realPosition.x = (slicePosition.x * xTransScale) + offset.x;
        realPosition.y = (slicePosition.y * yTransScale) + offset.y;
        realPosition.z = squareWidth; // Storing squareWidth in otherwise unused z-coordinate
    }

    /**
     * Draws the LevelNode, if the node has any scaling then scales the LevelNode accordingly and adjusts currentScale
     * based on whether the LevelNode is currently selected or not
     * @param g PGraphics to draw the LevelNode with
     */
    public void drawNode(PGraphics g) {
        float width = realPosition.z; // Calculate width here in case it is scaled due to scaling
        if (scaling) {
            width *= 1 + (currentScale * SELECT_SCALE_RATE); // Scale width based on currentScale
            // Determine text colour
            if (!GREY_UNAVAILABLE || isAvailable()) g.fill(level.getColourSecondary());
            else g.fill(GREY_OUTER_COLOUR);
            // Determine text size
            g.textSize(1 + (int) (DEFAULT_TEXT_SIZE * currentScale / SELECT_SCALE_TIME));
            // Determine text position relative to LevelNode and draw text
            if (slicePosition.y > 0.5) {
                g.textAlign(PConstants.CENTER, PConstants.TOP);
                g.text(level.getName(), realPosition.x, realPosition.y + (width / 1.9f));
            }
            else {
                g.textAlign(PConstants.CENTER, PConstants.BOTTOM);
                g.text(level.getName(), realPosition.x, realPosition.y - (width / 1.9f));
            }
            // Adjust currentScale
            if (selected) { // If LevelNode is selected, increment currentScale until it reaches SELECT_SCALE_TIME
                if (currentScale < SELECT_SCALE_TIME)
                    currentScale++;
            }
            else scaling = --currentScale > 1; // Otherwise, decrement currentScale until it reaches 0
        }
        // Determine stroke & fill colour of LevelNode based on whether it is available or not
        if (!GREY_UNAVAILABLE || isAvailable()) {
            g.stroke(level.getColourSecondary());
            g.fill(level.getColourPrimary());
        }
        else {
            g.stroke(GREY_OUTER_COLOUR);
            g.fill(GREY_INNER_COLOUR);
        }
        // Draw LevelNode
        g.rect(realPosition.x, realPosition.y, width, width, RECT_CORNER_ROUND);
    }

    /**
     * Draws edges from this LevelNode's dependencies to its realPosition
     * @param g PGraphics to draw the edges with
     */
    public void drawEdges(PGraphics g) {
        PVector dependencyPos;
        for (LevelNode node : dependencies) {
            dependencyPos = node.getRealPosition();
            if (node.isCompleted()) g.stroke(node.level.getColourSecondary());
            else g.stroke(GREY_INNER_COLOUR);
            g.line(dependencyPos.x, dependencyPos.y, realPosition.x, realPosition.y);
        }
    }

    @Override
    public String toString() {
        return "LevelNode{" + "levelId = " + level.getId() +
                ", levelName = " + level.getName() +
                ", colourPri = " + level.getColourPrimary() + '(' + Integer.toHexString(level.getColourPrimary()) + ')' +
                ", colourSec = " + level.getColourSecondary() + '(' + Integer.toHexString(level.getColourSecondary()) + ')' +
                ", slicePos = [" + slicePosition.x + ", " + slicePosition.y + ']' +
                ", realPos = [" + realPosition.x + ", " + realPosition.y + ']' +
                '}';
    }

}