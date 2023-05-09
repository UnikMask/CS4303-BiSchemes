package bischemes.level.parts.behaviour;

import bischemes.level.Room;
import bischemes.level.parts.RObject;

public class BStateSwitchStates extends BState {


    private final int[] linkedIDs;
    private final RObject[] linkedObjs;

    private BStateSwitchStates(RObject switcher, int[] linkedIDs) {
        super(switcher);
        this.linkedIDs = linkedIDs;
        linkedObjs = new RObject[linkedIDs.length];
        switcher.addOnStateChange(this);
    }

    public static BStateSwitchStates assign(RObject switcher, int[] linkedIDs) {
        return new BStateSwitchStates(switcher, linkedIDs);
    }

    private void initLinkedObjs() {
        if (linkedObjs[0] != null) return;
        for (int i = 0; i < linkedIDs.length; i++) {
            int id = linkedIDs[i];
            for (RObject rO : room.getObjects()) {
                if (rO.getId() != id) continue;
                linkedObjs[i] = rO;
                break;
            }
            if (linkedObjs[i] == null)
                throw new RuntimeException("initLinkedObjs() error for RObject(id = " + baseObj.getId() + "), " +
                        "of Room(id = " + room.getId() + "), of Level(id = " + room.getLevel().getId() + "). " +
                        "Cannot link lever to ID " + id + ", no RObject with the ID can be found.");
        }
    }

    @Override
    public void run() {
        initLinkedObjs();
        for (RObject rO : linkedObjs)
            rO.switchState();
    }

    @Override
    public void setColour(int colour) {
    }

}
