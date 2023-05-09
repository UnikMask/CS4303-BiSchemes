package bischemes.level.parts.behaviour;

import bischemes.engine.VisualAttribute;
import bischemes.engine.VisualUtils;
import bischemes.engine.physics.PhysicsMesh;
import bischemes.level.parts.RObject;
import bischemes.level.util.SpriteLoader;
import processing.core.PVector;

import static java.lang.Math.min;

public class BStateBlock extends BState {

    private VisualAttribute blockSymbol;
    private VisualAttribute lockSymbol;

    private BStateBlock(RObject block, PVector maxDimension) {
        super(block);

        PVector dimension = new PVector(
                min(maxDimension.x, 1),
                min(maxDimension.y, 1));
        blockSymbol = VisualUtils.makeRect(dimension, SpriteLoader.getBlockSymbol());
        lockSymbol = VisualUtils.makeRect(dimension, SpriteLoader.getBlockSymbol());
    }

    public static BStateBlock assign(RObject block, boolean initState, PVector maxDimension) {
        BStateBlock b = new BStateBlock(block, maxDimension);

        block.setState(initState);
        block.addOnStateChange(b);

        if (initState) b.makeImmovable();
        else b.makeMovable();
        return b;
    }

    @Override
    public void run() {
        baseObj.removeVisualAttributes(1);
        if (baseObj.getState()) makeImmovable();
        else makeMovable();
    }

    private void makeMovable() {
        baseObj.addVisualAttributes(blockSymbol);
        // TODO Alter RigidBody properties of block to make movable
        baseObj.getRigidBody().getProperties().isMovable = true;
        baseObj.getRigidBody().getProperties().isMovable = true;
    }

    private void makeImmovable() {
        baseObj.addVisualAttributes(lockSymbol);
        // TODO Alter RigidBody properties of block to make immovable
        baseObj.getRigidBody().getProperties().isMovable = false;
        baseObj.getRigidBody().getProperties().isRotatable = false;
    }

    @Override
    public void setColour(int colour) {
        //TODO WIP
        if (baseObj.getState()) baseObj.removeVisualAttributes(lockSymbol);
        else baseObj.removeVisualAttributes(blockSymbol);

        blockSymbol = VisualUtils.makeRect(new PVector(1, 1), colour, SpriteLoader.getBlockSymbol());
        lockSymbol = VisualUtils.makeRect(new PVector(1, 1), colour, SpriteLoader.getLockSymbol());

        if (baseObj.getState()) baseObj.addVisualAttributes(lockSymbol);
        else baseObj.addVisualAttributes(blockSymbol);;
    }

}
