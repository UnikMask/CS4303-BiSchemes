package bischemes.level.parts.behaviour;

import bischemes.engine.GObject;
import bischemes.engine.VisualAttribute;
import bischemes.engine.VisualUtils;
import bischemes.level.parts.RObject;
import bischemes.level.util.SpriteLoader;

import bischemes.game.InputHandler;

import processing.core.PVector;

public class OnUpdateInteractable implements OnUpdate {

    private final RObject interactable;
    private final boolean useCircleProx;
    private float xRange;
    private float yRange;
    private float radius;

    private boolean isInteracting = false;


    private static final float INDICATOR_SCALE_RATE = 0.05f;
    private VisualAttribute indicator = null;
    private boolean showingIndicator = false;
    private float indicatorScale = 0f;

    private OnUpdateInteractable(RObject interactable, float x, float y) {
        this.interactable = interactable;
        useCircleProx = false;
        xRange = x;
        yRange = y;
        interactable.setOnUpdate(this);
    }

    private OnUpdateInteractable(RObject interactable, float r) {
        this.interactable = interactable;
        useCircleProx = true;
        radius = r;
        interactable.setOnUpdate(this);
    }

    public static void newOnUpdate(RObject interactable, float x, float y) {
        new OnUpdateInteractable(interactable, x, y);
    }

    public static void newOnUpdate(RObject interactable, float r) {
        new OnUpdateInteractable(interactable, r);
    }

    public static void newOnUpdate(RObject interactable, float x, float y, PVector indicatorOffset) {
        OnUpdateInteractable u = new OnUpdateInteractable(interactable, x, y);
        u.indicator = VisualUtils.makeTexturedPolygon(
                new PVector(1, 1), 4, 0, indicatorOffset, SpriteLoader.getInteractSymbol());
    }

    public static void newOnUpdate(RObject interactable, float r, PVector indicatorOffset) {
        OnUpdateInteractable u = new OnUpdateInteractable(interactable, r);
        u.indicator = VisualUtils.makeTexturedPolygon(
                new PVector(1, 1), 4, 0, indicatorOffset, SpriteLoader.getInteractSymbol());
    }

    // Checks whether a position is close enough to the interactable to interact
    private boolean canInteract(PVector position) {
        if (useCircleProx) {
            float distance = position.dist(interactable.getPosition());
            return distance < radius;
        }
        else {
            PVector distance = position.copy().sub(interactable.getPosition());
            if (distance.x < 0) distance.x *= -1;
            if (distance.y < 0) distance.y *= -1;
            return distance.x < xRange && distance.y < yRange;
        }
    }

    // Checks whether the InputHandler currently holds the InputCommand INTERACT
    private boolean interactHeld() {
        return InputHandler.getInstance().getHeldCommands().contains(InputHandler.InputCommand.INTERACT);

    }

    // Detects once the InputHandler no longer holds the InputCommand INTERACT
    private boolean hasInteracted() {
        boolean interactHeld = interactHeld();
        if (isInteracting) {
             if (interactHeld) return false;
             isInteracting = false;
             return true;
        }
        isInteracting = interactHeld();
        return false;
    }

    private void updateIndicator(boolean showIndicator) {
        if (showIndicator) {
            if (!showingIndicator) {
                interactable.addVisualAttributes(indicator);
                indicatorScale = INDICATOR_SCALE_RATE;
                showingIndicator = true;
            }
            else if (indicatorScale < 1f) {
                indicatorScale += INDICATOR_SCALE_RATE;
                if (indicatorScale > 1f)
                    indicatorScale = 1f;
            }
            indicator.setScaling(indicatorScale);
        }
        else if (showingIndicator){
            indicatorScale -= INDICATOR_SCALE_RATE;
            showingIndicator = indicatorScale >= 0f;
            if (showingIndicator)
                indicator.setScaling(indicatorScale);
            else
                interactable.removeVisualAttributes(indicator);
        }
    }

    @Override
    public void run() {
        GObject player = null;
        // TODO need a way to actually get the player's GObject
        PVector playerPos = player.getPosition();
        boolean canInteract = canInteract(playerPos);
        if (canInteract) {
            if (hasInteracted()) interactable.switchState();
        }
        else isInteracting = false;
        if (indicator != null) updateIndicator(canInteract);
    }

    @Override
    public void setColour(int colour) {
        if (indicator == null) return;
        if (showingIndicator) return;
        indicator.makeTintedTexture(colour);
    }
}
