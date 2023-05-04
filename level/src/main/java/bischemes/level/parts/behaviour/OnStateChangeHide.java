package bischemes.level.parts.behaviour;

import bischemes.engine.VisualAttribute;
import bischemes.engine.VisualUtils;
import bischemes.level.parts.RObject;
import bischemes.level.util.SpriteLoader;
import processing.core.PVector;

import static java.lang.Math.min;

public class OnStateChangeHide implements OnStateChange {

    private final VisualAttribute doorVAttr;
    private final RObject hideable;
    private VisualAttribute lockSymbol = null;


    private OnStateChangeHide(RObject hideable) {
        this.hideable = hideable;
        doorVAttr = hideable.getVisualAttribute(0);
    }

    public static OnStateChangeHide assign(RObject hideable, boolean initState) {
        OnStateChangeHide d = new OnStateChangeHide(hideable);

        hideable.setState(initState);
        hideable.addOnStateChange(d);

        if (initState) d.openDoor();

        return d;
    }

    public void addLockIcon(PVector maxDimension) {
        PVector dimension = new PVector(
                min(maxDimension.x, 1),
                min(maxDimension.y, 1));
        lockSymbol = VisualUtils.makeRect(dimension, SpriteLoader.getLockSymbol());
        if (!hideable.getState()) hideable.addVisualAttributes(lockSymbol);
    }


    @Override
    public void run() {
        if (hideable.getState()) openDoor();
        else closeDoor();
    }

    private void openDoor() {
        if (lockSymbol != null) hideable.removeVisualAttributes(1, 0);
        else hideable.removeVisualAttributes(0);
        // TODO Alter RigidBody properties of door to make passable
    }

    private void closeDoor() {
        if (lockSymbol != null) hideable.addVisualAttributes(doorVAttr, lockSymbol);
        else hideable.addVisualAttributes(doorVAttr);
        // TODO Alter RigidBody properties of door to make impassable
    }

    @Override
    public void setColour(int colour) {
        if (!hideable.getState()) return;
        doorVAttr.setColour(colour);
        if (lockSymbol != null) lockSymbol.makeTintedTexture(colour);
    }

}
