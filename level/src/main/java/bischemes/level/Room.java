package bischemes.level;

import bischemes.engine.GObject;
import bischemes.level.util.InvalidIdException;
import bischemes.level.util.JParsing;
import bischemes.level.util.LevelParseException;
import processing.core.PVector;

import javax.json.JsonObject;


public class Room {

    private Level parent;

    private final int id;
    private final PVector dimensions; // Dimension (width/height) of the room
    private final PVector spawnPos; // Position the player spawns at in the room (x,y)

    private final GObject primaryGeometry;
    private final GObject secondaryGeometry;

    private final GObject roomObjects;

    //TODO private final ??? adjacent; // to make points of adjacency between rooms


    public static Room getRoom(Room[] rooms, int id) {
        for (Room room : rooms) if (room.id == id) return room;
        throw new InvalidIdException("getRoom(id), room with id " + id + " does not exist");
    }

    public int getId() { return id; }
    public PVector getDimensions() { return dimensions; }
    public PVector getSpawnPosition() { return spawnPos; }
    public GObject getPrimaryGeometry() { return primaryGeometry; }
    public GObject getSecondaryGeometry() { return secondaryGeometry; }
    public GObject getObjects() { return roomObjects; }

    public void setParentLevel(Level level) {
        this.parent = level;

        primaryGeometry.setColour(level.getColourPrimary());
        secondaryGeometry.setColour(level.getColourSecondary());
    }

    private Room(int id, PVector dimensions, PVector spawnPos) {
        this.id = id;
        this.dimensions = dimensions;
        this.spawnPos = spawnPos;
        primaryGeometry = new GObject(null, new PVector(), 0);
        secondaryGeometry = new GObject(null, new PVector(), 0);
        roomObjects = new GObject(null, new PVector(), 0);
    }


    public static Room parseRoom(JsonObject roomJson) {
        Room room;
        int id = -1;
        try {
             id = JParsing.parseInt(roomJson, "id");
             PVector dims = JParsing.parsePVec(roomJson, "dimensions");
             PVector spawnPos = JParsing.parsePVec(roomJson, "spawnPosition");
             room = new Room(id, dims, spawnPos);

             JsonObject geometry = JParsing.parseObj(roomJson, "geometry");
             JParsing.parseGeometryArr(geometry, "primary", room.primaryGeometry);
             JParsing.parseGeometryArr(geometry, "secondary", room.secondaryGeometry);


             // TODO room object parsing
             // TODO adjacency parsing

        } catch (LevelParseException e) {
            throw new LevelParseException("parseRoom(" + ((id!=-1) ? id : "") + "), encountered a LevelParseException\n\t"+e.getLocalizedMessage());
        } catch (InvalidIdException e) {
            throw new InvalidIdException("parseRoom(" + ((id!=-1) ? id : "") + "), encountered an InvalidIdException \n\t"+e.getLocalizedMessage());
        }
        return room;
    }

}
