package models;

import java.util.Iterator;

public class SegementIterator implements Iterator<Segment> {

    private CDDNode node;
    private int index;

    public SegementIterator(CDDNode node) {
        this.node = node;
    }

    @Override
    public boolean hasNext() {
        boolean hasNext = !node.isEndOfSegments(index);
        return hasNext;
    }

    @Override
    public Segment next() {
        Segment segment = node.getSegmentAtIndex(index);
        index++;
        return segment;
    }
}

