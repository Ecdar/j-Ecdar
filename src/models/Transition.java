package models;

import java.util.ArrayList;
import java.util.List;

public class Transition {
    private State source, target;
    private final Move move;
    private Zone guardZone, timeline;

    public Transition(State source, State target, Move move, int dim) {
        this.source = source;
        this.target = target;
        this.move = move;
        this.guardZone = new Zone(dim, true);
        this.timeline = new Zone(2, true);
    }

    public Transition(State source, State target, Move move, Zone guardZone, Zone tline) {
        this.source = source;
        this.target = target;
        this.move = move;
        this.guardZone = guardZone;
        this.timeline = tline;
    }

    // self loop
    public Transition(State state, int dim) {
        this(state, state, new Move(state.getLocation(), state.getLocation(), new ArrayList<>()), dim);
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
        return move.getEdges();
    }

    public List<Guard> getGuards() {
        return move.getGuards();
    }

    public List<Update> getUpdates() {
        return move.getUpdates();
    }
}