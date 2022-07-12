package models;

import java.util.Iterator;

public class SegmentIterable implements Iterable<Segment> {
    private final CDDNode node;

    public SegmentIterable(CDDNode node) {
        this.node = node;
    }

    @Override
    public Iterator<Segment> iterator() {
        return new SegmentIterator(node);
    }
}
