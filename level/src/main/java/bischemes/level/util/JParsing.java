package bischemes.level.util;

import bischemes.engine.GObject;
import bischemes.engine.VisualAttribute;
import bischemes.engine.VisualUtils;
import processing.core.PVector;

import javax.json.JsonArray;
import javax.json.JsonObject;

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

    //todo add method to iteratitvly add GObjects as child to a provided one

    //todo add sub exception catching to help geometry parsing

    public static void parseGeometryArr(JsonObject obj, String name, GObject parent) {
        JsonArray arr = parseArr(obj, name);
        int i = 0;
        try { for (; i < arr.size(); i++) parseGeometry(arr.getJsonObject(i), parent); }
        catch (ClassCastException e) { throw new LevelParseException("\"" + name + "\" array does not contain assignable JsonObjects at index " + i); }
        catch (LevelParseException e) { throw new LevelParseException("parseGeometryArr(obj, " + name + ", parent), encountered an exception at index " + i + ":\n\t" + e.getLocalizedMessage()); }
    }

    public static void parseGeometry(JsonObject obj, GObject parent) {
        PVector anchor = parsePVec(obj, "anchor");
        String type = parseStr(obj, "type").toUpperCase();
        GObject geometry = new GObject(parent, anchor, 0);
        VisualAttribute visualAttribute = switch (type) {
            // Can't include QUAD or ARC as both may be concave (not convex)
            // Can't include ELLIPSE as VisualAttribute only works for polygons
            case "RECT" -> parseRect(obj);
            case "TRIANGLE" -> parseTriangle(obj);
            case "LINE" -> parseLine(obj);
            case "POLYGON" -> parsePolygon(obj);
            default -> throw new LevelParseException("\"type\" of \"" + type + "\" is unknown");
        };
        geometry.addVisualAttributes(visualAttribute);
    }

    public static VisualAttribute parseRect(JsonObject obj) {
        return VisualUtils.makeRect(
                parsePVec(obj, "dimensions"),
                0x000000ff);
    }
    public static VisualAttribute parseTriangle(JsonObject obj) {
        return VisualUtils.makeTriangle(
                new PVector(),
                parsePVec(obj, "vertex1"),
                parsePVec(obj, "vertex2"),
                0x000000ff);
    }
    public static VisualAttribute parseLine(JsonObject obj) {
        return VisualUtils.makeEdge(
                new PVector(),
                parsePVec(obj, "vertex"),
                0x000000ff);
    }
    public static VisualAttribute parsePolygon(JsonObject obj) {
        return VisualUtils.makeUntexturedPolygon(
                parsePVec(obj, "dimensions"),
                parseInt(obj, "sides"),
                parseFloat(obj, "baseAngle"),
                new PVector(),
                0x000000ff);
    }

}
