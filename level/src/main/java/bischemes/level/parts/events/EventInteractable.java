package bischemes.level.parts.events;

import bischemes.engine.EventCallback;
import bischemes.level.parts.RObject;

public class EventInteractable implements EventCallback {

    protected RObject parent;

    @Override
    public void call(Object... o) {
        parent.switchState();
    }
}
