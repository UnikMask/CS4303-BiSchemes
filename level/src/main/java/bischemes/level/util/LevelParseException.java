package bischemes.level.util;

public class LevelParseException extends RuntimeException{

    public LevelParseException(String errMessage) {
        super(errMessage);
    }

    public LevelParseException(String errMessage, Throwable err) {
        super(errMessage, err);
    }

}