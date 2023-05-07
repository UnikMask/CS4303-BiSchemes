package bischemes.level.parts;

public enum RObjType {
    GEOMETRY,
    DOOR,
    LEVER,
    BLOCK,
    SPIKE,
    PORTAL,
    EXIT,
    CUSTOM;

    public static RObjType parse(String s) {
        return switch (s.toUpperCase()) {
            case "DOOR" -> DOOR;
            case "LEVER" -> LEVER;
            case "BLOCK" -> BLOCK;
            case "SPIKE" -> SPIKE;
            case "PORTAL" -> PORTAL;
            case "EXIT" -> EXIT;
            case "CUSTOM" -> CUSTOM;
            default -> null;
        };
    }

}
