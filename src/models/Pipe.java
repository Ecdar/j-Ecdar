package models;

public interface Pipe<I, O> {
    O process(I input);
}
