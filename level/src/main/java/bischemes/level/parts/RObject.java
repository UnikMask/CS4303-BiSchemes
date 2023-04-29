package bischemes.level.parts;

import bischemes.engine.GObject;
import bischemes.level.util.LColour;
import processing.core.PVector;

public class RObject extends GObject {

    // Current plan, 6 extensions.
    // 'State' ideas are a major WIP as of right now, mostly just stemming from the fact that might be able to be used
    // to switch a boolean state for more objects (not just doors) -> even if we didn't use it, the flexibility might help
    // generate some level designs :)
    // DOOR     Takes a position, dimension & initial state (optional)
    //          State determines whether door is open/closed
    // LEVER    Takes a position (dimension is assumed to be (1, 1)) and an array of linked objects (or maybe ids?)
    // BLOCK    Takes a position, dimension & initial state (optional)
    //          State determines whether block is pushable?
    // SPIKE    Takes a position (dimension is assumed to be (1, 1)) & initial state (optional)
    //          State determines whether the spike is dangerous
    // PORTAL   Takes a position, a vertex & initial state (optional)
    //          State determines whether the portal may be passed through
    // EXIT     Takes a position, a vertex & initial state (optional)
    //          State determines whether the exit may be passed through


    public RObject(GObject parent, PVector postion, float orientation, int id) {
        super(parent, postion, orientation);
        this.id = id;
    }

    public RObject(GObject parent, PVector postion, float orientation, int id, LColour colour) {
        super(parent, postion, orientation);
        this.id = id;
        this.colour = colour;
    }

    protected boolean state = false;
    protected int id;

    protected LColour colour = null;


    public boolean getState() {
        return state;
    }
    public void setState(boolean state) {
        this.state = state;
    }
    public void switchState() {
        state = !state;
    }

    public int getId() {
        return id;
    }
}
