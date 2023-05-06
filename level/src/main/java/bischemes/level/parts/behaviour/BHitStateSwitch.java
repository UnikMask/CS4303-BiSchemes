package bischemes.level.parts.behaviour;

import bischemes.engine.GObject;
import bischemes.level.parts.RObject;

public class BHitStateSwitch extends BHit {

    private final RObject switcher;

    private BHitStateSwitch(RObject switcher) {
        this.switcher = switcher;
        switcher.addOnHit(this);
    }

    public static BHitStateSwitch assign(RObject switcher) {
        return new BHitStateSwitch(switcher);
    }

    @Override
    public void run(GObject hit) {
        switcher.switchState();
    }

    @Override
    public void setColour(int colour) {
    }
}
