package models;

import java.util.ArrayList;
import java.util.List;

public class Transition {
    private State source, target;
    private final Move move;
    private Zone guardZone;

    public Transition(State source, State target, Move move, int dim) {
        this.source = source;
        this.target = target;
        this.move = move;
        this.guardZone = new Zone(dim, true);
    }

    public Transition(State source, State target, Move move, Zone guardZone) {
        this.source = source;
        this.target = target;
        this.move = move;
        this.guardZone = guardZone;
    }

    // self loop
    public Transition(State state, int dim) {
        this(state, state, new Move(state.getLocation(), state.getLocation(), new ArrayList<>()), dim);
    }

    // self loop
    public Transition(State state, Zone guardZone) {
        this(state, state, new Move(state.getLocation(), state.getLocation(), new ArrayList<>()), new Zone(guardZone));
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

    public Zone getGuardZone() {
        return guardZone;
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