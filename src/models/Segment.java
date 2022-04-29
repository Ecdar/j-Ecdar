package models;

public class Segment {
    private CDDNode child;
    private int bound;

    public Segment(CDDNode child, int bound) {
        this.child = child;
        this.bound = bound;
    }

    public CDDNode getChild() {
        return child;
    }

    public int getBound() {
        return bound;
    }
}
