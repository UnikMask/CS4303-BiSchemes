package bischemes.level;

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


public class Level {

    private final String name;
    private final int id;
    private final int[] prerequisites;
    private final int colourPrimary;
    private final int colourSecondary;
    private final Room initRoom;
    private final Room[] rooms;

    private Level(String name, int id, int[] prerequisites, int colourPrimary, int colourSecondary, Room initRoom, Room[] rooms) {
        this.name = name;
        this.id = id;
        this.prerequisites = prerequisites;
        this.colourPrimary = colourPrimary;
        this.colourSecondary = colourSecondary;
        this.initRoom = initRoom;
        this.rooms = rooms;

        for (Room room : this.rooms) room.setParentLevel(this);
    }

    public String getName() { return name; }
    public int getId() { return id; }
    public int[] getPrerequisites() { return prerequisites; }
    public int getColourPrimary() { return colourPrimary; }
    public int getColourSecondary() { return colourSecondary; }
    public Room getInitRoom() { return initRoom; }
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
        String fullFilePath = levelDir + "\\" + infoFile;
        try {
            in = new BufferedInputStream(new FileInputStream(fullFilePath));
            jsonReader = Json.createReader(in);
            return parseLevel(jsonReader, levelDir);
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

    private static Level parseLevel(JsonReader jsonReader, String roomDir) throws InvalidIdException, LevelParseException {
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

        JsonObject[] roomObjs = new JsonObject[jRooms.size()];
        if (jRooms.get(0) instanceof JsonObject) {
            for (int i = 0; i < roomObjs.length; i++) roomObjs[i] = jRooms.getJsonObject(i);
        }
        else {
            String[] rooms = JParser.parseStrs(jRooms, "rooms");
            for (int i = 0; i < roomObjs.length; i++) {
                BufferedInputStream in;
                try { in = new BufferedInputStream(new FileInputStream(roomDir + "\\" + rooms[i])); }
                catch (FileNotFoundException e) {
                    throw new LevelParseException("FileNotFoundException, could not find \"" + roomDir + "\\" + rooms[i] + "\" from \"rooms\"");
                }

                JsonReader roomReader = Json.createReader(in);
                roomObjs[i] = roomReader.readObject();

                jsonReader.close();
                try { in.close(); }
                catch (IOException ignored) {}
            }
        }

        Room[] rooms = new Room[jRooms.size()];
        for(int i = 0; i < rooms.length; i++) {
            rooms[i] = Room.parseRoom(roomObjs[i]);
            //todo add better error reporting here?
            for (int j = 0; j < i; j++) {
                if (rooms[i].getId() == rooms[j].getId())
                    throw new InvalidIdException("\"id\" " + id + " for room in level (" + id + ", " + name + ") is repeated (indexes " + j + ", " + i + ")");
            }
        }

        //TODO check validity of rooms, i.e. room adjacency is valid

        Room initRoom = Room.getRoom(rooms, initId);

        return new Level(name, id, prqs, cPri, cSec, initRoom, rooms);
    }

}
