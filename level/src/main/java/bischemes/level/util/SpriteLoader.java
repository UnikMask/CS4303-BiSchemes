package bischemes.level.util;

import processing.core.PGraphics;
import processing.core.PImage;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;


public final class SpriteLoader {

    private static PImage blockSymbol = null;
    private static PImage lever = null;
    private static PImage lockSymbol = null;
    private static PImage interactSymbol = null;
    private static PImage switchSymbol = null;
    private static PImage teleportSymbol = null;

    private static final String DIRECTORY = "sprites/";

    private static String getSpritePath(String filename) {
        return DIRECTORY + filename;
    }

    private static PImage loadImage(String file) {
        InputStream i = null;
        try {
            try {
                i = new FileInputStream(file);
            } catch (IOException ignored) {}
            if (i == null) i = new FileInputStream("game/" + file);
            PImage img = PImage.loadTGA(i);
            i.close();
            return img;
        }
        catch (IOException e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    public static PImage getBlockSymbol() {
        if (blockSymbol == null) {
            blockSymbol = loadImage(getSpritePath("BlockSymbol.tga"));
        }
        return blockSymbol;
    }

    public static PImage getLockSymbol() {
        if (lockSymbol == null) {
            lockSymbol = loadImage(getSpritePath("LockSymbol.tga"));
        }
        return lockSymbol;
    }

    public static PImage getLever() {
        if (lever == null) {
            lever = loadImage(getSpritePath("Lever.tga"));
        }
        return lever;
    }

    public static PImage getInteractSymbol() {
        if (interactSymbol == null) {
            interactSymbol = loadImage(getSpritePath("InteractSymbol.tga"));
        }
        return interactSymbol;
    }

    public static PImage getSwitchSymbol() {
        if (switchSymbol == null) {
            switchSymbol = loadImage(getSpritePath("SwitchSymbol.tga"));
        }
        return switchSymbol;
    }

    public static PImage getTeleportSymbol() {
        if (teleportSymbol == null) {
            teleportSymbol = loadImage(getSpritePath("TeleportSymbol.tga"));
        }
        return teleportSymbol;
    }


    private SpriteLoader() {}


}
