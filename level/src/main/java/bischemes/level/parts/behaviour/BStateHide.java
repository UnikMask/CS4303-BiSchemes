package bischemes.level.parts.behaviour;

import bischemes.engine.VisualAttribute;
import bischemes.engine.VisualUtils;
import bischemes.level.parts.RObject;
import bischemes.level.util.SpriteLoader;
import processing.core.PVector;

import static java.lang.Math.min;

public class BStateHide extends BState {

    private final VisualAttribute doorVAttr;
    private VisualAttribute lockSymbol = null;


    private BStateHide(RObject hideable) {
        super(hideable);
        doorVAttr = hideable.getVisualAttribute(0);
    }

    public static BStateHide assign(RObject hideable, boolean initState) {
        BStateHide d = new BStateHide(hideable);

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
        if (!baseObj.getState()) baseObj.addVisualAttributes(lockSymbol);
    }


    @Override
    public void run() {
        if (baseObj.getState()) openDoor();
        else closeDoor();
    }

    private void openDoor() {
        if (lockSymbol != null) baseObj.removeVisualAttributes(1, 0);
        else baseObj.removeVisualAttributes(0);
        // TODO Alter RigidBody properties of door to make passable
    }

    private void closeDoor() {
        if (lockSymbol != null) baseObj.addVisualAttributes(doorVAttr, lockSymbol);
        else baseObj.addVisualAttributes(doorVAttr);
        // TODO Alter RigidBody properties of door to make impassable
    }

    @Override
    public void setColour(int colour) {
        if (!baseObj.getState()) return;
        doorVAttr.setColour(colour);
        if (lockSymbol != null) lockSymbol.makeTintedTexture(colour);
    }

}
