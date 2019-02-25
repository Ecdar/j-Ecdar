package logic;

import lib.DBMLib;
import models.*;

import global.LibLoader;
import java.util.*;
import java.util.stream.Collectors;

// parent class for all TS's, so we can use it with regular TS's, composed TS's etc.
public abstract class TransitionSystem {
    final List<Clock> clocks;
    int dbmSize;

    TransitionSystem() {
        this.clocks = new ArrayList<>();

        LibLoader.load();
    }

    int getDbmSize() {
        return dbmSize;
    }

    public List<Clock> getClocks() {
        return clocks;
    }

    public State getInitialState() {
        int[] zone = initializeDBM();
        State state = new State(getInitialLocation(), zone);
        state.applyInvariants(clocks);

        return state;
    }

    protected abstract SymbolicLocation getInitialLocation();

    SymbolicLocation getInitialLocation(TransitionSystem[] systems) {
        // build ComplexLocation with initial location from each TransitionSystem
        return new ComplexLocation(Arrays.stream(systems).map(TransitionSystem::getInitialLocation).collect(Collectors.toList()));
    }

    List<Transition> createNewTransitions(State currentState, List<Move> moves) {
        List<Transition> transitions = new ArrayList<>();

        // loop through moves
        for (Move move : moves) {
            List<Edge> egdes = move.getEdges();

            // gather all the guards and resets of one move
            List<Guard> guards = egdes.stream().map(Edge::getGuards).flatMap(Arrays::stream).collect(Collectors.toList());
            List<Update> updates = egdes.stream().map(Edge::getUpdates).flatMap(Arrays::stream).collect(Collectors.toList());

            State state = new State(move.getTarget(), currentState.getZone());
            // get the new zone by applying guards and resets on the zone of the target state
            if (!guards.isEmpty()) state.applyGuards(guards, clocks);
            if (!updates.isEmpty()) state.applyResets(updates, clocks);

            // if the zone is valid, build the transition and add it to the list
            if (isDbmValid(state.getZone()))
                transitions.add(new Transition(currentState, state, move.getEdges()));
        }

        return transitions;
    }

    public abstract Set<Channel> getInputs();

    public abstract Set<Channel> getOutputs();

    public Set<Channel> getSyncs() {
        return new HashSet<>();
    }

    public Set<Channel> getActions() {
        Set<Channel> actions = new HashSet<>(getInputs());
        actions.addAll(getOutputs());
        actions.addAll(getSyncs());

        return actions;
    }

    public abstract List<Transition> getNextTransitions(State currentState, Channel channel);

    protected abstract List<Move> getNextMoves(SymbolicLocation location, Channel channel);

    private int[] initializeDBM() {
        // we need a DBM of size n*n, where n is the number of clocks (x0, x1, x2, ... , xn)
        // clocks x1 to xn are clocks derived from our automata, while x0 is a reference clock needed by the library
        // initially dbm is an array of 0's, which is what we need
        int[] dbm = new int[dbmSize * dbmSize];
        dbm = DBMLib.dbm_init(dbm, dbmSize);
        return dbm;
    }

    private boolean isDbmValid(int[] dbm) {
        return DBMLib.dbm_isValid(dbm, dbmSize);
    }

    List<Move> moveProduct(List<Move> moves1, List<Move> moves2, boolean toNest) {
        List<Move> moves = new ArrayList<>();

        for (Move move1 : moves1) {
            for (Move move2 : moves2) {

                SymbolicLocation source, target;

                if (toNest) {
                    source = new ComplexLocation(new ArrayList<>(Arrays.asList(move1.getSource(), move2.getSource())));
                    target = new ComplexLocation(new ArrayList<>(Arrays.asList(move1.getTarget(), move2.getTarget())));
                } else {
                    List<SymbolicLocation> newSourceLoc = new ArrayList<>(((ComplexLocation) move1.getSource()).getLocations());
                    newSourceLoc.add(move2.getSource());
                    source = new ComplexLocation(newSourceLoc);

                    List<SymbolicLocation> newTargetLoc = new ArrayList<>(((ComplexLocation) move1.getTarget()).getLocations());
                    newTargetLoc.add(move2.getTarget());
                    target = new ComplexLocation(newTargetLoc);
                }

                List<Edge> edges = new ArrayList<>(move1.getEdges());
                edges.addAll(move2.getEdges());

                Move newMove = new Move(source, target, edges);
                moves.add(newMove);
            }
        }

        return moves;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransitionSystem that = (TransitionSystem) o;
        return dbmSize == that.dbmSize &&
                clocks.equals(that.clocks);
    }
}