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

    /**
     * This function prints the <code>StatePair</code> in a more readable manner compared to <code>toString</code>.
     * <p>
     *     The format of the function is <p>
     *         <code>(LEFT_LOCATION, RIGHT_LOCATION) [ EXPRESSION ]</code>
     *     </p>whereas the format of <code>toString</code> is <p>
     *         <code>L=(LEFT_LOCATION, RIGHT_LOCATION)  CDD= LEFT_INVARIANT RIGHT_INVARIANT</code>
     *     </p>
     * </p>
     * <p>
     *     This function prints the zone as an expression, not a <code>CDD</code> or <code>DBM</code>.
     * </p>
     * <p>
     *     Since <code>left.getInvariant()</code> is equal to <code>right.getInvariant()</code>,
     *     only one is printed.
     * </p>
     * @return String
     */
    public String prettyPrint() {
        return String.format("(%s %s) [ %s ]",
                left.getLocation(), right.getLocation(),
                left.getInvariant().getExpression().prettyPrint());
    }

    @Override
    public String toString() {
        return "L=(" + left.getLocation() + ", " + right.getLocation() + ")  CDDs=" + left.getInvariant()+  " " + right.getInvariant();
    }
}
