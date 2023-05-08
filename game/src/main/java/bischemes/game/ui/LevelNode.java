package bischemes.game.ui;


import bischemes.level.Level;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PVector;

import static processing.core.PConstants.RECT;

public final class LevelNode {

    private static final float SELECT_SIZE_FACTOR = 1.3f; // How much bigger a selected node appears
    private static final int   SELECT_SCALE_TIME  = 15; // How many draw() frames required to scale to full size
    private static final float SELECT_SCALE_RATE  = (SELECT_SIZE_FACTOR - 1f) / (float) SELECT_SCALE_TIME;
    private static final int   DEFAULT_TEXT_SIZE  = 20; // How large drawn text (level names) should be

    private static final int   RECT_CORNER_ROUND  = 20; // Radius value for corners of drawn nodes
    private static final int   GREY_OUTER_COLOUR  = 0x4F4F4F; // Colour used for unavailable levels
    private static final int   GREY_INNER_COLOUR  = 0xB9B9B9; // Colour used for unavailable levels


    public final Level level;

    private final LevelNode[] dependencies;
    private final PVector slicePosition;
    private final PVector realPosition;

    private PShape rect;

    private boolean completed = false;
    private boolean selected = false;

    private boolean scaling  = false;
    private float currentScale = 0;

    public LevelNode(Level level, LevelNode[] dependencies, PVector position) {
        this.level = level;
        this.dependencies = dependencies;
        this.slicePosition = position;
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

    public void setCompleted(boolean v) {
        completed = v;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setSelected(boolean v) {
        selected = v;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean isAvailable() {
        if (completed) return true;
        boolean available = true;
        for (LevelNode node : dependencies) available &= node.isCompleted();
        return available;
    }

    public boolean trySelect(PVector mapPosition, float squareWidth) {
        PVector distance = PVector.sub(mapPosition, realPosition);
        if (distance.x < 0) distance.x *= -1;
        if (distance.y < 0) distance.y *= -1;
        if (selected) distance.div(SELECT_SIZE_FACTOR);
        selected = distance.x < squareWidth && distance.y < squareWidth;
        if (selected) scaling = true;
        return selected;
    }


    public void calcPosition(PVector offset, int xTransScale, int yTransScale, float squareWidth) {
        realPosition.x = (slicePosition.x * xTransScale) + offset.x;
        realPosition.y = (slicePosition.y * yTransScale) + offset.y;
        realPosition.z = squareWidth;
    }

    public void drawNode(PGraphics g) {
        float width = realPosition.z;
        if (scaling) {
            float scale = 1 + (currentScale * SELECT_SCALE_RATE);
            width *= scale;

            if (isAvailable()) g.fill(level.getColourSecondary());
            else g.fill(GREY_OUTER_COLOUR);

            g.textSize(DEFAULT_TEXT_SIZE * scale);
            g.text(level.getName(), realPosition.x, realPosition.y - (width / 1.5f));

            if (selected) {
                if (currentScale < SELECT_SCALE_TIME)
                    currentScale++;
            }
            else scaling = --currentScale > 1;
        }

        if (isAvailable()) {
            g.stroke(level.getColourSecondary());
            g.fill(level.getColourPrimary());
        }
        else {
            g.stroke(GREY_OUTER_COLOUR);
            g.fill(GREY_INNER_COLOUR);
        }

        g.rect(realPosition.x, realPosition.y, width, width, RECT_CORNER_ROUND);
    }

    public void drawEdges(PGraphics g) {
        PVector dependencyPos;
        for (LevelNode node : dependencies) {
            dependencyPos = node.getRealPosition();
            if (node.isCompleted()) g.stroke(node.level.getColourPrimary());
            else g.stroke(GREY_INNER_COLOUR);
            g.line(dependencyPos.x, dependencyPos.y, realPosition.x, realPosition.y);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("LevelNode{");
        sb.append("levelId = ").append(level.getId());
        sb.append(", levelName = ").append(level.getName());
        sb.append(", colourPri = ").append(level.getColourPrimary()).append('(').append(Integer.toHexString(level.getColourPrimary())).append(')');
        sb.append(", colourSec = ").append(level.getColourSecondary()).append('(').append(Integer.toHexString(level.getColourSecondary())).append(')');;
        sb.append(", slicePos = [").append(slicePosition.x).append(", ").append(slicePosition.y).append(']');
        sb.append(", realPos = [").append(realPosition.x).append(", ").append(realPosition.y).append(']');
        sb.append('}');
        return sb.toString();
    }

}