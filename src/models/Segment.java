package models;

public class Segment {
    private final CDDNode child;
    private final int upperBound;
    private boolean isUpperBoundIncluded;

    public Segment(CDDNode child, int rawUpperBound) {
        this.child = child;
        this.upperBound = rawUpperBound >> 1;
        isUpperBoundIncluded = (rawUpperBound & 1) != 0;
    }

    public CDDNode getChild() {
        return child;
    }

    public int getUpperBound() {
        return upperBound;
    }

    public boolean isUpperBoundIncluded() {
        return isUpperBoundIncluded;
    }

    public void setUpperBoundIncluded(boolean upperBoundIncluded) {
        isUpperBoundIncluded = upperBoundIncluded;
    }
}
