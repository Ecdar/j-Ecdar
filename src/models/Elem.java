package models;

public class Elem{
    private CDDNode child;
    private int bound;

    public Elem(CDDNode child, int bound) {
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
