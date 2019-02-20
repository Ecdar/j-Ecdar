package logic;

import models.Guard;
import models.Edge;

import java.util.List;
import java.util.stream.Collectors;

public class Transition {
    private State source, target;
    private List<Edge> edges;

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