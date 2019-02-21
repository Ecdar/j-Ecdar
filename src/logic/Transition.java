package logic;

import models.Guard;
import models.Edge;

import java.util.List;
import java.util.stream.Collectors;

class Transition {
    private final State source, target;
    private final List<Edge> edges;

    public Transition(State source, State target, List<Edge> edges) {
        this.source = source;
        this.target = target;
        this.edges = edges;
    }

    public State getSource() {
        return source;
    }

    public State getTarget() {
        return target;
    }

    public List<Guard> getGuards() {
        // collect guards from each Edge and flatten the list
        return edges.stream().map(Edge::getGuards).flatMap(List::stream).collect(Collectors.toList());
    }
}