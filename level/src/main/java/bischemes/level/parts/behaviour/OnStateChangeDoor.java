package bischemes.level.parts.behaviour;

import bischemes.engine.VisualAttribute;
import bischemes.engine.VisualUtils;
import bischemes.level.parts.RObject;
import bischemes.level.util.SpriteLoader;
import processing.core.PVector;

import static java.lang.Math.min;

public class OnStateChangeDoor implements OnStateChange {

    private final VisualAttribute doorVAttr;
    private final VisualAttribute lockSymbol;
    private final RObject door;

    private OnStateChangeDoor(RObject door, boolean initState, PVector maxDimension) {
        this.door = door;

        PVector dimension = new PVector(
                min(maxDimension.x, 1),
                min(maxDimension.y, 1));
        lockSymbol = VisualUtils.makeRect(dimension, SpriteLoader.getBlockSymbol());

        doorVAttr = door.getVisualAttribute(0);
    }

    public static void newOnStateChange(RObject door, boolean initState, PVector maxDimension) {
        OnStateChangeDoor d = new OnStateChangeDoor(door, initState, maxDimension);

        door.setState(initState);
        door.setOnStateChange(d);

        if (initState) {
            door.addVisualAttributes(d.lockSymbol);
            d.openDoor();
        }
        else {
            door.removeVisualAttributes(0);
            d.closeDoor();
        }
    }


    @Override
    public void run() {
        if (door.getState()) openDoor();
        else closeDoor();
    }

    private void openDoor() {
        door.removeVisualAttributes(1, 0);
        // Alter RigidBody properties of door to make passable
    }

    private void closeDoor() {
        door.addVisualAttributes(doorVAttr, lockSymbol);
        // Alter RigidBody properties of door to make impassable
    }

    @Override
    public void setColour(int colour) {
        if (!door.getState()) {
            doorVAttr.setColour(colour);
            lockSymbol.makeTintedTexture(colour);
        }
    }

}
