package bischemes.level.parts.behaviour;

import bischemes.level.parts.RObject;

public class BInteractStateSwitch extends BInteract {

    protected BInteractStateSwitch(RObject interactable, float x, float y) {
        super(interactable, x, y);
    }

    protected BInteractStateSwitch(RObject interactable, float r) {
        super(interactable, r);
    }

    public static BInteractStateSwitch assign(RObject interactable, float x, float y) {
        return new BInteractStateSwitch(interactable, x, y);
    }

    public static BInteractStateSwitch assign(RObject interactable, float r) {
        return new BInteractStateSwitch(interactable, r);
    }

    @Override
    public void onInteraction() {
        baseObj.switchState();
    }


}
