package bischemes.game;

import bischemes.level.Level;
import bischemes.level.Levels;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class LevelLoadTest {

    public static void main(String[] args) {
        Levels.loadLevels(true);
        Collection<Level> levels = Levels.getLevels().values();
        System.out.println("Successfully loaded " + levels.size() + " levels.");
    }

}
