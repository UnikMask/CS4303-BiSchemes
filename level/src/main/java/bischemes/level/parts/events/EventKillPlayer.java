package bischemes.level.parts.events;

import bischemes.engine.EventCallback;

public class EventKillPlayer implements EventCallback {

    @Override
    public void call(Object... o) {
        // something like:
        /*
        room.getPlayer().setPosition(room.getSpawnPosition());
        room.resetObjects();
        */
    }
}
