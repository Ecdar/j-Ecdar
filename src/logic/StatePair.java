package logic;

public class StatePair {
    private final State left, right;
    private GraphNode node;

    public StatePair(State state1, State state2) {
        this.left = state1;
        this.right = state2;
    }

    public StatePair(State left, State right, GraphNode node) {
        this.left = left;
        this.right = right;
        this.node = node;
    }

    public State getLeft() {
        return left;
    }

    public State getRight() {
        return right;
    }

    public GraphNode getNode() {
        return node;
    }

    public void setNode(GraphNode node) {
        this.node = node;
    }

    public String prettyPrint() {
        return "L=(" + left.getLocation() + ", " + right.getLocation() + ")  CDDs=" + left.getInvariant() + " " + right.getInvariant();
    }

    @Override
    public String toString() {
        //return "L=" + left + ", R=" + right;
        return "L=(" + left.getLocation() + ", " + right.getLocation() + ")  CDDs=" + left.getInvariant() + " " + right.getInvariant();
    }
}