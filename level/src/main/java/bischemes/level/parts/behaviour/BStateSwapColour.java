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

    private final int colourPrimary;
    private final int colourSecondary;

    private VisualAttribute switchSymbol = null;

    private PVector iconDimension;

    private BStateSwapColour(RObject swapper) {
        super(swapper);
        swapper.addOnStateChange(this);
        Level l = Room.getRoom(swapper).getLevel();
        colourPrimary = l.getColourPrimary();
        colourSecondary = l.getColourSecondary();
    }

    public static BStateSwapColour assign(RObject swapper) {
        return new BStateSwapColour(swapper);
    }

    public void addSwitchIcon(PVector maxDimension) {
        PVector iconDimension = new PVector(
                min(maxDimension.x, 1),
                min(maxDimension.y, 1));

        baseObj.addVisualAttributes(switchSymbol);
    }

    @Override
    public void run() {
        int oldColour = room.getLevel().getColour(baseObj.getLColour());
        if (baseObj.getLColour() == LColour.PRIMARY) {
            baseObj.setLColour(LColour.SECONDARY);
            baseObj.setColour(colourSecondary);
        } else {
            baseObj.setLColour(LColour.PRIMARY);
            baseObj.setColour(colourPrimary);
        }

        if (switchSymbol != null) {
            baseObj.removeVisualAttributes(switchSymbol);
            switchSymbol = VisualUtils.makeRect(iconDimension , oldColour, SpriteLoader.getSwitchSymbol());
            baseObj.addVisualAttributes(switchSymbol);
        }

    }

    @Override
    public void setColour(int colour) {
    }
}
