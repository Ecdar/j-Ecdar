package models;

import java.util.Iterator;

public class SegmentIterator implements Iterator<Segment> {
    private final CDDNode node;
    private int index;

    public SegmentIterator(CDDNode node) {
        this.node = node;
    }

    @Override
    public boolean hasNext() {
        return !node.isEndOfSegments(index);
    }

    @Override
    public Segment next() {
        Segment segment = node.getSegmentAtIndex(index);
        index++;
        return segment;
    }
}

