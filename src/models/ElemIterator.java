package models;

import java.util.Iterator;

public class ElemIterator implements Iterator<Segment> {

    private CDDNode node;
    private int index;

    public ElemIterator(CDDNode node) {
        this.node = node;
    }

    @Override
    public boolean hasNext() {
        boolean hasNext = !node.isEndOfElems(index);
        return hasNext;
    }

    @Override
    public Segment next() {
        Segment segment = node.getElemAtIndex(index);
        index++;
        return segment;
    }
}

