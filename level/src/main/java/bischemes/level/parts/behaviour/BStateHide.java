package bischemes.level.parts.behaviour;

import bischemes.engine.VisualAttribute;
import bischemes.engine.VisualUtils;
import bischemes.engine.physics.PhysicsMesh;
import bischemes.level.parts.RObject;
import bischemes.level.util.SpriteLoader;
import processing.core.PVector;

import static java.lang.Math.min;

public class BStateHide extends BState {

    private final VisualAttribute doorVAttr;
    private VisualAttribute lockSymbol = null;
    private PhysicsMesh mesh;

    private PVector symbolDimension;


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
        symbolDimension = new PVector(
                min(Math.abs(maxDimension.x), 1),
                min(Math.abs(maxDimension.y), 1));
        lockSymbol = VisualUtils.makeRect(symbolDimension, SpriteLoader.getLockSymbol());
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
        baseObj.getRigidBody().getProperties().mesh = mesh;
    }

    private void closeDoor() {
        if (lockSymbol != null) baseObj.addVisualAttributes(doorVAttr, lockSymbol);
        else baseObj.addVisualAttributes(doorVAttr);
        // TODO Alter RigidBody properties of door to make impassable
        mesh = baseObj.getRigidBody().getProperties().mesh;
        baseObj.getRigidBody().getProperties().mesh = null;
    }

    @Override
    public void setColour(int colour) {
        if (baseObj.getState()) doorVAttr.setColour(colour);
        if (lockSymbol == null) return;
        if (!baseObj.getState()) baseObj.removeVisualAttributes(lockSymbol);
        lockSymbol = VisualUtils.makeRect(symbolDimension, colour, SpriteLoader.getLockSymbol());
        if (!baseObj.getState()) baseObj.addVisualAttributes(lockSymbol);
    }

}
