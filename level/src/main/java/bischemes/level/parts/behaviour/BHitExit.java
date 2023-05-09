package bischemes.level.parts.behaviour;

import bischemes.engine.GObject;
import bischemes.engine.physics.Manifold;
import bischemes.level.PlayerAbstract;
import bischemes.level.Room;
import bischemes.level.parts.RObject;

public class BHitExit extends BHit {

    private BHitExit(RObject exit) {
        super(exit);
        exit.addOnHit(this);
    }

    public static BHitExit assign(RObject exit) {
        return new BHitExit(exit);
    }

    @Override
    public void setActiveOnState(boolean activeOnState) {
        this.activeOnState = activeOnState;
        this.stateActivity = true;
    }

    @Override
    public void run(GObject hit, Manifold m) {
        if (activeOnState && (stateActivity != baseObj.getState())) return;
        if (hit instanceof PlayerAbstract)
            room.getLevel().getGame().completeLevel();
    }

    @Override
    public void setColour(int colour) {}

}
