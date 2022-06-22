package exceptions;

public class ClockNotFoundException extends RuntimeException{
    public ClockNotFoundException(String message) {
        super(message);
    }
}
