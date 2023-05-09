package bischemes.level.parts.behaviour;

import bischemes.engine.VisualUtils;
import bischemes.level.Level;
import bischemes.level.Room;
import bischemes.level.parts.RObject;
import bischemes.level.util.SpriteLoader;
import processing.core.PVector;

public class BInteractTeleport extends BInteract {

    private final Teleporter teleporter;

    protected BInteractTeleport(RObject interactable, float x, float y, Room destination, PVector link, boolean swapColour) {
        super(interactable, x, y);
        teleporter = new Teleporter(interactable, destination, link, swapColour);
    }

    protected BInteractTeleport(RObject interactable, float r, Room destination, PVector link, boolean swapColour) {
        super(interactable, r);
        teleporter = new Teleporter(interactable, destination, link, swapColour);
    }

    public static BInteractTeleport assign(RObject interactable, float x, float y, PVector link, boolean swapColour) {
        return new BInteractTeleport(interactable, x, y, null, link, swapColour);
    }

    public static BInteractTeleport assign(RObject interactable, float x, float y, Room destination, PVector link,
                                           boolean swapColour) {
        return new BInteractTeleport(interactable, x, y, destination, link, swapColour);
    }

    public static BInteractTeleport assign(RObject interactable, float r, PVector link, boolean swapColour) {
        return new BInteractTeleport(interactable, r, null, link, swapColour);
    }

    public static BInteractTeleport assign(RObject interactable, float r, Room destination, PVector link,
                                           boolean swapColour) {
        return new BInteractTeleport(interactable, r, destination, link, swapColour);
    }

    @Override
    public void addIndicator(PVector indicatorOffset) {
        indicator = VisualUtils.makeTexturedPolygon(
                new PVector(1, 1), 4, 0, indicatorOffset, SpriteLoader.getSwitchSymbol());
    }

    public void addTeleportIndicator(PVector indicatorOffset) {
        indicator = VisualUtils.makeTexturedPolygon(
                new PVector(1, 1), 4, 0, indicatorOffset, SpriteLoader.getTeleportSymbol());
    }

    public void addColourSwitchIndicator(PVector indicatorOffset) {
        indicator = VisualUtils.makeTexturedPolygon(
                new PVector(1, 1), 4, 0, indicatorOffset, SpriteLoader.getSwitchSymbol());
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
    public void onInteraction() {
        teleporter.teleport(room.getLevel().getPlayer());
    }

    @Override
    public void setColour(int colour) {
        super.setColour(colour);
        teleporter.setColour();
    }

}
