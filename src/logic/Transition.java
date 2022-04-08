package logic;

import models.*;

import java.util.ArrayList;
import java.util.List;

public  class Transition {
    private State source, target;
    private final Move move;
    private CDD guardCDD;

    public Transition(State source, State target, Move move, int dim) {
        this.source = source;
        this.target = target;
        this.move = move;
        this.guardCDD = move.getGuardCDD();
    }

    public Transition(State source, State target, Move move, CDD guardCDD) {
        this.source = source;
        this.target = target;
        this.move = move;
        this.guardCDD = new CDD(guardCDD.getPointer());
    }

    // self loop
    public Transition(State state, int dim) {
        this(state, state, new Move(state.getLocation(), state.getLocation(), new ArrayList<>()), dim);
    }

    // self loop
    public Transition(State state, CDD guardCDD) {

        this(state, state, new Move(state.getLocation(), state.getLocation(), new ArrayList<>()), new CDD(guardCDD.getPointer()));



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

    public CDD getGuardCDD() {
        return guardCDD;
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