package bischemes.level.util;

import bischemes.engine.GObject;
import bischemes.level.Room;
import bischemes.level.parts.Adjacency;
import bischemes.level.parts.PartFactory;
import bischemes.level.parts.RObjType;
import bischemes.level.parts.RObject;
import bischemes.level.parts.behaviour.*;
import processing.core.PVector;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

public final class JParser {

    private JParser(){}
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
    public static JsonArray parseArrOrNull(JsonObject obj, String name) {
        try { return parseArr(obj, name); }
        catch (LevelParseException e) { return null; }
    }
    public static int parseInt(JsonObject obj, String name) {
        try { return obj.getInt(name); }
        catch (NullPointerException e) { throw new LevelParseException("\"" + name + "\" does not have a mapping"); }
        catch (ClassCastException e) { throw new LevelParseException("\"" + name + "\" is not an assignable integer"); }
    }
    public static int parseInt(JsonObject obj, String name, int defaultValue) {
        try { return parseInt(obj, name); }
        catch (LevelParseException e) { return defaultValue; }
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
        try { return parseFloat(obj, name); }
        catch (LevelParseException e) { return defaultValue; }
    }
    public static double parseDouble(JsonObject obj, String name) {
        try { return obj.getJsonNumber(name).doubleValue(); }
        catch (NullPointerException e) { throw new LevelParseException("\"" + name + "\" does not have a mapping"); }
        catch (ClassCastException e) { throw new LevelParseException("\"" + name + "\" is not an assignable double"); }
    }
    public static double parseDouble(JsonObject obj, String name, double defaultValue) {
        try { return parseDouble(obj, name); }
        catch (LevelParseException e) { return defaultValue; }
    }
    public static boolean parseBoolean(JsonObject obj, String name) {
        try { return obj.getBoolean(name); }
        catch (NullPointerException e) { throw new LevelParseException("\"" + name + "\" does not have a mapping"); }
        catch (ClassCastException e) { throw new LevelParseException("\"" + name + "\" is not an assignable boolean"); }
    }
    public static boolean parseBoolean(JsonObject obj, String name, boolean defaultValue) {
        try { return parseBoolean(obj, name); }
        catch (LevelParseException e) { return defaultValue; }
    }
    public static Boolean parseBooleanOrNull(JsonObject obj, String name) {
        try { return parseBoolean(obj, name); }
        catch (LevelParseException e) { return null; }
    }
    public static String parseStr(JsonObject obj, String name) {
        String s;
        try { s = obj.getString(name); }
        catch (NullPointerException e) { throw new LevelParseException("\"" + name + "\" does not have a mapping"); }
        catch (ClassCastException e) { throw new LevelParseException("\"" + name + "\" is not an assignable string"); }
        if (s.length() == 0) throw new LevelParseException("\"" + name + "\" is an empty string");
        return s;
    }
    public static String parseStr(JsonObject obj, String name, String defaultValue) {
        try { return parseStr(obj, name); }
        catch (LevelParseException e) { return defaultValue; }
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

    public static PVector parsePVec(JsonObject obj, String name, PVector defaultValue) {
        try { return parsePVec(obj, name); }
        catch (LevelParseException e) { return defaultValue; }
    }

    public static PVector parsePVecOrNull(JsonObject obj, String name) {
        try { return parsePVec(obj, name); }
        catch (LevelParseException e) { return null; }
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
        PartFactory partFactory = new PartFactory();

        int i = 0;
        try { for (; i < arr.size(); i++) parseGeometry(arr.getJsonObject(i), parent, partFactory); }
        catch (ClassCastException e) { throw new LevelParseException("parseGeometryArr(obj, " + name + ", parent), encountered an exception:\n\t\"" + name + "\" array does not contain assignable JsonObjects at index " + i); }
        catch (LevelParseException e) { throw new LevelParseException("parseGeometryArr(obj, " + name + ", parent), encountered an exception at index " + i + ":\n\t" + e.getLocalizedMessage()); }
    }

    public static GObject parseGeometry(JsonObject obj, GObject parent, PartFactory pF) {
        String type = parseStr(obj, "type").toUpperCase();

        pF.setSurfaceProperties(
                parseDouble(obj, "restitution", 0.0),
                parseDouble(obj, "staticFriction", 1),
                parseDouble(obj, "dynamicFriction", 1));

        // Cannot allow shapes such as QUAD or ARC as they may be concave (not convex)
        // Cannot allow ELLIPSE/CIRCLE/POLYGON as they cannot (easily) have a primary/secondary counterpart
        //      ELLIPSE/CIRCLE/POLYGON must be defined as RObjects instead (so that they may utilise masking)
        PVector corner = parsePVecOrNull(obj, "corner");
        if (corner == null) {
            PVector anchor = parsePVec(obj, "anchor");
            return switch (type) {
                case "RECT" ->
                        pF.createRect(parent, anchor, parsePVec(obj, "dimensions"),
                                parseFloat(obj, "orientation", 0f));
                case "TRIANGLE" ->
                        pF.createTriangle(parent, anchor, parsePVec(obj, "vertex1"),
                                parsePVec(obj, "vertex2"), parsePVec(obj, "vertex3"));
                case "TRAPEZIUM" ->
                        pF.createTrapezium(parent, anchor, parseFloat(obj, "orientation", 0f),
                                parseFloat(obj, "height", 1f), parsePVec(obj, "widths"));
                default ->
                        throw new LevelParseException("\"type\" of \"" + type + "\" is unknown");
            };
        }
        else {
            return switch (type) {
                case "RECT" ->
                        pF.createCornerRect(parent, corner, parsePVec(obj, "dimensions"),
                                parseFloat(obj, "orientation", 0f));
                case "TRIANGLE" ->
                        pF.createCornerTriangle(parent, corner, parsePVec(obj, "vertex1"),
                                parsePVec(obj, "vertex2"));
                case "TRAPEZIUM" ->
                        pF.createCornerTrapezium(parent, corner, parseFloat(obj, "orientation", 0f),
                                parseFloat(obj, "height", 1f), parsePVec(obj, "widths"));
                default ->
                        throw new LevelParseException("\"type\" of \"" + type + "\" is unknown");
            };
        }

    }

    public static void parseRObjectArr(JsonObject obj, String name, GObject parent, List<RObject> roomObjects) {
        JsonArray arr = parseArr(obj, name);
        PartFactory partFactory = new PartFactory();
        int i = 0;
        try {
            for (; i < arr.size(); i++)
                roomObjects.add(parseRObject(arr.getJsonObject(i), parent, partFactory));
        }
        catch (ClassCastException e) {
            System.out.println("_______" + e.getLocalizedMessage());
            throw new LevelParseException("parseRObjectArr(obj, " + name + ", parent), encountered an exception: \n\t\"" + name + "\" array does not contain assignable JsonObjects at index " + i); }
        catch (LevelParseException e) {
            System.out.println("_______" + e.getLocalizedMessage());
            throw new LevelParseException("parseRObjectArr(obj, " + name + ", parent), encountered an exception at index " + i + ":\n\t" + e.getLocalizedMessage()); }
    }

    public static RObject parseRObject(JsonObject obj, GObject parent, PartFactory pF) {
        RObjType type = parseRObjType(obj, "type");
        int id = parseInt(obj, "id");

        if (type == RObjType.EXIT)
            return parseExit(obj, parent, pF, id);

        PVector anchor = parsePVecOrNull(obj, "corner");
        if (anchor == null)
            anchor = parsePVec(obj, "anchor");
        else
            anchor.z = 1;

        pF.setSurfaceProperties(
                parseDouble(obj, "restitution", 0),
                parseDouble(obj, "staticFriction", 1),
                parseDouble(obj, "dynamicFriction", 1));

        return switch (type) {
            case GEOMETRY -> parseGeometryRObj(obj, parent, pF, anchor, id, true);
            case BLOCK -> parseBlock(obj, parent, pF, anchor, id);
            case DOOR -> parseDoor(obj, parent, pF, anchor, id);
            case LEVER -> parseLever(obj, parent, pF, anchor, id);
            case SPIKE -> parseSpike(obj, parent, pF, anchor, id);
            case PORTAL -> parsePortal(obj, parent, pF, anchor, id);
            case CUSTOM -> parseCustom(obj, parent, pF, anchor, id);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    public static RObject parseGeometryRObj(JsonObject obj, GObject parent, PartFactory pF, PVector anchor, int id,
                                            boolean resetRB) {
        LColour colour = parseLColour(obj, "colour");
        String type = parseStr(obj, "gType");

        if (resetRB) pF.initRBGeometry();

        if (anchor.z == 1) {
            return switch (type) {
                case "RECT" ->
                        pF.createCornerRect(parent, anchor, parsePVec(obj, "dimensions"),
                                parseFloat(obj, "orientation", 0f), colour, id);
                case "TRIANGLE" ->
                        pF.createCornerTriangle(parent, anchor, parsePVec(obj, "vertex1"),
                                parsePVec(obj, "vertex2"), colour, id);
                case "TRAPEZIUM" ->
                        pF.createCornerTrapezium(parent, anchor, parseFloat(obj, "orientation", 0f),
                                parseFloat(obj, "height", 1f), parsePVec(obj, "widths"), colour, id);
                default ->
                        throw new LevelParseException("\"gType\" of \"" + type + "\" is unknown when defining by corner");
            };
        }
        else {
            return switch (type) {
                case "RECT" ->
                        pF.createRect(parent, anchor, parsePVec(obj, "dimensions"),
                                parseFloat(obj, "orientation", 0f), colour, id);
                case "TRIANGLE" ->
                        pF.createTriangle(parent, anchor, parsePVec(obj, "vertex1"),
                                parsePVec(obj, "vertex2"), parsePVec(obj, "vertex3"), colour, id);
                case "POLYGON" ->
                        pF.createPolygon(parent, anchor, parsePVec(obj, "dimensions"),
                                parseInt(obj, "sides"), parseFloat(obj, "orientation", 0f), colour, id);
                case "ELLIPSE" ->
                        pF.createEllipse(parent, anchor, parsePVec(obj, "dimensions"),
                                parseFloat(obj, "orientation", 0f), colour, id);
                case "CIRCLE" ->
                        pF.createCircle(parent, anchor, parseFloat(obj, "radius"), colour, id);
                case "TRAPEZIUM" ->
                        pF.createTrapezium(parent, anchor, parseFloat(obj, "orientation", 0f),
                                parseFloat(obj, "height", 1f), parsePVec(obj, "widths"), colour, id);
                default ->
                        throw new LevelParseException("\"gType\" of \"" + type + "\" is unknown");
            };
        }
    }
    public static RObject parseBlock(JsonObject obj, GObject parent, PartFactory pF, PVector anchor, int id) {
        LColour colour = parseLColour(obj, "colour");
        PVector dimensions = parsePVec(obj, "dimensions");
        boolean initState = parseBoolean(obj, "initState", false);
        double mass = parseDouble(obj, "mass", 1);
        if (anchor.z == 1) return pF.makeCornerBlock(parent, anchor, dimensions, initState, mass, colour, id);
        else return pF.makeBlock(parent, anchor, dimensions, initState, mass, colour, id);
    }

    public static RObject parseDoor(JsonObject obj, GObject parent, PartFactory pF, PVector anchor, int id) {
        LColour colour = parseLColour(obj, "colour");
        PVector dimensions = parsePVec(obj, "dimensions");
        boolean initState = parseBoolean(obj, "initState", false);
        if (anchor.z == 1) return pF.makeCornerDoor(parent, anchor, dimensions, initState, colour, id);
        else return pF.makeDoor(parent, anchor, dimensions, initState, colour, id);
    }

    public static RObject parseLever(JsonObject obj, GObject parent, PartFactory pF, PVector anchor, int id) {
        if (anchor.z == 1) throw new LevelParseException("cannot use \"corner\" parameter in Lever definition");
        LColour colour = parseLColour(obj, "colour");
        float orientation = parseFloat(obj, "orientation", 0f);
        int[] linkedTo = parseInts(obj, "linkedTo");
        return pF.makeLever(parent, anchor, orientation, linkedTo, colour, id);
    }

    public static RObject parseSpike(JsonObject obj, GObject parent, PartFactory pF, PVector anchor, int id) {
        if (anchor.z == 1) throw new LevelParseException("cannot use \"corner\" parameter in Spike definition");
        LColour colour = parseLColour(obj, "colour");
        float orientation = parseFloat(obj, "orientation", 0f);
        int length = parseInt(obj, "length", 1);
        if(length < 1)
            throw new LevelParseException("\"length\" of is invalid for " + length + ", length of SPIKE cannot be less than 1");
        return pF.makeSpike(parent, anchor, orientation, length, colour, id);
    }

    public static RObject parsePortal(JsonObject obj, GObject parent, PartFactory pF, PVector anchor, int id) {
        if (anchor.z == 1) throw new LevelParseException("cannot use \"corner\" parameter in Portal definition");
        int width = parseInt(obj, "width", 1);

        boolean initialState = parseBoolean(obj, "startOpen", true);
        boolean oneWay = parseBoolean(obj, "isOneWay", false);

        if (oneWay) {
            LColour colour = parseLColour(obj, "colour");
            String side = parseStr(obj, "side");
            boolean isVertical = false;
            boolean flipFace = false;

            switch (side.toUpperCase()) {
                case "LEFT" :
                    break;
                case "RIGHT" :
                    flipFace = true;
                    break;
                case "TOP" :
                    isVertical = true;
                    break;
                case "BOTTOM" :
                    isVertical = true;
                    flipFace = true;
                    break;
                default :
                    throw new LevelParseException("\"side\" of \"" + side + "\" is unknown");
            }

            return pF.makeOnewayPortal(parent, anchor, width, isVertical, flipFace, initialState, colour, id);
        }
        else {
            boolean isVertical = parseBoolean(obj, "isVertical");

            LColour colour1;
            if (isVertical) colour1 = parseLColour(obj, "topColour");
            else colour1 = parseLColour(obj, "leftColour");

            return pF.makePortal(parent, anchor, width, isVertical, initialState, colour1, id);

        }
    }

    public static RObject parseExit(JsonObject obj, GObject parent, PartFactory pF, int id) {
        PVector range = parsePVec(obj, "range");

        if (range.x < 0)
            throw new LevelParseException("\"range\" start value of [" + range.x + ", " + range.y + "] is invalid. " +
                    "Range values cannot extend below 0");
        if (range.x >= range.y)
            throw new LevelParseException("\"range\" start value of [" + range.x + ", " + range.y + "] is invalid. " +
                    "Range start values cannot be greater than range end values");

        String side = parseStr(obj, "side");

        boolean isVertical = false;
        boolean zeroAxis = false;

        switch (side.toUpperCase()) {
            case "LEFT" :
                break;
            case "RIGHT" :
                zeroAxis = true;
                break;
            case "TOP" :
                isVertical = true;
                break;
            case "BOTTOM" :
                isVertical = true;
                zeroAxis = true;
                break;
            default :
                throw new LevelParseException("\"side\" of \"" + side + "\" is unknown");
        }

        float maxBound = (isVertical) ? Room.getRoom(parent).getDimensions().x : Room.getRoom(parent).getDimensions().y;
        if (range.y > maxBound)
            throw new LevelParseException("\"range\" start value of [" + range.x + ", " + range.y + "] is invalid. " +
                    "Range values cannot extend past side length (length = " + maxBound + " for " + side + " of room " +
                    "(id = " + Room.getRoom(parent).getId() + "))");

        return pF.makeExit(parent, range, isVertical, zeroAxis, id);
    }

    public static RObject parseCustom(JsonObject obj, GObject parent, PartFactory pF, PVector anchor, int id) {

        String rbType = parseStr(obj, "rbType", "GEOMETRY");

        switch (rbType.toUpperCase()) {
            case "GEOMETRY" -> pF.initRBGeometry();
            case "NO COLLISION" -> pF.initRBNoCollision();
            case "ROTATEABLE" -> pF.initRBRotateable(parseDouble(obj, "mass",1));
            case "MOVEABLE" -> pF.initRBMoveable(parseDouble(obj, "mass", 1));
            case "BLOCK" -> pF.initRBBlock(parseDouble(obj, "mass", 1));
            default ->
                    throw new LevelParseException("\"rbType\" of \"" + rbType + "\" is unknown");
        }

        RObject customObj = parseGeometryRObj(obj, parent, pF, anchor, id, false);
        parseBehaviours(obj, "behaviours", customObj);

        return null;
    }

    public static List<Behaviour> parseBehaviours(JsonObject obj, String name, RObject customObj) {
        List<Behaviour> behaviours = new ArrayList<>();
        JsonArray arr = parseArrOrNull(obj, name);
        if (arr == null) return new ArrayList<>(behaviours);

        int i = 0;
        try {
            for (; i < arr.size(); i++)
                behaviours.add(parseBehaviour(obj, customObj));
        }
        catch (ClassCastException e) { throw new LevelParseException("parseBehaviours(obj, " + name + ", parent), encountered an exception: \n\t\"" + name + "\" array does not contain assignable JsonObjects at index " + i); }
        catch (LevelParseException e) { throw new LevelParseException("parseBehaviours(obj, " + name + ", parent), encountered an exception at index " + i + ":\n\t" + e.getLocalizedMessage()); }
        return behaviours;
    }

    public static Behaviour parseBehaviour(JsonObject obj, RObject customObj) {
        String type = parseStr(obj, "bType");
        return switch (type.toUpperCase()) {
            case "BHITKILL" -> BHitKill.assign(customObj);
            case "BHITSTATESWITCH" -> BHitStateSwitch.assign(customObj);
            case "BHITTELEPORT" -> parseBHitTeleport(obj, customObj);
            case "BINTERACTTELEPORT" -> parseBInteractTeleport(obj, customObj);
            case "BINTERACTSTATESWITCH" -> parseBInteractStateSwitch(obj, customObj);
            case "BSTATEBLOCK" -> parseBStateBlock(obj, customObj);
            case "BSTATEFLIP" -> parseBStateFlip(obj, customObj);
            case "BSTATEHIDE" -> parseBStateHide(obj, customObj);
            case "BSTATESWAPCOLOUR" -> parseBStateSwapColour(obj, customObj);
            case "BSTATESWITCHSTATES" -> parseBStateSwitchStates(obj, customObj);
            case "BUPDATEFOLLOWPATH" -> parseBUpdateFollowPath(obj, customObj);
            case "BUPDATETIMER" -> parseBUpdateTimer(obj, customObj);
            default -> throw new LevelParseException("\"bType\" of \"" + type + "\" is unknown");
        };
    }

    public static BHitTeleport parseBHitTeleport(JsonObject obj, RObject customObj) {

        PVector link = parsePVec(obj, "destination");
        boolean swapColour = parseBoolean(obj, "swapColour", false);
        int roomId = parseInt(obj, "destRoomId", -1);

        BHitTeleport b;

        if (roomId == -1)
            b = BHitTeleport.assign(customObj, link, swapColour);
        else if (roomId >= 0) {
            Room destination = Room.getRoom(customObj).getLevel().getRoom(roomId);
            b = BHitTeleport.assign(customObj, destination, link, swapColour);
        }
        else
            throw new InvalidIdException("\"destRoomId\" of " + roomId + " is invalid, ids cannot be negative");


        boolean flipGravity = parseBoolean(obj, "flipGravity", false);
        b.configureGravityFlip(flipGravity);

        Boolean activeState = parseBooleanOrNull(obj, "activeOnState");
        if (activeState != null) b.setActiveOnState(activeState);

        if (parseBoolean(obj, "playerOnly"))
            b.makePlayerOnly();

        if (parseBoolean(obj, "teleportIcon", false))
            b.addTeleportIcon(parsePVec(obj, "iconSize", new PVector(1, 1)));

        Boolean offsetX = parseBooleanOrNull(obj, "offsetX");
        Boolean offsetY = parseBooleanOrNull(obj, "offsetY");
        Boolean mirrorY = parseBooleanOrNull(obj, "mirrorY");
        Boolean mirrorX = parseBooleanOrNull(obj, "mirrorX");
        if (offsetX != null || offsetY != null || mirrorX != null || mirrorY != null) {
            if (offsetX == null)
                throw new LevelParseException("\"offsetX\" is required in any offset or mirror booleans are provided");
            if (offsetY == null)
                throw new LevelParseException("\"offsetY\" is required in any offset or mirror booleans are provided");
            if (mirrorX == null)
                throw new LevelParseException("\"mirrorX\" is required in any offset or mirror booleans are provided");
            if (mirrorY == null)
                throw new LevelParseException("\"mirrorY\" is required in any offset or mirror booleans are provided");
            b.configureOffset(offsetX, offsetY, mirrorX, mirrorY);
        }

        return b;
    }

    public static BInteractTeleport parseBInteractTeleport(JsonObject obj, RObject customObj) {
        PVector link = parsePVec(obj, "destination");
        boolean swapColour = parseBoolean(obj, "swapColour", false);
        int roomId = parseInt(obj, "destRoomId", -1);

        float r = parseFloat(obj, "radius", -1);
        float x = 0;
        float y = 0;

        boolean useXY = r == -1;
        if (useXY) {
            x = parseFloat(obj, "xDist");
            y = parseFloat(obj, "yDist");
        }
        else if (r < 0)
            throw new LevelParseException("\"radius\" of BInteractTeleport cannot be negative");

        BInteractTeleport b;

        if (roomId == -1) {
            if (useXY) b = BInteractTeleport.assign(customObj, x, y, link, swapColour);
            else b = BInteractTeleport.assign(customObj, r, link, swapColour);
        }
        else if (roomId >= 0) {
            Room destination = Room.getRoom(customObj).getLevel().getRoom(roomId);
            if (useXY) b = BInteractTeleport.assign(customObj, x, y, destination, link, swapColour);
            else b = BInteractTeleport.assign(customObj, r, destination, link, swapColour);
        }
        else
            throw new InvalidIdException("\"destRoomId\" of " + roomId + " is invalid, ids cannot be negative");

        boolean flipGravity = parseBoolean(obj, "flipGravity", false);
        b.configureGravityFlip(flipGravity);

        Boolean activeState = parseBooleanOrNull(obj, "activeOnState");
        if (activeState != null) b.setActiveOnState(activeState);

        if (parseBoolean(obj, "playerOnly"))
            b.makePlayerOnly();

        if (parseBoolean(obj, "teleportIcon", false))
            b.addTeleportIcon(parsePVec(obj, "iconSize", new PVector(1, 1)));

        if (parseBoolean(obj, "addIndicator", false)){
            PVector indicatorOffset = parsePVec(obj, "indicatorOffset", new PVector(0, 0));
            String type = parseStr(obj, "indicatorType", "INTERACT");
            switch (type.toUpperCase()) {
                case "INTERACT" -> b.addIndicator(indicatorOffset);
                case "TELEPORT" -> b.addTeleportIndicator(indicatorOffset);
                case "COLOURSWITCH" -> b.addColourSwitchIndicator(indicatorOffset);
                default ->
                        throw new LevelParseException("\"indicatorType\" of \"" + type + "\" is unknown");
            }
        }

        Boolean offsetX = parseBooleanOrNull(obj, "offsetX");
        Boolean offsetY = parseBooleanOrNull(obj, "offsetY");
        Boolean mirrorY = parseBooleanOrNull(obj, "mirrorY");
        Boolean mirrorX = parseBooleanOrNull(obj, "mirrorX");
        if (offsetX != null || offsetY != null || mirrorX != null || mirrorY != null) {
            if (offsetX == null)
                throw new LevelParseException("\"offsetX\" is required in any offset or mirror booleans are provided");
            if (offsetY == null)
                throw new LevelParseException("\"offsetY\" is required in any offset or mirror booleans are provided");
            if (mirrorX == null)
                throw new LevelParseException("\"mirrorX\" is required in any offset or mirror booleans are provided");
            if (mirrorY == null)
                throw new LevelParseException("\"mirrorY\" is required in any offset or mirror booleans are provided");
            b.configureOffset(offsetX, offsetY, mirrorX, mirrorY);
        }

        return b;
    }

    public static BInteractStateSwitch parseBInteractStateSwitch(JsonObject obj, RObject customObj) {
        float r = parseFloat(obj, "radius", -1);
        BInteractStateSwitch b;
        if (r == -1) {
            float x = parseFloat(obj, "xDist");
            float y = parseFloat(obj, "yDist");
            b = BInteractStateSwitch.assign(customObj, x, y);
        }
        else if (r > 0)
            b =  BInteractStateSwitch.assign(customObj, r);
        else
            throw new LevelParseException("\"radius\" of BInteractStateSwitch cannot be negative");
        PVector indicator = parsePVecOrNull(obj, "indicatorOffset");
        if (indicator != null)
            b.addIndicator(indicator);
        return b;
    }
    public static BStateBlock parseBStateBlock(JsonObject obj, RObject customObj) {
        boolean state = parseBoolean(obj, "initialState", false);
        float iconSize = parseFloat(obj, "iconSize", 1f);
        if (iconSize < 0)
            throw new LevelParseException("\"iconSize\" of parseBStateBlock cannot be negative");
        return BStateBlock.assign(customObj, state, new PVector(iconSize, iconSize));
    }
    public static BStateFlip parseBStateFlip(JsonObject obj, RObject customObj) {
        boolean state = parseBoolean(obj, "initialState", false);
        return BStateFlip.assign(customObj, state);
    }
    public static BStateHide parseBStateHide(JsonObject obj, RObject customObj) {
        boolean state = parseBoolean(obj, "initialState", false);
        return BStateHide.assign(customObj, state);
    }
    public static BStateSwapColour parseBStateSwapColour(JsonObject obj, RObject customObj) {
        float iconSize = parseFloat(obj, "iconSize", -1f);
        BStateSwapColour b = BStateSwapColour.assign(customObj);
        if (iconSize != -1) {
            if (iconSize < 0)
                throw new LevelParseException("\"iconSize\" of BStateSwapColour cannot be negative");
            b.addSwitchIcon(new PVector(iconSize, iconSize));
        }
        return b;

    }
    public static BStateSwitchStates parseBStateSwitchStates(JsonObject obj, RObject customObj) {
        int[] linkedIDs = parseInts(obj, "linkedTo");
        for (int id : linkedIDs)
            if (id == customObj.getId())
                throw new LevelParseException("cannot link a BStateSwitchStates behaviour to the parent RObject");
        return BStateSwitchStates.assign(customObj, linkedIDs);
    }
    public static BUpdateFollowPath parseBUpdateFollowPath(JsonObject obj, RObject customObj) {
        //TODO
        return null;
    }
    public static BUpdateTimer parseBUpdateTimer(JsonObject obj, RObject customObj) {

        int period = parseInt(obj, "period", -1);
        int offset = parseInt(obj, "offset", -1);

        BUpdateTimer b;
        if (period > 0) {
            if (offset == -1) b = BUpdateTimer.assign(customObj, period);
            else if (offset > 0) b = BUpdateTimer.assign(customObj, period, offset);
            else
                throw new LevelParseException("\"offset\" of parseBUpdateTimer cannot be negative");
        }
        else if (period == -1) {
            int[] periods = parseInts(obj, "periods");
            if (offset == -1) b = BUpdateTimer.assign(customObj, periods);
            else if (offset > 0) b = BUpdateTimer.assign(customObj, periods, offset);
            else
                throw new LevelParseException("\"offset\" of parseBUpdateTimer cannot be negative");
        }
        else
            throw new LevelParseException("\"period\" of parseBUpdateTimer cannot be negative");

        Boolean activeState = parseBooleanOrNull(obj, "activeOnState");
        if (activeState != null) b.setActiveOnState(activeState);
        return b;
    }

    public static void parseAdjacencyArr(JsonObject obj, String name, GObject parent, List<Adjacency> adjacencies) {
        JsonArray arr = parseArr(obj, name);
        int i = 0;
        try {
            for (; i < arr.size(); i++)
                adjacencies.add(parseAdjacency(arr.getJsonObject(i), parent));
        }
        catch (ClassCastException e) {
            throw new LevelParseException("parseAdjacencyArr(obj, " + name + ", parent), encountered an exception: \n\t\"" + name + "\" array does not contain assignable JsonObjects at index " + i); }
        catch (LevelParseException e) {
            throw new LevelParseException("parseAdjacencyArr(obj, " + name + ", parent), encountered an exception at index " + i + ":\n\t" + e.getLocalizedMessage()); }
    }

    public static Adjacency parseAdjacency(JsonObject obj, GObject parent) {
        int id = parseInt(obj, "id");
        LColour colour = parseLColour(obj, "colour");

        PVector range = parsePVec(obj, "range");

        if (range.x < 0)
            throw new LevelParseException("\"range\" start value of [" + range.x + ", " + range.y + "] is invalid. " +
                    "Range values cannot extend below 0");
        if (range.x >= range.y)
            throw new LevelParseException("\"range\" start value of [" + range.x + ", " + range.y + "] is invalid. " +
                    "Range start values cannot be greater than range end values");

        String side = parseStr(obj, "side");

        boolean isVertical = false;
        boolean zeroAxis = false;

        switch (side.toUpperCase()) {
            case "LEFT" :
                break;
            case "RIGHT" :
                zeroAxis = true;
                break;
            case "TOP" :
                isVertical = true;
                break;
            case "BOTTOM" :
                isVertical = true;
                zeroAxis = true;
                break;
            default :
                throw new LevelParseException("\"side\" of \"" + side + "\" is unknown");
        }

        float maxBound = (isVertical) ? Room.getRoom(parent).getDimensions().x : Room.getRoom(parent).getDimensions().y;
        if (range.y > maxBound)
            throw new LevelParseException("\"range\" start value of [" + range.x + ", " + range.y + "] is invalid. " +
                    "Range values cannot extend past side length (length = " + maxBound + " for " + side + " of room " +
                    "(id = " + Room.getRoom(parent).getId() + "))");

        Adjacency adjacency;

        int roomId = parseInt(obj, "destRoomId", -1);
        if (roomId == -1)
            adjacency = new Adjacency(parent, range, isVertical, zeroAxis, id, colour);
        else if (roomId >= 0) {
            int linkId = parseInt(obj, "linkId");
            if (linkId < 0)
                throw new InvalidIdException("\"linkId\" of " + id + " is invalid, ids cannot be negative");
            adjacency = new Adjacency(parent, range, roomId, linkId, isVertical, zeroAxis, id, colour);
        }
        else
            throw new InvalidIdException("\"destRoomId\" of " + id + " is invalid, ids cannot be negative");

        return adjacency;
    }

}
