package Exceptions;

public class CddAlreadyRunningException extends RuntimeException {
    public CddAlreadyRunningException(String message) {
        super(message);
    }
}
