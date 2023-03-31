package bischemes.level;

//import processing.core.PGraphics;
//import processing.core.PVector;

import processing.core.PVector;

import javax.json.JsonArray;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class LevelParser {

    private final static String DEFAULT_LEVEL_DIR = "levels";
    private final static String DEFAULT_INFO_FILE = "info.json";

    public static int[] hexToRGB(String hex) {
        if (hex.length() != 7 || hex.charAt(0) != '#') return null;
        return new int[]{
                Integer.parseInt(hex.substring(1,3), 16),
                Integer.parseInt(hex.substring(3,5), 16),
                Integer.parseInt(hex.substring(5,7), 16)
        };
    }

    public static PVector[] extractPVectors(JsonArray jsonArray) {
        if (jsonArray.size() < 2 || jsonArray.size() % 2 != 0) return null;
        PVector[] pVectors = new PVector[jsonArray.size() / 2];
        for (int i = 0; i < pVectors.length; i++) {
            pVectors[i] = new PVector(
                jsonArray.getJsonNumber(i * 2).numberValue().floatValue(),
                jsonArray.getJsonNumber(1 + (i * 2)).numberValue().floatValue()
            );
        }
        return pVectors;
    }


    private static List<Level> parseLevels() {
        return parseLevels(DEFAULT_LEVEL_DIR);
    }
    private static List<Level> parseLevels(String directory) {
        List<Level> levels = new ArrayList<>();
        try (Stream<Path> stream = Files.list(Paths.get(directory))){
            for(Path p : stream.toList()) {
                levels.add(Level.parseLevel(p.toString(), DEFAULT_INFO_FILE));
            }
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
        return levels;
    }

    public static void main(String[] args) {
        List<Level> levels = parseLevels();
        System.out.println("Successfully loaded " + levels.size() + " levels");
    }
}