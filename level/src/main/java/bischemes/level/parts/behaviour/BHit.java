package bischemes.level.parts.behaviour;

import bischemes.engine.GObject;
import bischemes.engine.physics.Manifold;
import bischemes.level.Room;
import bischemes.level.parts.RObject;

public abstract class BHit implements Behaviour {

    protected boolean activeOnState = false;
    protected boolean stateActivity;

    protected final RObject baseObj;
    protected final Room room;

    protected BHit(RObject baseObj) {
        this.baseObj = baseObj;
        if (baseObj != null) this.room = Room.getRoom(baseObj);
        else this.room = null;
    }


    public abstract void run(GObject hit, Manifold m);

    public abstract void setActiveOnState(boolean activeOnState);

}
