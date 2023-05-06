package bischemes.level.parts.behaviour;

import bischemes.engine.GObject;
import bischemes.engine.VisualAttribute;
import bischemes.engine.VisualUtils;
import bischemes.level.parts.RObject;
import bischemes.level.util.SpriteLoader;
import processing.core.PVector;

public abstract class BInteract extends BUpdate {

    protected final RObject interactable;
    private final boolean useCircleProx;
    private float xRange;
    private float yRange;
    private float radius;

    private static final float INDICATOR_SCALE_RATE = 0.05f;
    protected VisualAttribute indicator = null;
    private boolean showingIndicator = false;
    private float indicatorScale = 0f;

    protected BInteract(RObject interactable, float x, float y) {
        this.interactable = interactable;
        useCircleProx = false;
        xRange = x;
        yRange = y;
        interactable.addOnUpdate(this);
    }

    protected BInteract(RObject interactable, float r) {
        this.interactable = interactable;
        useCircleProx = true;
        radius = r;
        interactable.addOnUpdate(this);
    }

    public void addIndicator(PVector indicatorOffset) {
        indicator = VisualUtils.makeTexturedPolygon(
                new PVector(1, 1), 4, 0, indicatorOffset, SpriteLoader.getInteractSymbol());
    }

    // Checks whether a position is close enough to the interactable to interact
    protected boolean canInteract(PVector position) {
        if (useCircleProx) {
            float distance = position.dist(interactable.getPosition());
            return distance < radius;
        } else {
            PVector distance = position.copy().sub(interactable.getPosition());
            if (distance.x < 0) distance.x *= -1;
            if (distance.y < 0) distance.y *= -1;
            return distance.x < xRange && distance.y < yRange;
        }
    }

    // Checks whether the InputHandler currently holds the InputCommand INTERACT
    private boolean isInteraction() {
        // TODO need to get interaction from room
        return false;
    }

    private void updateIndicator(boolean showIndicator) {
        if (showIndicator) {
            if (!showingIndicator) {
                interactable.addVisualAttributes(indicator);
                indicatorScale = INDICATOR_SCALE_RATE;
                showingIndicator = true;
            } else if (indicatorScale < 1f) {
                indicatorScale += INDICATOR_SCALE_RATE;
                if (indicatorScale > 1f)
                    indicatorScale = 1f;
            }
            indicator.setScaling(indicatorScale);
        } else if (showingIndicator) {
            indicatorScale -= INDICATOR_SCALE_RATE;
            showingIndicator = indicatorScale >= 0f;
            if (showingIndicator)
                indicator.setScaling(indicatorScale);
            else
                interactable.removeVisualAttributes(indicator);
        }
    }

    protected abstract void onInteraction();

    @Override
    public void run() {
        GObject player = null;
        // TODO need a way to actually get the player's GObject
        PVector playerPos = player.getPosition();
        boolean canInteract = canInteract(playerPos);
        if (canInteract && isInteraction()) onInteraction();
        if (indicator != null) updateIndicator(canInteract);
    }

    @Override
    public void setColour(int colour) {
        if (indicator == null || showingIndicator) return;
        indicator.makeTintedTexture(colour);
    }


}
