package bischemes.level.parts.events;

import bischemes.engine.EventCallback;
import bischemes.level.parts.RObject;

public class EventInteractable implements EventCallback {

    protected RObject parent;

    //TODO change to an array of RObject once there is a proper implementation
    protected int[] targetIDs;



    @Override
    public void call(Object... o) {
        // something like:
        /*
        for (RObject o : room) {
            for (int i : targetIDs) {
                if (o.getId() == i) o.switchState();
            }
        }
        */
    }
}
