package bischemes.level.parts;

import bischemes.engine.GObject;
import bischemes.engine.physics.Manifold;
import bischemes.engine.physics.ForceGenerators.DirectionalGravity;
import bischemes.level.parts.behaviour.BHit;
import bischemes.level.parts.behaviour.BState;
import bischemes.level.parts.behaviour.BUpdate;
import bischemes.level.util.InvalidIdException;
import bischemes.level.util.LColour;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;

/** An extension to GObject to create RoomObjects which possess ids, boolean state and definable Behaviours */
public class RObject extends GObject {

    /**
     * Creates a new RObject
     * @param parent parent object
     * @param position position relative to parent object
     * @param orientation rotation around position
     * @param id unique identifier of object
     */
    public RObject(GObject parent, PVector position, float orientation, int id) {
        this(parent, position, orientation, id, null);
    }

    /**
     * Creates a new RObject
     * @param parent parent object
     * @param position position relative to parent object
     * @param orientation rotation around position
     * @param id unique identifier of object
     * @param colour colour of the object
     */
    public RObject(GObject parent, PVector position, float orientation, int id, LColour colour) {
        super(parent, position, orientation);
        this.id = id;
        if (id < 0)
            throw new InvalidIdException("\"id\" of " + id + " is invalid, ids cannot be negative");
        this.colour = colour;
    }

    /** Unique identifier for the RObject (unqiue only in the context of a single Room) */
    protected int id;
    /** Colour of the RObject, PRIMARY, SECONDARY or null (no colour) */
    protected LColour colour;
    /** State of the RObject, influences Behaviour run() methods */
    protected boolean state = false;
    /** Behaviours which are called during a change of state */
    protected List<BState> bState = null;
    /** Behaviours which are called during update() */
    protected List<BUpdate> bUpdate = null;
    /** Behaviours which are called upon collision with another GObject */
    protected List<BHit> bHit = null;

	// Components related to game
	protected GObject player;
	protected DirectionalGravity gravity;

    public int getId() { return id; }
    public LColour getLColour() { return colour; }
    public void setLColour(LColour colour) { this.colour = colour; }
    public boolean getState() { return state; }

    /**
     * Sets the RObject's state to the boolean value and the state of all its RObject children.
     * Calls the on-state-change (BState) Behaviour run() methods if state changes.
     * @param state the state to set this RObject's state to
     */
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

    /**
     * Switches the state of the RObject and the state of all its RObject children.
     * Calls the on-state-change (BState) Behaviour run() methods.
     */
    public void switchState() {
        state = !state;
        if (bState != null)
            for (BState o : bState)
                o.run();
        for (GObject c : children)
            if (c instanceof RObject r)
                r.switchState();
    }

	public void init(GObject player, DirectionalGravity gravity) {
		this.player = player;
		this.gravity = gravity;

		// Add gravity if movable or rotatable.
		if (getRigidBody().getProperties().isMovable || getRigidBody().getProperties().isRotatable) {
			addOnUpdate(new BUpdate(null) {
					public void run() {
						gravity.updateForce(getRigidBody());
					}
					public void setColour(int c) {
						// Placeholder
					}
				});
		}
	}

    /**
     * Calls all the BUpdate Behaviour run() methods
     */
    @Override
    public void update() {
        if (bUpdate != null)
            for (BUpdate o : bUpdate)
                o.run();
    }

    /**
     * Calls all the on-hit (BHit) behaviour run() methods
     * @param hit the GObject collision occurred with
     * @param m collision manifold the for the collision
     */
    @Override
    public void onHit(GObject hit, Manifold m) {
        if (bHit != null)
            for (BHit o : bHit)
                o.run(hit, m);
    }

    /**
     * Adds a new on-state-change (BState) Behaviour
     * @param bState the new Behaviour to add
     */
    public void addOnStateChange(BState bState) {
        if (this.bState == null)
            this.bState = new ArrayList<>();
        this.bState.add(bState);
    }
    /**
     * Adds a new on-update (BUpdate) Behaviour
     * @param bUpdate the new Behaviour to add
     */
    public void addOnUpdate(BUpdate bUpdate) {
        if (this.bUpdate == null)
            this.bUpdate = new ArrayList<>();
        this.bUpdate.add(bUpdate);
    }
    /**
     * Adds a new on-collision (BHit) Behaviour
     * @param bHit the new Behaviour to add
     */
    public void addOnHit(BHit bHit) {
        if (this.bHit == null)
            this.bHit = new ArrayList<>();
        this.bHit.add(bHit);
    }

    /**
     * Runs GObject's setColour() method and then runs setColour() for all Behaviours
     * @param colour the RGB integer colour to set
     */
    @Override
    public void setColour(int colour) {
        super.setColour(colour);
        if (bState != null)
            for (BState o : bState)
                o.setColour(colour);
        if (bUpdate != null)
            for (BUpdate o : bUpdate)
                o.setColour(colour);
        if (bHit != null)
            for (BHit o : bHit)
                o.setColour(colour);
    }

}
