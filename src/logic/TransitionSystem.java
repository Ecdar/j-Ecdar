package logic;

import models.*;

import java.util.*;
import java.util.stream.Collectors;

// parent class for all TS's, so we can use it with regular TS's, composed TS's etc.
public abstract class TransitionSystem {
    final List<Clock> clocks;

    TransitionSystem() {
        this.clocks = new ArrayList<>();
    }

    public List<Clock> getClocks() {
        return clocks;
    }

    public State getInitialState() {
        Zone zone = new Zone(clocks.size() + 1);
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

            // gather all the guards and resets of one move
            List<Guard> guards = move.getGuards();
            List<Update> updates = move.getUpdates();

            // need to make a copy of the zone
            Zone copiedZone = new Zone(currentState.getZone());
            State targetState = new State(move.getTarget(), copiedZone);
            // get the new zone by applying guards and resets on the zone of the target state
            Zone absZone = targetState.getZone().getAbsoluteZone(guards, clocks);
            if (absZone.containsNegatives()) continue;

            if (!guards.isEmpty()) targetState.applyGuards(guards, clocks);
            if (!targetState.getZone().isValid()) continue;


            targetState.getZone().updateLowerBounds(currentState.getZone(), absZone.getRawRowMax());
            if (!targetState.getZone().isValid()) continue;

            if (!updates.isEmpty()) targetState.applyResets(updates, clocks);

            targetState.getZone().delay();

            targetState.applyInvariants(clocks);

            if (!targetState.getZone().isValid()) continue;

            transitions.add(new Transition(currentState, targetState, move.getEdges()));
        }

        return transitions;
    }

    public abstract Set<Channel> getInputs();

    public abstract Set<Channel> getOutputs();

    public abstract List<TransitionSystem> getSystems();

    public Set<Channel> getSyncs() {
        return new HashSet<>();
    }

    public Set<Channel> getActions() {
        Set<Channel> actions = new HashSet<>(getInputs());
        actions.addAll(getOutputs());
        actions.addAll(getSyncs());

        return actions;
    }

    public boolean isDeterministic(){
        List<TransitionSystem> systems = getSystems();

        for (TransitionSystem ts : systems){
            if(!ts.isDeterministic())
                return false;
        }
        return true;
    }

    public boolean isConsistent(){
        List<TransitionSystem> systems = getSystems();

        for (TransitionSystem ts : systems){
            if(!ts.isConsistent())
                return false;
        }
        return true;
    }

    public boolean isImplementation(){
        List<TransitionSystem> systems = getSystems();

        for (TransitionSystem ts : systems){
            if(!ts.isImplementation())
                return false;
        }
        return true;
    }

    public abstract List<Transition> getNextTransitions(State currentState, Channel channel);

    protected abstract List<Move> getNextMoves(SymbolicLocation location, Channel channel);

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
        return clocks.equals(that.clocks);
    }
}