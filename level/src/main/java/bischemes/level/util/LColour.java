package bischemes.level.util;

public enum LColour {
    PRIMARY,
    SECONDARY;

    public LColour parse(String s) {
        return switch (s.toUpperCase()) {
            case "PRIMARY" -> PRIMARY;
            case "SECONDARY" -> SECONDARY;
            default -> null;
        };
    }
}