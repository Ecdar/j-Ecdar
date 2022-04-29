package models;

import java.util.Iterator;

public class ElemIterable implements Iterable<Segment> {
    private CDDNode node;

    public ElemIterable(CDDNode node) {
        this.node = node;
    }

    @Override
    public Iterator<Segment> iterator() {
        return new ElemIterator(node);
    }
}
