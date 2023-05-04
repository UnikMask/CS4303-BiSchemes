package bischemes.level.parts.behaviour;

import bischemes.engine.VisualAttribute;
import bischemes.level.parts.RObject;

public class OnStateChangeFlip implements OnStateChange {

    private final VisualAttribute leverVAttr;
    private final RObject flipable;

    private OnStateChangeFlip(RObject flipable) {
        this.flipable = flipable;
        leverVAttr = flipable.getVisualAttribute(0);
    }

    public static OnStateChangeFlip assign(RObject flipable, boolean initState) {
        OnStateChangeFlip o = new OnStateChangeFlip(flipable);
        if (initState) o.leverVAttr.mirrorVerticesV();
        flipable.addOnStateChange(o);
        return o;
    }

    @Override
    public void run() {
        leverVAttr.mirrorVerticesV();
    }

    @Override
    public void setColour(int colour) {}

}
