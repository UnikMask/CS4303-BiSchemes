package bischemes.level.parts.behaviour;

import bischemes.engine.GObject;
import bischemes.engine.physics.Manifold;
import bischemes.level.parts.RObject;

public class BHitKill extends BHit {

    private final RObject killer;

    private BHitKill(RObject killer) {
        this.killer = killer;
        killer.addOnHit(this);
    }

    public static BHitKill assign(RObject killer) {
        return new BHitKill(killer);
    }

    public void setActiveOnState(boolean activeOnState) {
        this.activeOnState = activeOnState;
        this.stateActivity = true;
    }

    @Override
    public void run(GObject hit, Manifold m) {
        if (activeOnState && (stateActivity != killer.getState())) return;
        //TODO
    }

    @Override
    public void setColour(int colour) {
    }
}
