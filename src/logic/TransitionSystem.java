package logic;

import lib.DBMLib;
import models.*;

import java.io.File;
import java.util.*;

// parent class for all TS's, so we can use it with regular TS's, composed TS's etc.
public abstract class TransitionSystem {
    List<Clock> clocks;
    int dbmSize;

    TransitionSystem() {
        this.clocks = new ArrayList<>();

        String fileName = "src/" + System.mapLibraryName("DBM");
        File lib = new File(fileName);
        System.load(lib.getAbsolutePath());
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
        state.delay();

        return state;
    }

    public abstract SymbolicLocation getInitialLocation();

    public SymbolicLocation getInitialLocation(List<TransitionSystem> systems) {
        List<SymbolicLocation> locations = new ArrayList<>();

        for (TransitionSystem ts : systems) {
            locations.add(ts.getInitialLocation());
        }

        return new ComplexLocation(locations);
    }

    List<Transition> createNewTransitions(State currentState, List<Move> moves) {
        List<Transition> transitions = new ArrayList<>();

        // loop through moves
        for (Move move : moves) {
            List<Guard> guards = new ArrayList<>();
            List<Update> updates = new ArrayList<>();

            // gather all the guards and resets of one move
            for (Edge t : move.getEdges()) {
                if (t != null) {
                    guards.addAll(t.getGuards());
                    updates.addAll(t.getUpdates());
                }
            }

            State state = new State(move.getTarget(), currentState.getZone());
            // get the new zone by applying guards and resets on the zone of the target state
            if (!guards.isEmpty()) state.applyGuards(guards, clocks);
            if (!updates.isEmpty()) state.applyResets(updates, clocks);

            // if the zone is valid, build the transition and add it to the list
            if (isDbmValid(state.getZone())) {
                Transition transition = new Transition(currentState, state, move.getEdges());
                transitions.add(transition);
            }
        }

        return transitions;
    }

    public abstract Set<Channel> getInputs();

    public abstract Set<Channel> getOutputs();

    public Set<Channel> getSyncs() {
        return new HashSet<>();
    }

    public abstract List<Transition> getNextTransitions(State currentState, Channel channel);

    public abstract List<Move> getNextMoves(SymbolicLocation location, Channel channel);

    public List<Move> getNextMoves(SymbolicLocation symLocation, Channel channel, List<TransitionSystem> systems) {
        List<SymbolicLocation> symLocs = ((ComplexLocation) symLocation).getLocations();

        List<Move> resultMoves = systems.get(0).getNextMoves(symLocs.get(0), channel);
        // used when there are no moves for some TS
        if (resultMoves.isEmpty())
            resultMoves = new ArrayList<>(Collections.singletonList(new Move(symLocs.get(0), symLocs.get(0), new ArrayList<>())));

        for (int i = 1; i < systems.size(); i++) {
            List<Move> moves = systems.get(i).getNextMoves(symLocs.get(i), channel);

            if (moves.isEmpty())
                moves = new ArrayList<>(Collections.singletonList(new Move(symLocs.get(i), symLocs.get(i), new ArrayList<>())));

            resultMoves = moveProduct(resultMoves, moves, i == 1);
        }

        // if there are no actual moves, then return empty list
        Move move = resultMoves.get(0);
        if (move.getSource().equals(move.getTarget())) {
            return new ArrayList<>();
        }

        return resultMoves;
    }

    int[] initializeDBM() {
        // we need a DBM of size n*n, where n is the number of clocks (x0, x1, x2, ... , xn)
        // clocks x1 to xn are clocks derived from our automata, while x0 is a reference clock needed by the library
        // initially dbm is an array of 0's, which is what we need
        int[] dbm = new int[dbmSize * dbmSize];
        dbm = DBMLib.dbm_init(dbm, dbmSize);
        return dbm;
    }

    boolean isDbmValid(int[] dbm) {
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
                    List<SymbolicLocation> newSourceLoc = ((ComplexLocation) move1.getSource()).getLocations();
                    newSourceLoc.add(move2.getSource());
                    source = new ComplexLocation(newSourceLoc);

                    List<SymbolicLocation> newTargetLoc = ((ComplexLocation) move1.getTarget()).getLocations();
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
    public boolean equals (Object obj)
    {
        if (this==obj) return true;
        if (this == null) return false;
        if (this.getClass() != obj.getClass()) return false;
        TransitionSystem ts = (TransitionSystem) obj;

        return this.getOutputs().equals(ts.getOutputs()) &&
                this.getInputs().equals(ts.getInputs()) &&
                this.getClocks().equals(ts.getClocks()) &&
                this.getDbmSize() == ts.getDbmSize();
    }
}