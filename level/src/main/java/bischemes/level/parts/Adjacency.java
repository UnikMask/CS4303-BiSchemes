package bischemes.level.parts;

import bischemes.engine.GObject;
import bischemes.engine.physics.Primitive;
import bischemes.engine.physics.RigidBody;
import bischemes.engine.physics.RigidBodyProperties;
import bischemes.engine.physics.Surface;
import bischemes.level.Level;
import bischemes.level.Room;
import bischemes.level.parts.behaviour.BHitTeleport;
import bischemes.level.util.LColour;
import bischemes.level.util.LevelParseException;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Adjacency extends RObject {

    private boolean initialised = false;
    private final int roomId;
    private final int linkId;
    private final boolean isVertical;
    private final boolean zeroAxis;
    private final float length;

    private Room linkedRoom;
    private Adjacency linkedAdjacency;

    public Adjacency(GObject parent, PVector range, boolean isVertical, boolean zeroAxis, int id, LColour colour) {
        this(parent, range, -1, -1, isVertical, zeroAxis, id, colour);
        initialised = true;
    }

    public Adjacency(GObject parent, PVector range, int roomId, int linkId, boolean isVertical, boolean zeroAxis,
                     int id, LColour colour) {
        super(parent, new PVector(), 0f, id, colour);
        this.roomId = roomId;
        this.linkId = linkId;
        this.isVertical = isVertical;
        this.zeroAxis = zeroAxis;

        length = range.y - range.x;
        float anchor = (range.x + range.y) / 2f;

        float halfLength = length / 2f;
        List<PVector> vertices = new ArrayList<>(4);

        if (isVertical) {
            position.x = anchor;

            float y = -1;
            if (!zeroAxis) {
                position.y = Room.getRoom(parent).getDimensions().y;
                y = 1;
            }

            vertices.add(new PVector(-halfLength, 0));
            vertices.add(new PVector(halfLength, 0));
            vertices.add(new PVector(halfLength, y));
            vertices.add(new PVector(-halfLength, y));
        }
        else {
            position.y = anchor;

            float x = -1;
            if (!zeroAxis) {
                position.x = Room.getRoom(parent).getDimensions().x;
                x = 1;
            }

            vertices.add(new PVector(0, -halfLength));
            vertices.add(new PVector(0, halfLength));
            vertices.add(new PVector(x, halfLength));
            vertices.add(new PVector(x, -halfLength));
        }

        setRigidBody(new RigidBody(new RigidBodyProperties(Map.of(
                "mesh", new Primitive(new Surface(0,0,0), vertices)))));

    }

    public void init() {
        if (initialised) return;
        Level level = Room.getRoom(parent).getLevel();

        linkedRoom = level.getRoom(roomId);
        linkedAdjacency = linkedRoom.getAdjacency(linkId);
        if (linkedAdjacency.isVertical != isVertical)
            throw new LevelParseException("adjacency (id = " + id + ") initialisation failed. Vertical/Horizontal orientation does not match linked adjacency (roomID = " + roomId + ", id = " + linkId + ")");
        if (linkedAdjacency.zeroAxis == zeroAxis)
            throw new LevelParseException("adjacency (id = " + id + ") initialisation failed. Adjacency not on opposite side to linked adjacency (roomID = " + roomId + ", id = " + linkId + ")");
        if (linkedAdjacency.length != length)
            throw new LevelParseException("adjacency (id = " + id + ") initialisation failed. Length does not match linked adjacency (roomID = " + roomId + ", id = " + linkId + ")");
        if (linkedAdjacency.colour != colour)
            throw new LevelParseException("adjacency (id = " + id + ") initialisation failed. Colour does not match linked adjacency (roomID = " + roomId + ", id = " + linkId + ")");

        BHitTeleport b = BHitTeleport.assign(this, linkedRoom, linkedAdjacency.position, false);
        b.configureOffset(true, true, !isVertical, isVertical);
        b.addAdditionalOffset(
                ((isVertical) ? 0 : 1) * ((zeroAxis) ? 1 : -1),
                ((isVertical) ? 1 : 0) * ((zeroAxis) ? 1 : -1));

        initialised = true;
    }


}
