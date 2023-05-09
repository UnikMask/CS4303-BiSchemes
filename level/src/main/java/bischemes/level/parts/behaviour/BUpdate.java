package bischemes.level.parts.behaviour;

import bischemes.level.Room;
import bischemes.level.parts.RObject;

public abstract class BUpdate implements Behaviour {

    protected final RObject baseObj;
    protected final Room room;

    protected BUpdate(RObject baseObj)
    {
        this.baseObj = baseObj;
        if (baseObj != null) this.room = Room.getRoom(baseObj);
        else this.room = null;
    }

    public abstract void run();

}
