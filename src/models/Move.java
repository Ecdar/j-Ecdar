package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Move {

    private final SymbolicLocation source, target;
    private final List<Edge> edges;
    private List<Guard> guards;
    private List<Update> updates;

    public Move(SymbolicLocation source, SymbolicLocation target, List<Edge> edges) {
        this.source = source;
        this.target = target;
        this.edges = edges;
        this.guards = edges.isEmpty() ? new ArrayList<>() : edges.stream().map(Edge::getGuards).flatMap(List::stream).collect(Collectors.toList());
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

    public List<Guard> getGuards() {
        return guards;
    }

    public void setGuards(List<Guard> guards) {
        this.guards = guards;
    }

    public List<Update> getUpdates() {
        return updates;
    }

    public void setUpdates(List<Update> updates) {
        this.updates = updates;
    }
}
