package bischemes.level.parts.behaviour;

import bischemes.engine.VisualAttribute;
import bischemes.level.Room;
import bischemes.level.parts.RObject;

public class OnStateChangeLever implements OnStateChange {

    private final VisualAttribute leverVAttr;
    private final RObject lever;

    private final int[] linkedIDs;
    private final RObject[] linkedObjs;

    private OnStateChangeLever(RObject lever, int[] linkedIDs) {
        this.lever = lever;
        leverVAttr = lever.getVisualAttribute(0);
        this.linkedIDs = linkedIDs;
        linkedObjs = new RObject[linkedIDs.length];
    }

    public static void newOnStateChange(RObject lever, boolean initState, int[] linkedIDs) {
        OnStateChangeLever l = new OnStateChangeLever(lever, linkedIDs);

        lever.setState(initState);
        lever.setOnStateChange(l);

        if (initState) l.leverVAttr.mirrorVerticesV();
    }

    private void initLinkedObjs() {
        if (linkedObjs[0] != null) return;
        Room room = Room.getRoom(lever);
        for (int i = 0; i < linkedIDs.length; i++) {
            int id = linkedIDs[i];
            for (RObject rO : room.getObjects()) {
                if (rO.getId() != id) continue;
                linkedObjs[i] = rO;
                break;
            }
            if (linkedObjs[i] == null)
                throw new RuntimeException("initLinkedObjs() error for Lever(id = " + lever.getId() + "), " +
                        "of Room(id = " + room.getId() + "), of Level(id = " + room.getLevel().getId() +"). " +
                        "Cannot link lever to ID " + id + ", no RObject with the ID can be found.");
        }
    }

    @Override
    public void run() {
        initLinkedObjs();
        for (RObject rO : linkedObjs)
            rO.switchState();
        leverVAttr.mirrorVerticesV();
    }

    @Override
    public void setColour(int colour) {}

}
