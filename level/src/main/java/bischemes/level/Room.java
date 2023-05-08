package bischemes.level;

import bischemes.engine.GObject;
import bischemes.level.parts.Adjacency;
import bischemes.level.parts.RObject;
import bischemes.level.util.InvalidIdException;
import bischemes.level.util.JParser;
import bischemes.level.util.LevelParseException;
import processing.core.PVector;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;


public class Room extends GObject {

    private final Level parent;

    private final int id;
    private final PVector dimensions; // Dimension (width/height) of the room
    private final PVector spawnPos; // Position the player spawns at in the room (x,y)

    private final GObject primaryGeometry;
    private final GObject secondaryGeometry;
    private final List<RObject> roomObjects;
    private final List<Adjacency> adjacencies;

    public static Room getRoom(GObject child) {
        if (child instanceof Room) return (Room) child;
        return getRoom(child.getParent());
    }

    public static Room getRoom(Room[] rooms, int id) {
        for (Room room : rooms) if (room.id == id) return room;
        throw new InvalidIdException("getRoom(id), room with id " + id + " does not exist");
    }

    public Level getLevel() { return parent; }
    public int getId() { return id; }
    public PVector getDimensions() { return dimensions; }
    public PVector getSpawnPosition() { return spawnPos; }
    public GObject getPrimaryGeometry() { return primaryGeometry; }
    public GObject getSecondaryGeometry() { return secondaryGeometry; }
    public List<RObject> getObjects() { return roomObjects; }
    public List<Adjacency> getAdjacencies() { return adjacencies; }

    public RObject getObject(int id) {
        for (RObject object : roomObjects) if (object.getId() == id) return object;
        throw new InvalidIdException("getObject(id), RObject with id " + id + " does not exist");
    }
    public Adjacency getAdjacency(int id) {
        for (Adjacency adjacency : adjacencies) if (adjacency.getId() == id) return adjacency;
        throw new InvalidIdException("getAdjacency(id), Adjacency with id " + id + " does not exist");
    }


    private boolean interaction;
    /**
     * Flags that an INTERACT command has occurred within the last update() frame
     */
    public void interact() {
        interaction = true;
    }

    public boolean isInteraction() {
        if (!interaction) return false;
        interaction = false;
        return true;
    }

    @Override
    public void update() {
        super.update();
        // TODO might need to change when interaction is set to false, the idea is its set to false after all RObjects have been updated
        interaction = false;
    }

    private Room(Level level, int id, PVector dimensions, PVector spawnPos) {
        super(null, new PVector(), 0f);
        this.parent = level;
        this.id = id;
        this.dimensions = dimensions;
        this.spawnPos = spawnPos;
        primaryGeometry = new GObject(this, new PVector(), 0);
        secondaryGeometry = new GObject(this, new PVector(), 0);
        roomObjects = new ArrayList<>();
        adjacencies = new ArrayList<>();
    }


    public static Room parseRoom(Level parent, JsonObject roomJson) {
        Room room;
        int id = -1;
        try {
             id = JParser.parseInt(roomJson, "id");
            if (id < 0)
                throw new InvalidIdException("\"id\" is invalid with " + id + " as id cannot be negative");
            PVector dims = JParser.parsePVec(roomJson, "dimensions");
             if (dims.x < 0 || dims.y < 0)
                 throw new LevelParseException("\"dimensions\" is invalid with [" + dims.x + ", " + dims.y + "]. Dimensions cannot be negative");
             PVector spawnPos = JParser.parsePVec(roomJson, "spawnPosition");
             if (spawnPos.x < 0 || spawnPos.x > dims.x || spawnPos.y < 0 || spawnPos.y > dims.y)
                 throw new LevelParseException("\"spawnPosition\" is invalid with [" + spawnPos.x + ", " + spawnPos.y + "]. Spawn position must be within the room");
            room = new Room(parent, id, dims, spawnPos);

             JsonObject geometry = JParser.parseObj(roomJson, "geometry");
             JParser.parseGeometryArr(geometry, "primary", room.primaryGeometry);
             JParser.parseGeometryArr(geometry, "secondary", room.secondaryGeometry);

             JParser.parseRObjectArr(roomJson, "objects", room, room.roomObjects);

             JParser.parseAdjacencyArr(roomJson, "adjacent", room, room.adjacencies);

        } catch (LevelParseException e) {
            throw new LevelParseException("parseRoom(" + ((id!=-1) ? id : "") + "), encountered a LevelParseException\n\t"+e.getLocalizedMessage());
        } catch (InvalidIdException e) {
            throw new InvalidIdException("parseRoom(" + ((id!=-1) ? id : "") + "), encountered an InvalidIdException \n\t"+e.getLocalizedMessage());
        }

        room.primaryGeometry.setColour(room.parent.getColourPrimary());
        room.secondaryGeometry.setColour(room.parent.getColourSecondary());
        for (RObject rObject : room.roomObjects) {
            if (rObject.getLColour() == null) continue;
            switch (rObject.getLColour()) {
                case PRIMARY -> rObject.setColour(room.parent.getColourPrimary());
                case SECONDARY -> rObject.setColour(room.parent.getColourSecondary());
            }
        }

        for (int j = 0; j < room.roomObjects.size(); j++) {
            for (int k = j + 1; k < room.roomObjects.size(); k++) {
                if (room.roomObjects.get(j).getId() == room.roomObjects.get(k).getId())
                    throw new InvalidIdException("\"id\" " + room.roomObjects.get(j).getId() + " is repeated in room " +
                            "(id = " + room.getId() + ") in level (" + parent.getId() + ", " + parent.getName() + ") " +
                            "is repeated (indexes " + j + ", " + k + ")");
            }
        }

        return room;
    }

}
