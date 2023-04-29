package bischemes.level.util;

import bischemes.engine.GObject;
import bischemes.level.parts.PartFactory;
import bischemes.level.parts.RObjType;
import bischemes.level.parts.RObject;
import processing.core.PVector;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.List;

public final class JParsing {

    private JParsing(){}
    public static JsonObject parseObj(JsonObject obj, String name) {
        try {
            JsonObject obj2 = obj.getJsonObject(name);
            if (obj2 == null) throw new LevelParseException("\"" + name + "\" does not have a mapping");
            return obj2;
        } catch (ClassCastException e) { throw new LevelParseException("\"" + name + "\" is not a JsonObject"); }
    }
    public static JsonArray parseArr(JsonObject obj, String name) {
        try {
            JsonArray arr = obj.getJsonArray(name);
            if (arr == null) throw new LevelParseException("\"" + name + "\" does not have a mapping");
            return arr;
        } catch (ClassCastException e) { throw new LevelParseException("\"" + name + "\" is not a JsonArray"); }
    }
    public static int parseInt(JsonObject obj, String name) {
        try { return obj.getInt(name); }
        catch (NullPointerException e) { throw new LevelParseException("\"" + name + "\" does not have a mapping"); }
        catch (ClassCastException e) { throw new LevelParseException("\"" + name + "\" is not an assignable integer"); }
    }
    public static int parseInt(JsonObject obj, String name, int defaultValue) {
        try { return obj.getInt(name); }
        catch (NullPointerException | ClassCastException e) { return defaultValue; }
    }
    public static int[] parseInts(JsonObject obj, String name) {
        JsonArray arr = parseArr(obj, name);
        int[] ints = new int[arr.size()];
        try { for (int i = 0; i < ints.length; i++) ints[i] = arr.getInt(i); }
        catch (ClassCastException e) { throw new LevelParseException("\"" + name + "\" array does not contain assignable integers"); }
        return ints;
    }

    public static float parseFloat(JsonObject obj, String name) {
        try { return (float) obj.getJsonNumber(name).doubleValue(); }
        catch (NullPointerException e) { throw new LevelParseException("\"" + name + "\" does not have a mapping"); }
        catch (ClassCastException e) { throw new LevelParseException("\"" + name + "\" is not an assignable float"); }
    }
    public static float parseFloat(JsonObject obj, String name, float defaultValue) {
        try { return (float) obj.getJsonNumber(name).doubleValue(); }
        catch (ClassCastException | NullPointerException e) { return defaultValue; }
    }
    public static String parseStr(JsonObject obj, String name) {
        String s;
        try { s = obj.getString(name); }
        catch (NullPointerException e) { throw new LevelParseException("\"" + name + "\" does not have a mapping"); }
        catch (ClassCastException e) { throw new LevelParseException("\"" + name + "\" is not an assignable string"); }
        if (s.length() == 0) throw new LevelParseException("\"" + name + "\" is an empty string");
        return s;
    }
    public static String[] parseStrs(JsonObject obj, String name) {
        JsonArray arr = parseArr(obj, name);
        return parseStrs(arr, name);
    }
    public static String[] parseStrs(JsonArray arr, String name) {
        String[] strs = new String[arr.size()];
        try {
            for (int i = 0; i < strs.length; i++) {
                strs[i] = arr.getString(i);
                if (strs[i].length() == 0) throw new LevelParseException("\"" + name + "\" contains an empty string (index: " + i + ")");
            }
        }
        catch (ClassCastException e) { throw new LevelParseException("\"" + name + "\" array does not contain assignable strings"); }
        return strs;
    }
    public static int parseColour(JsonObject obj, String name) {
        String hexStr = parseStr(obj, name);
        if (hexStr.length() != 6) throw new LevelParseException("\"" + name + "\" is not a valid hexadecimal: length != 6 for \"" + hexStr + "\"");
        try { return Integer.valueOf(hexStr, 16); }
        catch (NumberFormatException e) { throw new LevelParseException("\"" + name + "\" is not a valid hexadecimal: NumberFormatException on \"" + hexStr + "\""); }
    }

