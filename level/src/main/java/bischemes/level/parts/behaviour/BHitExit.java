package bischemes.level.parts.behaviour;

import bischemes.engine.GObject;
import bischemes.engine.physics.Manifold;
import bischemes.level.Room;
import bischemes.level.parts.RObject;

public class BHitExit extends BHit {

    private final RObject exit;

    private BHitExit(RObject exit) {
        this.exit = exit;
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
        if (activeOnState && (stateActivity != exit.getState())) return;
        //TODO check if player is 'hit'
        Room.getRoom(exit).getLevel().setCompleted(true);
        //TODO return to menu
    }

    @Override
    public void setColour(int colour) {}

}
