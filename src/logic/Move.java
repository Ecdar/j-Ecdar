package logic;

import models.Edge;

import java.util.List;

public class Move {

    private SymbolicLocation source, target;
    private List<Edge> edges;

    public Move(SymbolicLocation source, SymbolicLocation target, List<Edge> edges) {
        this.source = source;
        this.target = target;
        this.edges = edges;
    }

    public SymbolicLocation getSource() {
        return source;
    }

    public SymbolicLocation getTarget() {
        return target;
    }

    public List<Edge> getEdges() {
        return edges;
    }
}