    public static PVector parsePVec(JsonObject obj, String name) {
        JsonArray arr = parseArr(obj, name);
        if (arr.size() < 2) throw new LevelParseException("\"" + name + "\" cannot be a PVector: length < 2");
        float x = 0, y = 0, z = 0;
        try {
            if (arr.size() > 3) throw new LevelParseException("\"" + name + "\" cannot be a PVector: length > 3");
            else if (arr.size() == 3) z = (float) arr.getJsonNumber(2).doubleValue();
            y = (float) arr.getJsonNumber(1).doubleValue();
            x = (float) arr.getJsonNumber(0).doubleValue();
        }
        catch (ClassCastException e) { throw new LevelParseException("\"" + name + "\" is not an assignable number (double)"); }
        return new PVector(x, y, z);
    }

    public static RObjType parseRObjType(JsonObject obj, String name) {
        String typeStr = parseStr(obj, name);
        RObjType type = RObjType.parse(typeStr);
        if (type == null) throw new LevelParseException("\"" + name + "\" is not a valid RObjType: \"" + typeStr + "\" is not recognised");
        return type;
    }

    public static LColour parseLColour(JsonObject obj, String name) {
        String typeStr = parseStr(obj, name);
        LColour colour = LColour.parse(typeStr);
        if (colour == null) throw new LevelParseException("\"" + name + "\" is not a valid LColour: \"" + typeStr + "\" is not recognised");
        return colour;
    }

    public static void parseGeometryArr(JsonObject obj, String name, GObject parent) {
        JsonArray arr = parseArr(obj, name);
        PartFactory partFactory = new PartFactory(PartFactory.PhysicsPreset.GEOMETRY);
        int i = 0;
        try { for (; i < arr.size(); i++) parseGeometry(arr.getJsonObject(i), parent, partFactory); }
        catch (ClassCastException e) { throw new LevelParseException("parseGeometryArr(obj, " + name + ", parent), encountered an exception:\n\t\"" + name + "\" array does not contain assignable JsonObjects at index " + i); }
        catch (LevelParseException e) { throw new LevelParseException("parseGeometryArr(obj, " + name + ", parent), encountered an exception at index " + i + ":\n\t" + e.getLocalizedMessage()); }
    }

    public static void parseGeometry(JsonObject obj, GObject parent, PartFactory pF) {
        String type = parseStr(obj, "type").toUpperCase();
        PVector anchor = parsePVec(obj, "anchor");
        switch (type) {
            // Cannot allow shapes such as QUAD or ARC as they may be concave (not convex)
            // Cannot allow ELLIPSE/CIRCLE/POLYGON as they cannot (easily) have a primary/secondary counterpart
            //      ELLIPSE/CIRCLE/POLYGON must be defined as RObjects instead (so that they may utilise masking)
            case "RECT" ->
                pF.createRect(parent, anchor, parsePVec(obj, "dimensions"), parseFloat(obj, "orientation", 0f));
            case "TRIANGLE" ->
                pF.createTriangle(parent, anchor, parsePVec(obj, "vertex1"), parsePVec(obj, "vertex2"));
            default ->
                throw new LevelParseException("\"type\" of \"" + type + "\" is unknown");
        }
    }

    public static void parseRObjectAr(JsonObject obj, String name, GObject parent, List<RObject> roomObjects) {
        JsonArray arr = parseArr(obj, name);
        PartFactory partFactory = new PartFactory(PartFactory.PhysicsPreset.GEOMETRY);
        int i = 0;
        try { for (; i < arr.size(); i++)
            roomObjects.add(parseRObject(arr.getJsonObject(i), parent, partFactory));
        }
        catch (ClassCastException e) { throw new LevelParseException("parseGeometryArr(obj, " + name + ", parent), encountered an exception: \n\t\"" + name + "\" array does not contain assignable JsonObjects at index " + i); }
        catch (LevelParseException e) { throw new LevelParseException("parseGeometryArr(obj, " + name + ", parent), encountered an exception at index " + i + ":\n\t" + e.getLocalizedMessage()); }
    }

