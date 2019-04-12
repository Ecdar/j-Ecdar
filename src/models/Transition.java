package models;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Transition {
    private State source, target;
    private final List<Edge> edges;
    private Zone guardZone, timeline;

    public Transition(State source, State target, List<Edge> edges, int dim) {
        this.source = source;
        this.target = target;
        this.edges = edges;
        this.guardZone = new Zone(dim, true);
        this.timeline = new Zone(2, true);
    }

    public Transition(State source, State target, List<Edge> edges, Zone guardZone, Zone tline) {
        this.source = source;
        this.target = target;
        this.edges = edges;
        this.guardZone = guardZone;
        this.timeline = tline;
    }

    public State getSource() {
        return source;
    }

    public void setTarget(State target) {
        this.target = target;
    }

    public State getTarget() {
        return target;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public List<Guard> getGuards() {
        // collect guards from each Edge and flatten the list
        return edges.stream().map(Edge::getGuards).flatMap(List::stream).collect(Collectors.toList());
    }

    public List<Update> getUpdates() {
        return edges.stream().map(Edge::getUpdates).flatMap(Arrays::stream).collect(Collectors.toList());
    }
}