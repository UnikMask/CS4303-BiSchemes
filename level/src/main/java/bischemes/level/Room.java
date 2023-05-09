package bischemes.level;

import bischemes.engine.GObject;
import bischemes.level.parts.Adjacency;
import bischemes.level.parts.PartFactory;
import bischemes.level.parts.RObject;
import bischemes.level.util.InvalidIdException;
import bischemes.level.util.JParser;
import bischemes.level.util.LevelParseException;
import processing.core.PVector;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;


/** Holds the geometry, objects and information of a single Room of a Level */
public class Room extends GObject {

    /** The Level which this Room belongs to */
    private final Level parent;
    /** Unique identifier for the Room */
    private final int id;
    /** The dimension (width and height) of the Room */
    private final PVector dimensions;
    /** The position (x and y coordinate) the Player spawns at in the Room */
    private final PVector spawnPos;
    /** Parent object for all the Room geometry of the Level's primary colour */
    private final GObject primaryGeometry;
    /** Parent object for all the Room geometry of the Level's secondary colour */
    private final GObject secondaryGeometry;
    /** List of all Room Objects */
    private final List<RObject> roomObjects;
    /** List of all Adjacency objects (sub-list of roomObjects)*/
    private final List<Adjacency> adjacencies;

    /**
     * Recursively finds the Room that any GObject belongs to
     * @param child the GObject which is a child of the Room
     * @return the Room of the GObject (or null if it reaches a GObject which isn't a Room and has no parent)
     */
    public static Room getRoom(GObject child) {
        if (child == null) return null;
        if (child instanceof Room) return (Room) child;
        return getRoom(child.getParent());
    }

    /**
     * Takes a Room array and returns the Room with the provided id or throws an InvalidIdException if it doesn't exist
     * @param rooms Array of Room objects to search
     * @param id The Room id to search for
     * @return The Room possessing the id
     */
    public static Room getRoom(Room[] rooms, int id) {
        for (Room room : rooms) if (room.id == id) return room;
        throw new InvalidIdException("getRoom(id), room with id " + id + " does not exist");
    }

    public Level getLevel() { return parent; }
    public int getId() { return id; }
    public PVector getDimensions() { return dimensions.copy(); }
    public PVector getSpawnPosition() { return spawnPos.copy(); }
    public GObject getPrimaryGeometry() { return primaryGeometry; }
    public GObject getSecondaryGeometry() { return secondaryGeometry; }
    public List<RObject> getObjects() { return roomObjects; }
    public List<Adjacency> getAdjacencies() { return adjacencies; }

    /**
     * Searches for and returns the RObject with the provided id or throws an InvalidIdException if it doesn't exist
     * @param id The RObject id to search for
     * @return The RObject possessing the id
     */
    public RObject getObject(int id) {
        for (RObject object : roomObjects) if (object.getId() == id) return object;
        throw new InvalidIdException("getObject(id), RObject with id " + id + " does not exist");
    }
    /**
     * Searches for and returns the Adjacency with the provided id or throws an InvalidIdException if it doesn't exist
     * @param id The Adjacency id to search for
     * @return The Adjacency possessing the id
     */
    public Adjacency getAdjacency(int id) {
        for (Adjacency adjacency : adjacencies) if (adjacency.getId() == id) return adjacency;
        throw new InvalidIdException("getAdjacency(id), Adjacency with id " + id + " does not exist");
    }


    private boolean interaction;
    /**
     * Flags that an INTERACT command has occurred within the last update() frame
     */
    public void interact() {
        System.out.println("INTERACTION DETECTED");
        interaction = true;
    }

    // TODO
    public boolean isInteraction() {
        if (!interaction) return false;
        //interaction = false; // will potentially only want to allow one interaction per key press?
        return true;
    }

    // TODO
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

    /**
     * Parses Room JSON and creates a Room object if successful
     * @param parent The Level which the parsed Room belongs to
     * @param roomJson The JsonObject holding the Room JSON
     * @return a newly parsed Room
     */
    public static Room parseRoom(Level parent, JsonObject roomJson) {
        Room room;
        int id = -1; //id is declared outside of try{} so that it may be used in exception messages
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

            PartFactory pf = new PartFactory();
            pf.createCornerRect(room.primaryGeometry, new PVector(0, 0), new PVector(-1, dims.y), 0).removeAllVisualAttributes();
            pf.createCornerRect(room.primaryGeometry, new PVector(0, 0), new PVector(dims.x, -1), 0).removeAllVisualAttributes();
            pf.createCornerRect(room.primaryGeometry, new PVector(dims.x, 0), new PVector(-1, dims.y), 0).removeAllVisualAttributes();
            pf.createCornerRect(room.primaryGeometry, new PVector(0, dims.y), new PVector(dims.x, -1), 0).removeAllVisualAttributes();
            pf.createCornerRect(room.secondaryGeometry, new PVector(0, 0), new PVector(-1, dims.y), 0).removeAllVisualAttributes();
            pf.createCornerRect(room.secondaryGeometry, new PVector(0, 0), new PVector(dims.x, -1), 0).removeAllVisualAttributes();
            pf.createCornerRect(room.secondaryGeometry, new PVector(dims.x, 0), new PVector(1, dims.y), 0).removeAllVisualAttributes();
            pf.createCornerRect(room.secondaryGeometry, new PVector(0, dims.y), new PVector(dims.x, 1), 0).removeAllVisualAttributes();

            //TODO remove primary visual attributes
            //for (GObject g : room.primaryGeometry.getChildren()) g.removeAllVisualAttributes();

            JParser.parseRObjectArr(roomJson, "objects", room, room.roomObjects);

            JParser.parseAdjacencyArr(roomJson, "adjacent", room, room.adjacencies);

            room.roomObjects.addAll(room.adjacencies);

        } catch (LevelParseException e) {
            throw new LevelParseException("parseRoom(" + ((id!=-1) ? id : "") + "), encountered a LevelParseException\n\t"+e.getLocalizedMessage());
        } catch (InvalidIdException e) {
            throw new InvalidIdException("parseRoom(" + ((id!=-1) ? id : "") + "), encountered an InvalidIdException \n\t"+e.getLocalizedMessage());
        }
        // calls setColour() for all geometry and RObjects
        room.primaryGeometry.setColour(room.parent.getColourPrimary());
        room.secondaryGeometry.setColour(room.parent.getColourSecondary());
        for (RObject rObject : room.roomObjects) {
            if (rObject.getLColour() == null) continue;
            switch (rObject.getLColour()) {
                case PRIMARY -> rObject.setColour(room.parent.getColourPrimary());
                case SECONDARY -> rObject.setColour(room.parent.getColourSecondary());
            }
        }
        // validates that ids are unique for every RObject
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
