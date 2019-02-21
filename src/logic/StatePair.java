package logic;

class StatePair {
    private final State left, right;

    public StatePair(State state1, State state2) {
        this.left = state1;
        this.right = state2;
    }

    public State getLeft() {
        return left;
    }

    public State getRight() {
        return right;
    }
}
