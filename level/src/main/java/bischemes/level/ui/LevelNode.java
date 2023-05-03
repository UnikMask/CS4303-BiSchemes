package bischemes.level.ui;


import processing.core.PGraphics;
import processing.core.PVector;
import bischemes.level.Level;

public final class LevelNode {

    private static final float SELECT_SIZE_FACTOR = 1.5f;

    public final Level level;

    private final LevelNode[] dependencies;
    private final PVector slicePosition;
    private final PVector realPosition;

    private boolean completed = false;
    private boolean selected = false;

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
        return selected;
    }


    public void calcPosition(PVector offset, int xTransScale, int yTransScale, float squareWidth) {
        realPosition.x = (slicePosition.x * xTransScale) + offset.x;
        realPosition.y = (slicePosition.y * yTransScale) + offset.y;
        realPosition.z = squareWidth;
        if (selected) realPosition.z *= SELECT_SIZE_FACTOR;
    }

    public void drawNode(PGraphics g) {
        g.stroke(level.getColourSecondary());
        g.fill(level.getColourPrimary());
        g.rect(realPosition.x, realPosition.y, realPosition.z, realPosition.z);
    }

    public void drawEdges(PGraphics g) {
        PVector dependencyPos;
        for (LevelNode node : dependencies) {
            dependencyPos = node.getRealPosition();
            g.line(dependencyPos.x, dependencyPos.y, realPosition.x, realPosition.y);
        }
    }

}