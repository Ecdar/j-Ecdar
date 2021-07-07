package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Transition {
    private State source, target;
    private final Move move;
    private Federation guardFederation;

    public Transition(State source, State target, Move move, int dim) {
        this.source = source;
        this.target = target;
        this.move = move;
        Zone emptyZone = new Zone(dim, true);
        ArrayList<Zone> zoneArrayList = new ArrayList<>();
        zoneArrayList.add(emptyZone);
        this.guardFederation = new Federation(zoneArrayList);
    }

    public Transition(State source, State target, Move move, Federation guardFederation) {
        this.source = source;
        this.target = target;
        this.move = move;
        this.guardFederation = guardFederation.getCopy();
    }

    // self loop
    public Transition(State state, int dim) {
        this(state, state, new Move(state.getLocation(), state.getLocation(), new ArrayList<>()), dim);
    }

    // self loop
    public Transition(State state, Federation guardFederation) {

        this(state, state, new Move(state.getLocation(), state.getLocation(), new ArrayList<>()), guardFederation.getCopy());



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

    public Federation getGuardFed() {
        return guardFederation;
    }

    public List<Edge> getEdges() {
        return move.getEdges();
    }

    public List<List<Guard>> getGuards() {
        return move.getGuards();
    }

    public List<Update> getUpdates() {
        return move.getUpdates();
    }
}