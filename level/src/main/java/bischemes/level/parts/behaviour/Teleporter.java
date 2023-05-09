package bischemes.level.parts.behaviour;

import bischemes.engine.GObject;
import bischemes.engine.VisualAttribute;
import bischemes.engine.VisualUtils;
import bischemes.level.Level;
import bischemes.level.PlayerAbstract;
import bischemes.level.Room;
import bischemes.level.parts.RObject;
import bischemes.level.util.LColour;
import bischemes.level.util.SpriteLoader;
import processing.core.PVector;

import static java.lang.Math.min;

public class Teleporter {

    private final RObject base;
    private final Room room;
    private final Room destination;
    private final PVector link;
    private final boolean swapColour;

    private final int colourPrimary;
    private final int colourSecondary;

    private VisualAttribute teleportIcon = null;

    private boolean mirrorX = false;
    private boolean mirrorY = false;
    private boolean offsetX = false;
    private boolean offsetY = false;
    private boolean playerOnly = false;

    private boolean flipGravity = false;

    public Teleporter(RObject base, Room destination, PVector link, boolean swapColour) {
        this.base = base;
        this.destination = destination;
        this.link = link;
        this.swapColour = swapColour;
        this.room = Room.getRoom(base);
        if (swapColour) //TODO eventually it would be nice for swapColour to be possible on all RObjects
            makePlayerOnly();
        if (destination != null)
            playerOnly = destination.getLevel().getId() != room.getLevel().getId();
        Level l = room.getLevel();
        colourPrimary = l.getColourPrimary();
        colourSecondary = l.getColourSecondary();
    }

    public Teleporter(RObject base, PVector link, boolean swapColour) {
        this(base, null, link, swapColour);
    }


    public void addTeleportIcon(PVector maxDimension) {
        PVector dimension = new PVector(
                min(maxDimension.x, 1),
                min(maxDimension.y, 1));
        teleportIcon = VisualUtils.makeRect(dimension, SpriteLoader.getSwitchSymbol());
        base.addVisualAttributes(teleportIcon);
    }

    public void makePlayerOnly() {
        playerOnly = true;
    }

    public void configureGravityFlip(boolean flipGravity) {
        this.flipGravity = flipGravity;
    }

    public void configureOffset(boolean offsetX, boolean offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public void configureOffset(boolean offsetX, boolean offsetY, boolean mirrorX, boolean mirrorY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.mirrorX = mirrorX;
        this.mirrorY = mirrorY;
    }

    public void configureMirror(boolean mirrorX, boolean mirrorY) {
        configureOffset(mirrorX || offsetX, mirrorY || offsetY, mirrorX, mirrorY);
    }

    public void teleport(GObject target) {

        PVector newPosition = link.copy();
        PVector offset = target.getLocalPosition().copy().sub(base.getLocalPosition());
        if (offsetX) {
            if (mirrorX) offset.x *= -1;
            newPosition.x += offset.x;
        }
        if (offsetY) {
            if (mirrorY) offset.y *= -1;
            newPosition.y += offset.y;
        }

        if (target instanceof PlayerAbstract player) {
            if (flipGravity) {
                PVector direction = player.getGravityDirection().copy();
                direction.y *= -1;
                player.setGravityDirection(direction);
            }
            else if (destination != null && destination.getId() == room.getId())
                room.getLevel().getGame().loadNextRoom(destination, newPosition);
            else
                target.setLocalPosition(newPosition);

            if (swapColour)
                room.getLevel().getGame().switchPlayerColour();
        }
        else  {
            if (playerOnly) return;
            if (flipGravity) {
                PVector direction = base.getGravityDirection().copy();
                direction.y *= -1;
                base.setGravityDirection(direction);
            }
            target.setLocalPosition(newPosition);
        }
    }

    public void setColour() {
        if (!swapColour || teleportIcon == null) return;
        if (base.getLColour() == LColour.PRIMARY)
            teleportIcon.setColour(colourSecondary);
        if (base.getLColour() == LColour.SECONDARY)
            teleportIcon.setColour(colourPrimary);
    }
}
