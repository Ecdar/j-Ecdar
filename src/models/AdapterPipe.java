package models;

public class AdapterPipe<I, K, O> implements Pipe<I, O> {
    private final Pipe<I, K> first;
    private final Pipe<K, O> last;

    public AdapterPipe(Pipe<I, K> first, Pipe<K, O> last) {
        this.first = first;
        this.last = last;
    }

    @Override
    public O process(I input) {
        return last.process(
                first.process(input)
        );
    }
}
