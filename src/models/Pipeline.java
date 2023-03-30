package models;

public class Pipeline<I, O> {
    private final Pipe<I, O> pipe;

    public Pipeline(Pipe<I, O> pipe) {
        this.pipe = pipe;
    }

    public <K> Pipeline<I, K> install(Pipe<O, K> installment) {
        return new Pipeline<>(
                new AdapterPipe<>(this.pipe, installment)
        );
    }
}
