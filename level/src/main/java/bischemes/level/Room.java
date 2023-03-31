package bischemes.level;

import processing.core.PVector;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Room {

    // All PVector are in metric units.

    // Dimension (width/height) of the room
    private PVector dimension;
    // Position the player spawns at in the room (x,y)
    private PVector spawnPosition;

    //TODO stop representing room objects & geometry as Strings and implement actual classes for it
    private String[] geometry;
    /* An actual geometry class seems more appropriate.
        Could extend GObject?
        At least whatever class is made should use Visual Attribute (and its included PShape).
     */

    private String[] objects;
    /* Will also individual GObject extensions for each type of object (door, portal, exit, lever, block, etc)

     */

    private Room() {}

    public static Room parseRoom(String path) {
        BufferedInputStream in;
        JsonReader jsonReader;
        try {
            in = new BufferedInputStream(new FileInputStream(path));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        jsonReader = Json.createReader(in);
        JsonObject roomJson = jsonReader.readObject();
        Room room = new Room();

        try {
            PVector[] pVectors;
            pVectors = LevelParser.extractPVectors(roomJson.getJsonArray("dimension"));
            if (pVectors == null || pVectors.length != 1)
                throw new RuntimeException("Invalid Room - " + path + " - invalid dimension");
            room.dimension = pVectors[0];

            pVectors = LevelParser.extractPVectors(roomJson.getJsonArray("spawnPosition"));
            if (pVectors == null || pVectors.length != 1)
                throw new RuntimeException("Invalid Room - " + path + " - invalid spawnPosition");
            room.spawnPosition = pVectors[0];

            //TODO this is temporary parsing of geometer and objects (I don't intend to keep them as Strings)
            JsonArray geometry = roomJson.getJsonArray("geometry");
            if (geometry == null || geometry.size() < 1)
                throw new RuntimeException("Invalid Room - " + path + " - missing geometry");
            room.geometry = new String[geometry.size()];
            for (int i = 0; i < room.geometry.length; i++) {
                JsonObject shape = geometry.getJsonObject(i);
                String shapeString;
                switch (shape.getString("type")) {
                    case "RECT" -> {
                        shapeString = "RECT, " + shape.getString("scheme", "SECONDARY") + ", ";
                        pVectors = LevelParser.extractPVectors(shape.getJsonArray("vertices"));
                        if (pVectors == null || pVectors.length != 2)
                            throw new RuntimeException("Invalid Room - " + path + " - geometry[" + i + "] has invalid RECT vertices");
                        shapeString = shapeString + pVectors[0] + ", " + pVectors[1];
                        room.geometry[i] = shapeString;
                    }
                    case "TRIANGLE" -> {
                        shapeString = "TRIANGLE, " + shape.getString("scheme", "SECONDARY") + ", ";
                        pVectors = LevelParser.extractPVectors(shape.getJsonArray("vertices"));
                        if (pVectors == null || pVectors.length != 3)
                            throw new RuntimeException("Invalid Room - " + path + " - geometry[" + i + "] has invalid TRIANGLE vertices");
                        shapeString = shapeString + pVectors[0] + ", " + pVectors[1] + ", " + pVectors[2];
                        room.geometry[i] = shapeString;
                    }
                    default ->
                            throw new RuntimeException("Invalid Room - " + path + " - geometry[" + i + "] has unrecognised type \"" + shape.getString("type") + "\"");
                }
            }

            JsonArray objects = roomJson.getJsonArray("objects");
            if (objects == null)
                room.objects = new String[1];
            else
                room.objects = new String[objects.size() + 1];

            JsonObject exit = roomJson.getJsonObject("exit");
            pVectors = LevelParser.extractPVectors(exit.getJsonArray("vertices"));
            if (pVectors == null || pVectors.length != 2)
                throw new RuntimeException("Invalid Room - " + path + " - invalid exit vertices");
            room.objects[0] = "EXIT, " + pVectors[0] + ", " + pVectors[1];

            for (int i = 1; i < room.objects.length; i++) {
                JsonObject object = objects.getJsonObject(i - 1);
                String objectString;
                switch (object.getString("type")) {
                    case "DOOR" -> {
                        objectString = "DOOR, " + object.getString("scheme", "SECONDARY") + ", " + object.getInt("id") + ", ";
                        pVectors = LevelParser.extractPVectors(object.getJsonArray("vertices"));
                        if (pVectors == null || pVectors.length != 2)
                            throw new RuntimeException("Invalid Room - " + path + " - objects[" + i + "] has invalid DOOR vertices");
                        objectString = objectString + pVectors[0] + ", " + pVectors[1];
                        room.objects[i] = objectString;
                    }
                    case "LEVER" -> {
                        objectString = "LEVER, " + object.getString("scheme", "SECONDARY") + ", " + object.getInt("id") + ", ";
                        pVectors = LevelParser.extractPVectors(object.getJsonArray("position"));
                        if (pVectors == null || pVectors.length != 1)
                            throw new RuntimeException("Invalid Room - " + path + " - objects[" + i + "] has invalid LEVER vertices");
                        objectString = objectString + pVectors[0] + ", LinkedTo[ ";
                        JsonArray linkedTo = object.getJsonArray("linkedTo");
                        for (int j = 0; j < linkedTo.size(); j++) objectString = objectString + linkedTo.getInt(j) + " ";
                        room.objects[i] = objectString + "]";
                    }
                    case "BLOCK" -> {
                        objectString = "BLOCK, " + object.getString("scheme", "SECONDARY") + ", " + object.getInt("id") + ", ";
                        pVectors = LevelParser.extractPVectors(object.getJsonArray("vertices"));
                        if (pVectors == null || pVectors.length != 2)
                            throw new RuntimeException("Invalid Room - " + path + " - objects[" + i + "] has invalid BLOCK vertices");
                        objectString = objectString + pVectors[0] + ", " + pVectors[1];
                        room.objects[i] = objectString;
                    }
                    case "PORTAL" -> {
                        objectString = "PORTAL, " + object.getInt("id") + ", ";
                        pVectors = LevelParser.extractPVectors(object.getJsonArray("vertices"));
                        if (pVectors == null || pVectors.length != 2)
                            throw new RuntimeException("Invalid Room - " + path + " - objects[" + i + "] has invalid PORTAL vertices");
                        objectString = objectString + pVectors[0] + ", " + pVectors[1];
                        room.objects[i] = objectString;
                    }
                    default ->
                            throw new RuntimeException("Invalid Room - " + path + " - objects[" + i + "] has unrecognised type \"" + object.getString("type") + "\"");
                }
            }

        } catch (NullPointerException | ClassCastException e ) {
            throw new RuntimeException(e);
        }

        return room;
    }
}
