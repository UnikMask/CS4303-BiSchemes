package bischemes.level.util;

import processing.core.PImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

// ----------------------------------------------------------------
// A temporary class so that I can load PImages for use in RObjects
// ----------------------------------------------------------------
public final class SpriteLoader {


    private static PImage blockSymbol = null;
    private static PImage lever = null;
    private static PImage lockSymbol = null;

    private static PImage loadImage(String file) {
        try {
            InputStream i = new FileInputStream(file);
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
            blockSymbol = loadImage("sprites/BlockSymbol.tga");
        }
        return blockSymbol;
    }

    public static PImage getLockSymbol() {
        if (lockSymbol == null) {
            lockSymbol = loadImage("sprites/LockSymbol.tga");
        }
        return lockSymbol;
    }

    public static PImage getLever() {
        if (lever == null) {
            lever = loadImage("sprites/Lever.tga");
        }
        return lever;
    }



    private SpriteLoader() {}


}