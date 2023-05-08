package bischemes.level.parts.behaviour;

import bischemes.engine.GObject;
import bischemes.engine.physics.Manifold;

public abstract class BHit implements Behaviour {

    protected boolean activeOnState = false;
    protected boolean stateActivity;

    public abstract void run(GObject hit, Manifold m);

    public abstract void setActiveOnState(boolean activeOnState);

}
