package bischemes.level.parts.behaviour;

import bischemes.engine.GObject;
import bischemes.engine.physics.Manifold;
import bischemes.level.Room;
import bischemes.level.parts.RObject;
import processing.core.PVector;

public class BHitTeleport extends BHit {

    private final RObject object;
    private final Teleporter teleporter;

    private BHitTeleport(RObject object, Room destination, PVector link, boolean swapColour) {
        this.object = object;
        this.teleporter = new Teleporter(object, destination, link, swapColour);
        object.addOnHit(this);
    }

    public static BHitTeleport assign(RObject object, PVector link, boolean swapColour) {
        return new BHitTeleport(object, null, link, swapColour);
    }

    public static BHitTeleport assign(RObject object, Room destination, PVector link, boolean swapColour) {
        return new BHitTeleport(object, destination, link, swapColour);
    }

    public void setActiveOnState(boolean activeOnState) {
        this.activeOnState = activeOnState;
        this.stateActivity = true;
    }

    public void addTeleportIcon(PVector maxDimension) {
        teleporter.addTeleportIcon(maxDimension);
    }

    public void configureGravityFlip(boolean flipGravity) { teleporter.configureGravityFlip(flipGravity);}

    public void makePlayerOnly() {
        teleporter.makePlayerOnly();
    }

    public void configureOffset(boolean offsetX, boolean offsetY) {
        teleporter.configureOffset(offsetX, offsetY);
    }

    public void configureOffset(boolean offsetX, boolean offsetY, boolean mirrorX, boolean mirrorY) {
        teleporter.configureOffset(offsetX, offsetY, mirrorX, mirrorY);
    }

    public void configureMirror(boolean mirrorX, boolean mirrorY) {
        teleporter.configureMirror(mirrorX, mirrorY);
    }

    @Override
    public void run(GObject hit, Manifold m) {
        if (activeOnState && (stateActivity != object.getState())) return;
        teleporter.teleport(hit);
    }

    @Override
    public void setColour(int colour) {
        teleporter.setColour();
    }
}
