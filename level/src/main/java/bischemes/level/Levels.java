package bischemes.level;

import bischemes.level.util.InvalidIdException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.stream.Stream;
public final class Levels {

    private static final HashMap<Integer, Level> levels = new HashMap<>();

    private final static String DEFAULT_LEVEL_DIR = "levels";
    private final static String DEFAULT_INFO_FILE = "info.json";

    private Levels() {}

    public static Level getLevel(int id) {
        if (idExists(id)) return levels.get(id);
        throw new InvalidIdException("getLevel(id), level with id " + id + " does not exist");
    }
    public static HashMap<Integer, Level> getLevels() {
        return levels;
    }
    public static boolean idExists(int id) {
        return levels.containsKey(id);
    }

    public static void loadLevels(boolean skipOnLoadFail) {
        loadLevels(skipOnLoadFail, DEFAULT_LEVEL_DIR);
    }
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


    public static void main(String[] args) {
        loadLevels(true);
        System.out.println("Successfully loaded " + levels.size() + " levels");
    }
}