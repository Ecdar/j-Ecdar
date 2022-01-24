package models;

import java.util.Iterator;

public class ElemIterable implements Iterable<Elem> {
    private CDDNode node;

    public ElemIterable(CDDNode node) {
        this.node = node;
    }

    @Override
    public Iterator<Elem> iterator() {
        return new ElemIterator(node);
    }
}
