package bischemes.level;

import bischemes.level.parts.Adjacency;
import bischemes.level.util.InvalidIdException;
import bischemes.level.util.JParser;
import bischemes.level.util.LColour;
import bischemes.level.util.LevelParseException;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/** Holds the information of a Level in the game, including its individual Rooms */
public class Level {

    /** name of the Level, drawn as text in the UI menu */
    private final String name;
    /** unique identifier for the Level (validity of uniqueness is checked by Levels.java) */
    private final int id;
    /** ids of prerequisite Level objects which require completion before this Level can be played */
    private final int[] prerequisites;
    /** primary colour of the Level's geometry/objects */
    private final int colourPrimary;
    /** secondary colour of the Level's geometry/objects */
    private final int colourSecondary;
    /** array of individual Room objects which comprise the Level */
    private final Room[] rooms;
    /** id of the initial Room of the Level */
    private final int initRoomId;
    /** String array of Room JSON filenames used to load 'rooms' from */
    private final String[] roomFiles;
    /** name of the Level JSON file this Level is parsed from */
    private final String filename;
    /** directory where the Level JSON file (and any related Room JSON files) can be found */
    private final String directory;
    /** whether the Level has been completed */
    private boolean completed = false;
    private GameInterface game;

    private Level(String filename, String directory, String name, int id, int[] prerequisites, int colourPrimary,
                  int colourSecondary, int totalRooms, String[] roomFiles, int initRoomId) {
        this.filename = filename;
        this.directory = directory;
        this.name = name;
        this.id = id;
        this.prerequisites = prerequisites;
        this.colourPrimary = colourPrimary;
        this.colourSecondary = colourSecondary;
        this.rooms = new Room[totalRooms];
        this.roomFiles = roomFiles;
        this.initRoomId = initRoomId;
    }

    public String getName() { return name; }
    public int getId() { return id; }
    public int[] getPrerequisites() { return prerequisites; }
    public int getColourPrimary() { return colourPrimary; }
    public int getColourSecondary() { return colourSecondary; }
    public Room getInitRoom() { return Room.getRoom(rooms, initRoomId); }
    public Room[] getRooms() { return rooms; }
    public Room getRoom(int id) { return Room.getRoom(rooms, id); }
    public int getColour(LColour colour) {
        return switch (colour) {
            case PRIMARY -> colourPrimary;
            case SECONDARY -> colourSecondary;
        };
    }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public boolean isCompleted() { return completed; }

    public void setGame(GameInterface g) { this.game = g; }
    public GameInterface getGame() {
        return game;
    }


    /**
     * attempts to parse a Level from a Level JSON file
     * @param levelDir the directory of the Level JSON file
     * @param infoFile the filename of the Level JSON file
     * @param skipOnFail determines whether exceptions should halt all parsing or whether the parsing of the current
     *                   Level JSON file should just be skipped
     * @return a correctly parsed Level object
     */
    public static Level parseLevel(String levelDir, String infoFile, boolean skipOnFail) {
        BufferedInputStream in = null;
        JsonReader jsonReader = null;
        String fullFilePath = levelDir + "/" + infoFile;
        try {
            in = new BufferedInputStream(new FileInputStream(fullFilePath));
            jsonReader = Json.createReader(in);
            return parseLevel(jsonReader, infoFile, levelDir);
        } catch (FileNotFoundException | InvalidIdException | LevelParseException e) {
            System.out.println("parseLevel(" + levelDir + ", " + infoFile + "), encountered an exception:");
            System.out.println("\t" + e.getLocalizedMessage());
            if (skipOnFail) System.out.println("\tSkipping level parse...");
            else throw new RuntimeException(e);
        }
        try {
            if (jsonReader != null) jsonReader.close();
            if (in != null) in.close();
        } catch (IOException ignored) {}
        return null;
    }

    /**
     * Parses a Level from a JsonReader
     * @param jsonReader the JsonReader initialised from the Level JSON file
     * @param filename the name of Level JSON file (used in Room JSON parsing)
     * @param roomDir the directory of the Level JSON file (used in Room JSON parsing)
     * @return a parsed Level
     */
    private static Level parseLevel(JsonReader jsonReader, String filename, String roomDir) {
        JsonObject levelJson = jsonReader.readObject();

        int id = JParser.parseInt(levelJson, "id");
        int initId  = JParser.parseInt(levelJson, "initRoomID");

        String name = JParser.parseStr(levelJson, "name");

        int cPri = JParser.parseColour(levelJson, "colourPrimary");
        int cSec = JParser.parseColour(levelJson, "colourSecondary");
        int[] prqs = JParser.parseInts(levelJson, "prerequisites");

        JsonArray jRooms = JParser.parseArr(levelJson, "rooms");
        if (jRooms.size() == 0)
            throw new LevelParseException("\"rooms\" is empty (the level has no rooms)");

        String[] roomFiles = null;
        if (! (jRooms.get(0) instanceof JsonObject)) roomFiles = JParser.parseStrs(jRooms, "rooms");

        Level level = new Level(filename, roomDir, name, id, prqs, cPri, cSec, jRooms.size(), roomFiles, initId);

        level.initialiseRooms();

        return level;
    }

    /**
     * Parses the Room JSON which corresponds to this Level's Level JSON
     * This can be used to reload Room data (e.g. reset Rooms to their original state)
     */
    public void initialiseRooms() {
        JsonObject[] roomObjs = new JsonObject[rooms.length];
        // Parses Room JSON from individual files
        if (roomFiles == null) {
            BufferedInputStream in;
            JsonReader jsonReader;
            String fullFilePath = directory + "/" + filename;
            try {
                in = new BufferedInputStream(new FileInputStream(fullFilePath));
                jsonReader = Json.createReader(in);
            } catch (FileNotFoundException | InvalidIdException | LevelParseException e) {
                throw new RuntimeException(e);
            }
            JsonArray jRooms = JParser.parseArr(jsonReader.readObject(), "rooms");

            for (int i = 0; i < roomObjs.length; i++)
                roomObjs[i] = jRooms.getJsonObject(i);

            try {
                jsonReader.close();
                in.close();
            } catch (IOException ignored) {}
        }
        // Parses Room JSON from "rooms" array of the Level JSON file
        else {
            for (int i = 0; i < roomObjs.length; i++) {
                BufferedInputStream in;
                try { in = new BufferedInputStream(new FileInputStream(directory + "/" + roomFiles[i])); }
                catch (FileNotFoundException e) {
                    throw new LevelParseException("FileNotFoundException, could not find \"" + directory + "/" +
                            roomFiles[i] + "\" from \"rooms\"");
                }

                JsonReader roomReader = Json.createReader(in);
                roomObjs[i] = roomReader.readObject();

                try {
                    roomReader.close();
                    in.close();
                } catch (IOException ignored) {}
            }
        }
        // Checks that no Room ids are repeated
        for(int i = 0; i < rooms.length; i++) {
            rooms[i] = Room.parseRoom(this, roomObjs[i]);
            for (int j = 0; j < i; j++) {
                if (rooms[i].getId() == rooms[j].getId())
                    throw new InvalidIdException("\"id\" " + id + " for room in level (" + id + ", " + name + ") " +
                            "is repeated (indexes " + j + ", " + i + ")");
            }
        }
        // Initialises Adjacency objects within Room (links them between Rooms)
        for (Room room : rooms)
            for (Adjacency adjacency : room.getAdjacencies())
                adjacency.init();
    }

}
