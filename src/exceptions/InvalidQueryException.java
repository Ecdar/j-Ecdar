package exceptions;

public class InvalidQueryException extends Exception{
    public InvalidQueryException(String message) {
        super(message);
    }
}
