package bischemes.level.parts.behaviour;

import bischemes.engine.VisualAttribute;
import bischemes.engine.VisualUtils;
import bischemes.level.parts.RObject;
import bischemes.level.util.SpriteLoader;
import processing.core.PVector;

import static java.lang.Math.min;

public class BStateBlock extends BState {

    private VisualAttribute blockSymbol;
    private VisualAttribute lockSymbol;
    private final RObject block;

    private BStateBlock(RObject block, PVector maxDimension) {
        this.block = block;

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
        block.removeVisualAttributes(1);
        if (block.getState()) makeImmovable();
        else makeMovable();
    }

    private void makeMovable() {
        block.addVisualAttributes(blockSymbol);
        // Alter RigidBody properties of block to make movable
    }

    private void makeImmovable() {
        block.addVisualAttributes(lockSymbol);
        // Alter RigidBody properties of block to make immovable
    }

    @Override
    public void setColour(int colour) {
        //TODO WIP
        if (block.getState()) block.removeVisualAttributes(lockSymbol);
        else block.removeVisualAttributes(blockSymbol);

        this.blockSymbol = VisualUtils.makeRect(new PVector(1, 1), colour, SpriteLoader.getBlockSymbol());
        this.lockSymbol = VisualUtils.makeRect(new PVector(1, 1), colour, SpriteLoader.getLockSymbol());
        if (block.getState()) blockSymbol.makeTintedTexture(colour);
        else lockSymbol.makeTintedTexture(colour);
    }

}
