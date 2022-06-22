package exceptions;

public class BooleanVariableNotFoundException extends RuntimeException{
    public BooleanVariableNotFoundException(String message) {
        super(message);
    }
}
