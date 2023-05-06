package bischemes.level.parts.behaviour;

import bischemes.engine.VisualAttribute;
import bischemes.level.parts.RObject;

public class BStateFlip extends BState {

    private final VisualAttribute leverVAttr;
    private final RObject flipable;

    private BStateFlip(RObject flipable) {
        this.flipable = flipable;
        leverVAttr = flipable.getVisualAttribute(0);
    }

    public static BStateFlip assign(RObject flipable, boolean initState) {
        BStateFlip o = new BStateFlip(flipable);
        if (initState) o.leverVAttr.mirrorVerticesV();
        flipable.addOnStateChange(o);
        return o;
    }

    @Override
    public void run() {
        leverVAttr.mirrorVerticesV();
    }

    @Override
    public void setColour(int colour) {
    }

}
