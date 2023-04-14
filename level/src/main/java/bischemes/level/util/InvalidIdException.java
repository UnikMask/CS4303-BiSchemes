package bischemes.level.util;

public class InvalidIdException extends RuntimeException{

    public InvalidIdException(String errMessage) {
        super(errMessage);
    }

    public InvalidIdException(String errMessage, Throwable err) {
        super(errMessage, err);
    }

}
