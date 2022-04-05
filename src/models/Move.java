package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Move {

    private final SymbolicLocation source, target;
    private final List<Edge> edges;
    private CDD guardCDD;
    private List<Update> updates;

    public Move(SymbolicLocation source, SymbolicLocation target, List<Edge> edges) {
        this.source = source;
        this.target = target;
        this.edges = edges;
        guardCDD= CDD.cddTrue();

        for (Edge e : edges)
        {
            guardCDD = guardCDD.conjunction(e.getGuardCDD());
        }

        this.updates = edges.isEmpty() ? new ArrayList<>() : edges.stream().map(Edge::getUpdates).flatMap(Arrays::stream).collect(Collectors.toList());
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


    public CDD getGuardCDD() {
        return (guardCDD);
    }
    public List<List<Guard>> getGuards() {
        return CDD.toGuards(guardCDD);
    }

    public void setGuards(CDD guardCDD) {
        this.guardCDD = guardCDD;
    }

    public List<Update> getUpdates() {
        return updates;
    }

    public void setUpdates(List<Update> updates) {
        this.updates = updates;
    }
}
