package bischemes.level;

import bischemes.level.parts.Adjacency;
import bischemes.level.parts.RObject;
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
import java.util.List;


public class Level {

    private final String name;
    private final int id;
    private final int[] prerequisites;
    private final int colourPrimary;
    private final int colourSecondary;
    private final Room[] rooms;

    private final int initRoomId;

    private final String[] roomFiles;


    private final String filename;
    private final String directory;



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

    public static Level parseLevel(String levelDir, String infoFile) {
        return parseLevel(levelDir, infoFile, false);
    }
    public static Level parseLevel(String levelDir, String infoFile, boolean skipOnFail) {
        BufferedInputStream in = null;
        JsonReader jsonReader = null;
        String fullFilePath = levelDir + "/" + infoFile;
        try {
            in = new BufferedInputStream(new FileInputStream(fullFilePath));
            jsonReader = Json.createReader(in);
            return parseLevel(infoFile, jsonReader, levelDir);
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

    private static Level parseLevel(String filename, JsonReader jsonReader, String roomDir)
            throws InvalidIdException, LevelParseException {
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

    public void initialiseRooms() {
        JsonObject[] roomObjs = new JsonObject[rooms.length];
        if (roomFiles == null) {

            BufferedInputStream in = null;
            JsonReader jsonReader = null;
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

        for(int i = 0; i < rooms.length; i++) {
            rooms[i] = Room.parseRoom(this, roomObjs[i]);
            for (int j = 0; j < i; j++) {
                if (rooms[i].getId() == rooms[j].getId())
                    throw new InvalidIdException("\"id\" " + id + " for room in level (" + id + ", " + name + ") " +
                            "is repeated (indexes " + j + ", " + i + ")");
            }
            List<RObject> rObjects = rooms[i].getObjects();
            for (int j = 0; j < rObjects.size(); j++) {
                if (rObjects.get(j) == null) continue; //TODO remove once exit implemented
                for (int k = j + 1; k < rObjects.size(); k++) {
                    if (rObjects.get(k) == null) continue; //TODO remove once exit implemented
                    if (rObjects.get(j).getId() == rObjects.get(k).getId())
                        throw new InvalidIdException("\"id\" " + rObjects.get(j).getId() + " is repeated in room " +
                                "(id = " + rooms[i].getId() + ") in level (" + id + ", " + name + ") " +
                                "is repeated (indexes " + j + ", " + k + ")");
                }
            }
        }

        for (Room room : rooms) {
            for (Adjacency adjacency : room.getAdjacencies())
                adjacency.init();
        }

    }

}
