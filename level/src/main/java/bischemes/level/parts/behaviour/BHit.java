package bischemes.level.parts.behaviour;

import bischemes.engine.GObject;

public abstract class BHit implements Behaviour {

    protected boolean activeOnState = false;
    protected boolean stateActivity;

    public abstract void run(GObject hit);

    public abstract void setActiveOnState(boolean activeOnState);

}