    public static RObject parseRObject(JsonObject obj, GObject parent, PartFactory pF) {
        RObjType type = parseRObjType(obj, "type");
        PVector anchor = parsePVec(obj, "anchor");
        int id = parseInt(obj, "id");

        return switch (type) {
            case GEOMETRY -> parseGeometryRObj(obj, parent, pF, anchor, id);
            case BLOCK -> parseBlock(obj, parent, pF, anchor, id);
            case DOOR -> parseDoor(obj, parent, pF, anchor, id);
            case LEVER -> parseLever(obj, parent, pF, anchor, id);
            case SPIKE -> parseSpike(obj, parent, pF, anchor, id);
            case PORTAL -> parsePortal(obj, parent, pF, anchor, id);
            case EXIT -> parseExit(obj, parent, pF, anchor, id);
        };
    }

    public static RObject parseGeometryRObj(JsonObject obj, GObject parent, PartFactory pF, PVector anchor, int id) {
        LColour colour = parseLColour(obj, "colour");

        String type = parseStr(obj, "gType");

        //TODO attach LColour and id

        pF.initPhysicsProperties(PartFactory.PhysicsPreset.GEOMETRY);
        return switch (type) {
            case "RECT" ->
                pF.createRect(parent, anchor, parsePVec(obj, "dimensions"),
                        parseFloat(obj, "orientation", 0f), colour, id);
            case "TRIANGLE" ->
                pF.createTriangle(parent, anchor, parsePVec(obj, "vertex1"),
                        parsePVec(obj, "vertex2"), colour, id);
            case "POLYGON" ->
                pF.createPolygon(parent, anchor, parsePVec(obj, "dimensions"),
                        parseInt(obj, "sides"), parseFloat(obj, "orientation", 0f), colour, id);
            case "ELLIPSE" ->
                pF.createEllipse(parent, anchor, parsePVec(obj, "dimensions"),
                        parseFloat(obj, "orientation", 0f), colour, id);
            case "CIRCLE" ->
                pF.createCircle(parent, anchor, parseFloat(obj, "radius"), colour, id);
            default ->
                throw new LevelParseException("\"gType\" of \"" + type + "\" is unknown");
        };
    }
    public static RObject parseBlock(JsonObject obj, GObject parent, PartFactory pF, PVector anchor, int id) {
        LColour colour = parseLColour(obj, "colour");
        PVector dimensions = parsePVec(obj, "dimensions");

        pF.initPhysicsProperties(PartFactory.PhysicsPreset.BLOCK);
        return pF.makeBlock(parent, anchor, dimensions, colour, id);
    }

    public static RObject parseDoor(JsonObject obj, GObject parent, PartFactory pF, PVector anchor, int id) {
        LColour colour = parseLColour(obj, "colour");
        PVector dimensions = parsePVec(obj, "dimensions");

        pF.initPhysicsProperties(PartFactory.PhysicsPreset.GEOMETRY);
        return pF.makeDoor(parent, anchor, dimensions, colour, id);
    }

    public static RObject parseLever(JsonObject obj, GObject parent, PartFactory pF, PVector anchor, int id) {
        LColour colour = parseLColour(obj, "colour");
        float orientation = parseFloat(obj, "orientation", 0f);
        int[] linkedTo = parseInts(obj, "linkedTo");

        pF.initPhysicsProperties(PartFactory.PhysicsPreset.CONTACT_ONLY);
        return pF.makeLever(parent, anchor, orientation, linkedTo, colour, id);
    }

    public static RObject parseSpike(JsonObject obj, GObject parent, PartFactory pF, PVector anchor, int id) {
        LColour colour = parseLColour(obj, "colour");
        float orientation = parseFloat(obj, "orientation", 0f);

        pF.initPhysicsProperties(PartFactory.PhysicsPreset.CONTACT_ONLY);
        return pF.makeSpike(parent, anchor, orientation, colour, id);
    }

    public static RObject parsePortal(JsonObject obj, GObject parent, PartFactory pF, PVector anchor, int id) {
        float width = parseFloat(obj, "width");
        float orientation = parseFloat(obj, "orientation", 0f);


        pF.initPhysicsProperties(PartFactory.PhysicsPreset.CONTACT_ONLY);
        return pF.makePortal(parent, anchor, width, orientation, id);
    }

    public static RObject parseExit(JsonObject obj, GObject parent, PartFactory pF, PVector anchor, int id) {
        pF.initPhysicsProperties(PartFactory.PhysicsPreset.CONTACT_ONLY);
        return pF.makeExit(parent);
    }

}
