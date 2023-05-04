package bischemes.level.parts;

import bischemes.engine.GObject;
import bischemes.level.parts.behaviour.BHit;
import bischemes.level.parts.behaviour.BState;
import bischemes.level.parts.behaviour.BUpdate;
import bischemes.level.util.LColour;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;

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


    protected int id;

    protected LColour colour = null;

    protected boolean state = false;
    protected List<BState> bState = null;

    protected List<BUpdate> bUpdate = null;

    protected List<BHit> bHit = null;

    public int getId() {
        return id;
    }


    public boolean getState() {
        return state;
    }
    public void setState(boolean state) {
        if (!state ^ this.state) return;
        switchState();
    }
    public void switchState() {
        state = !state;
        if (bState != null)
            for (BState o : bState)
                o.run();
    }

    @Override
    public void update() {
        if (bUpdate != null)
            for (BUpdate o : bUpdate)
                o.run();
    }

    @Override
    public void onHit(GObject hit) {
        if (bHit != null)
            for (BHit o : bHit)
                o.run(hit);
    }

    public void addOnStateChange(BState bState) {
        if (this.bState == null)
            this.bState = new ArrayList<>();
        this.bState.add(bState);
    }

    public void addOnUpdate(BUpdate bUpdate) {
        if (this.bUpdate == null)
            this.bUpdate = new ArrayList<>();
        this.bUpdate.add(bUpdate);
    }

    public void addOnHit(BHit bHit) {
        if (this.bHit == null)
            this.bHit = new ArrayList<>();
        this.bHit.add(bHit);
    }

    @Override
    public void setColour(int colour) {
        super.setColour(colour);
        if (bState != null)
            for (BState o : bState)
                o.setColour(colour);
        if (bUpdate != null)
            for (BUpdate o : bUpdate)
                o.setColour(colour);
    }
    public LColour getLColour() {
        return colour;
    }

    public void setLColour(LColour colour) {
        this.colour = colour;
    }

}
