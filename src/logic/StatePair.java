package logic;

public class StatePair {
    private final Refinement.State left, right;
    private GraphNode node;

    public StatePair(Refinement.State state1, Refinement.State state2) {
        this.left = state1;
        this.right = state2;
    }

    public StatePair(Refinement.State left, Refinement.State right, GraphNode node) {
        this.left = left;
        this.right = right;
        this.node = node;
    }

    public Refinement.State getLeft() {
        return left;
    }

    public Refinement.State getRight() {
        return right;
    }

    public GraphNode getNode() {
        return node;
    }

    public void setNode(GraphNode node) {
        this.node = node;
    }

    public String prettyPrint() {
        return "L=(" + left.getLocation() + ", " + right.getLocation() + ")  Z=" + left.getInvFed();
    }

    @Override
    public String toString() {
        //return "L=" + left + ", R=" + right;
        return "L=(" + left.getLocation() + ", " + right.getLocation() + ")  Z=" + left.getInvFed() + "  " + right.getInvFed();
    }
}