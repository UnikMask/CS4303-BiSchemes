package bischemes.level.parts.behaviour;

import bischemes.engine.GObject;
import bischemes.level.parts.RObject;

public class BHitStateSwitch extends BHit {

    private final RObject switcher;
    private boolean activeOnState = false;
    private boolean stateActivity;

    private BHitStateSwitch(RObject switcher) {
        this.switcher = switcher;
        switcher.addOnHit(this);
    }

    public static BHitStateSwitch assign(RObject switcher) {
        return new BHitStateSwitch(switcher);
    }

    public void setActiveOnState(boolean activeOnState) {
        this.activeOnState = activeOnState;
        this.stateActivity = true;
    }

    @Override
    public void run(GObject hit) {
        if (activeOnState && (stateActivity != switcher.getState())) return;
        switcher.switchState();
    }

    @Override
    public void setColour(int colour) {
    }
}
