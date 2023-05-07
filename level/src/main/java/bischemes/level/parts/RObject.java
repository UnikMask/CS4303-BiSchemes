package bischemes.level.parts;

import bischemes.engine.GObject;
import bischemes.engine.physics.Manifold;
import bischemes.level.parts.behaviour.BHit;
import bischemes.level.parts.behaviour.BState;
import bischemes.level.parts.behaviour.BUpdate;
import bischemes.level.util.InvalidIdException;
import bischemes.level.util.LColour;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;

public class RObject extends GObject {

    public RObject(GObject parent, PVector position, float orientation, int id) {
        super(parent, position, orientation);
        this.id = id;
        if (id < 0)
            throw new InvalidIdException("\"id\" of " + id + " is invalid, ids cannot be negative");
    }

    public RObject(GObject parent, PVector position, float orientation, int id, LColour colour) {
        super(parent, position, orientation);
        this.id = id;
        if (id < 0)
            throw new InvalidIdException("\"id\" of " + id + " is invalid, ids cannot be negative");
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
        if (state ^ this.state) {
            this.state = state;
            if (bState != null)
                for (BState o : bState)
                    o.run();
        }
        for (GObject c : children)
            if (c instanceof RObject r)
                r.setState(state);
    }
    public void switchState() {
        state = !state;
        if (bState != null)
            for (BState o : bState)
                o.run();
        for (GObject c : children)
            if (c instanceof RObject r)
                r.switchState();
    }

    @Override
    public void update() {
        if (bUpdate != null)
            for (BUpdate o : bUpdate)
                o.run();
    }

    @Override
    public void onHit(GObject hit, Manifold m) {
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
