package exceptions;

public class CddNotRunningException extends RuntimeException{
    public CddNotRunningException(String message) {
        super(message);
    }
}
