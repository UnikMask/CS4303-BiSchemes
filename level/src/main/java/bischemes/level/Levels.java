package bischemes.level;

import bischemes.level.util.InvalidIdException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.stream.Stream;

/** Class to start the parsing of all Level objects and handle storing all those objects */
public final class Levels {

    /** Static collection of all parsed Level objects with their ids as keys in the HashMap (easy lookup)*/
    private static final HashMap<Integer, Level> levels = new HashMap<>();
    /** Default directory name for where level JSON files are stored */
    private final static String DEFAULT_LEVEL_DIR = "levels";
    /** Default name for level JSON files when the level is contained in a subdirectory*/
    private final static String DEFAULT_INFO_FILE = "info.json";

    private Levels() {}

    /**
     * Gets a Level with the provided id from the levels HashMap
     * @param id id of the Level to get
     * @return The Level or throws an InvalidIdException of the id does not have a Level
     */
    public static Level getLevel(int id) {
        if (idExists(id)) return levels.get(id);
        throw new InvalidIdException("getLevel(id), level with id " + id + " does not exist");
    }
    /** Returns a copy of the HashMap of all Level objects */
    public static HashMap<Integer, Level> getLevels() {
        HashMap<Integer, Level> levels = new HashMap<>(Levels.levels.size());
        for (Level level : Levels.levels.values()) levels.put(level.getId(), level);
        return levels;
    }

    /**
     * Checks whether a Level with the provided id exists
     * @param id id of the Level to look for
     * @return true if the id exists, otherwise false
     */
    public static boolean idExists(int id) {
        return levels.containsKey(id);
    }

    /**
     * Calls loadLevels() but uses the DEFAULT_DIRECTORY instead of a user provided directory
     * @param skipOnLoadFail if true, exceptions are caught and the Level parsing is skipped.
     *                       if false, exceptions stop all Level parsing
     */
    public static void loadLevels(boolean skipOnLoadFail) {
        loadLevels(skipOnLoadFail, DEFAULT_LEVEL_DIR);
    }
    /**
     * Parses all Level JSON files in the provided directory and loads them into the private Level collection
     * @param directory the directory to look for Level JSON files in
     * @param skipOnLoadFail if true, exceptions are caught and the Level parsing is skipped.
     *                       if false, exceptions stop all Level parsing
     */
    public static void loadLevels(boolean skipOnLoadFail, String directory) {
        try (Stream<Path> stream = Files.list(Paths.get(directory))){
            for(Path p : stream.toList()) {
                Level level;
                if (p.toFile().isFile()) level = Level.parseLevel(directory, p.toFile().getName(), skipOnLoadFail);
                else level = Level.parseLevel(p.toString(), DEFAULT_INFO_FILE, skipOnLoadFail);
                if (level == null) continue;

                if (idExists(level.getId())) {
                    if (skipOnLoadFail) System.out.println("");
                    else throw new InvalidIdException("\"id\" " + level.getId() + " from " + getLevel(level.getId()).getName() + " is repeated in " + level.getName());
                }
                else {
                    levels.put(level.getId(), level);
                    System.out.println("Loaded level from \"" + p + "\", id = " + level.getId() + ", name = " + level.getName());
                }
            }
        }catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

}
