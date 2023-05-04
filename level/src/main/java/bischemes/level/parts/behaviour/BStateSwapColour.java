package bischemes.level.parts.behaviour;

import bischemes.engine.VisualAttribute;
import bischemes.engine.VisualUtils;
import bischemes.level.Level;
import bischemes.level.Room;
import bischemes.level.parts.RObject;
import bischemes.level.util.LColour;
import bischemes.level.util.SpriteLoader;
import processing.core.PVector;

import static java.lang.Math.min;

public class BStateSwapColour extends BState {

    private final RObject swapper;

    private final int colourPrimary;
    private final int colourSecondary;

    private VisualAttribute switchSymbol = null;

    private BStateSwapColour(RObject swapper) {
        this.swapper = swapper;
        swapper.addOnStateChange(this);
        Level l = Room.getRoom(swapper).getLevel();
        colourPrimary = l.getColourPrimary();
        colourSecondary = l.getColourSecondary();
    }

    public static BStateSwapColour assign(RObject swapper) {
        return new BStateSwapColour(swapper);
    }

    public void addSwitchIcon(PVector maxDimension) {
        PVector dimension = new PVector(
                min(maxDimension.x, 1),
                min(maxDimension.y, 1));
        switchSymbol = VisualUtils.makeRect(dimension, SpriteLoader.getSwitchSymbol());
        swapper.addVisualAttributes(switchSymbol);
    }

    @Override
    public void run() {
        if (swapper.getLColour() == LColour.PRIMARY) {
            swapper.setLColour(LColour.SECONDARY);
            swapper.setColour(colourSecondary);
            if (switchSymbol != null)
                switchSymbol.setColour(colourPrimary);
        }
        else {
            swapper.setLColour(LColour.PRIMARY);
            swapper.setColour(colourPrimary);
            if (switchSymbol != null)
                switchSymbol.setColour(colourSecondary);
        }
    }

    @Override
    public void setColour(int colour) {}
}
