package bischemes.level.parts.behaviour;

import bischemes.engine.VisualUtils;
import bischemes.level.parts.RObject;
import bischemes.level.util.SpriteLoader;
import processing.core.PVector;

public class BInteractPortal extends BInteract {

    // TODO add orientation

    protected BInteractPortal(RObject interactable, float x, float y) {
        super(interactable, x, y);
    }

    protected BInteractPortal(RObject interactable, float r) {
        super(interactable, r);
    }

    public static BInteractPortal assign(RObject interactable, float x, float y) {
        return new BInteractPortal(interactable, x, y);
    }

    public static BInteractPortal assign(RObject interactable, float r) {
        return new BInteractPortal(interactable, r);
    }

    @Override
    public void addIndicator(PVector indicatorOffset) {
        indicator = VisualUtils.makeTexturedPolygon(
                new PVector(1, 1), 4, 0, indicatorOffset, SpriteLoader.getSwitchSymbol());
    }

    @Override
    public void onInteraction() {
        //TODO
    }


}
