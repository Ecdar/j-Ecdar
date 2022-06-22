package models;

public class Segment {
    private CDDNode child;
    private int upperBound;
    private boolean isUpperBoundIncluded;

    public Segment(CDDNode child, int rawUpperBound) {
        this.child = child;
        this.upperBound = rawUpperBound>>1;
        System.out.println("raw upper " + rawUpperBound + " " + " upper " + upperBound );
        isUpperBoundIncluded =    (rawUpperBound & 1)==0 ? false : true;
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
