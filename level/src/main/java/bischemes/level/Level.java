package bischemes.level;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javax.json.*;

public class Level {

    private String displayName;

    // TODO add checks to make sure id is unique across all levels (probably implement this in LevelParser)
    // TODO make final
    private int id;

    // IDs of levels required for completion before this level can be started
    // I'm currently undecided whether the list is conjunctive or disjunctive (i.e. complete all or complete 1 of the prerequisite levels
    private int[] prerequisites;

    // The two colour schemes for the level, defined as RGB values (but read in as hex)
    // I've distinctly separated the two colours into a 'primary'/'secondary' but 'colour1' and 'colour2' is probably a better alternative
    private int[] colourPrimary;
    private int[] colourSecondary;

    // Sequence of individual rooms, the intention is entering the exit portal of one room puts the player into the next room
    private Room[] rooms;

    private Level(){}



    public static Level parseLevel(String levelDir, String infoFile) {
        BufferedInputStream in;
        JsonReader jsonReader;
        String fullFilePath = levelDir + "\\" + infoFile;
        try {
            in = new BufferedInputStream(new FileInputStream(fullFilePath));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        jsonReader = Json.createReader(in);
        JsonObject levelJson = jsonReader.readObject();
        Level level = new Level();
        try {
            level.displayName = levelJson.getString("displayName");
            level.id = levelJson.getInt("id");
            level.colourPrimary = LevelParser.hexToRGB(levelJson.getString("colourPrimary"));
            level.colourSecondary = LevelParser.hexToRGB(levelJson.getString("colourSecondary"));

            JsonArray prerequisites = levelJson.getJsonArray("prerequisites");
            level.prerequisites = new int[prerequisites.size()];
            for (int i = 0; i < level.prerequisites.length; i++) {
                level.prerequisites[i] = prerequisites.getInt(i);
            }

            JsonArray rooms = levelJson.getJsonArray("rooms");
            if (rooms.size() == 0)
                throw new RuntimeException("Empty Level - " + fullFilePath + " has no rooms");
            level.rooms = new Room[rooms.size()];
            for (int i = 0; i < level.rooms.length; i++) {
                Room room = Room.parseRoom(levelDir + "\\" + rooms.getString(i));
                level.rooms[i] = room;
            }

        } catch (NullPointerException | ClassCastException e ) {
            throw new RuntimeException(e);
        }

        return level;
    }

}
