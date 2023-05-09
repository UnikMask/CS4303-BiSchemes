package bischemes.level;

import bischemes.engine.GObject;
import processing.core.PVector;

public abstract class PlayerAbstract extends GObject {


    public PlayerAbstract(GObject parent, PVector position, float rotation) {
        super(parent, position, rotation);
    }

    public abstract PVector getGravityDirection();

    public abstract void setGravityDirection(PVector direction);

}
