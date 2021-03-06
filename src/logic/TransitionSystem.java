package logic;

import models.*;

import java.util.*;
import java.util.stream.Collectors;

// parent class for all TS's, so we can use it with regular TS's, composed TS's etc.
public abstract class TransitionSystem {
    final List<Clock> clocks;
    private StringBuilder lastErr = new StringBuilder();

    TransitionSystem() {
        this.clocks = new ArrayList<>();
    }

    public List<Clock> getClocks() {
        return clocks;
    }

    public State getInitialState() {
        Zone zone = new Zone(clocks.size() + 1, true);
        State state = new State(getInitialLocation(), zone);
        state.applyInvariants(clocks);

        return state;
    }

    public State getInitialStateRef(List<Clock> allClocks, List<Guard> invs) {
        Zone zone = new Zone(allClocks.size() + 1, true);
        State state = new State(getInitialLocation(), zone);
        state.applyInvariants(allClocks);
        state.applyGuards(invs, allClocks);

        return state;
    }

    protected abstract SymbolicLocation getInitialLocation();

    SymbolicLocation getInitialLocation(TransitionSystem[] systems) {
        // build ComplexLocation with initial location from each TransitionSystem
        return new ComplexLocation(Arrays.stream(systems).map(TransitionSystem::getInitialLocation).collect(Collectors.toList()));
    }

    List<Transition> createNewTransitions(State currentState, List<Move> moves, List<Clock> allClocks) {
        List<Transition> transitions = new ArrayList<>();

        // loop through moves
        for (Move move : moves) {

            // gather all the guards and resets of one move
            List<Guard> guards = move.getGuards();
            List<Update> updates = move.getUpdates();

            // need to make a copy of the zone. Arrival zone of target state is invalid right now
            State targetState = new State(move.getTarget(), currentState.getInvZone());

            if (!guards.isEmpty()) targetState.applyGuards(guards, allClocks);
            if (!targetState.getInvZone().isValid()) continue;
            Zone guardZone = new Zone(targetState.getInvZone());
            if (!updates.isEmpty()) targetState.applyResets(updates, allClocks);

            targetState.getInvZone().delay();
            targetState.applyInvariants(allClocks);

            if (!targetState.getInvZone().isValid()) continue;

            transitions.add(new Transition(currentState, targetState, move, guardZone));
        }
        return transitions;
    }

    public abstract Set<Channel> getInputs();

    public abstract Set<Channel> getOutputs();

    public abstract List<SimpleTransitionSystem> getSystems();

    public Set<Channel> getSyncs() {
        return new HashSet<>();
    }

    public Set<Channel> getActions() {
        Set<Channel> actions = new HashSet<>(getInputs());
        actions.addAll(getOutputs());
        actions.addAll(getSyncs());

        return actions;
    }

    public String getLastErr() {
        return lastErr.toString();
    }

    public void clearLastErr() {
        lastErr = new StringBuilder();
    }


    public boolean isDeterministic(){
        boolean isDeterministic = true;
        List<String> nondetermTs = new ArrayList<>();

        List<SimpleTransitionSystem> systems = getSystems();

        for (SimpleTransitionSystem ts : systems){
            if(!ts.isDeterministicHelper()){
                isDeterministic = false;
                nondetermTs.add(ts.getName());
            }
        }
        if(!isDeterministic) buildErrMessage(nondetermTs, "non-deterministic");
        return isDeterministic;
    }

    public boolean isLeastConsistent(){
        return isConsistent(true);
    }

    public boolean isFullyConsistent(){
        return isConsistent(false);
    }

    private boolean isConsistent(boolean canPrune) {
        boolean isDeterm = isDeterministic();
        boolean isConsistent = true;
        List<String> inconsistentTs = new ArrayList<>();

        List<SimpleTransitionSystem> systems = getSystems();

        for (SimpleTransitionSystem ts : systems){
            if(!ts.isConsistentHelper(canPrune)) {
                isConsistent = false;
                inconsistentTs.add(ts.getName());
            }
        }

        if(!isConsistent) buildErrMessage(inconsistentTs, "inconsistent");
        return isConsistent && isDeterm;
    }

    public boolean isImplementation(){
        boolean isCons = isFullyConsistent();
        boolean isImpl = true;
        List<String> nonImpl = new ArrayList<>();
        List<SimpleTransitionSystem> systems = getSystems();

        for (SimpleTransitionSystem ts : systems){
            if(!ts.isImplementationHelper())
                isImpl = false;
            nonImpl.add(ts.getName());
        }
        if(!isImpl) buildErrMessage(nonImpl, "not output urgent");
        return isImpl && isCons;
    }

    public List<Integer> getMaxBounds(){
        List<SimpleTransitionSystem> systems = getSystems();
        List<Integer> res = new ArrayList<>();

        for (TransitionSystem ts : systems){
            res.addAll(ts.getMaxBounds());
        }
        return res;
    }

    public List<Transition> getNextTransitions(State currentState, Channel channel){
        return getNextTransitions(currentState, channel, clocks);
    }

    public abstract List<Transition> getNextTransitions(State currentState, Channel channel, List<Clock> allClocks);

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

    public void buildErrMessage(List<String> inc, String checkType) {
        if (inc.size() == 1) {
            lastErr.append("Automaton ");
            lastErr.append(inc.get(0));
            lastErr.append(" is ").append(checkType).append(".\n");
        } else {
            lastErr.append("Automata ");
            for (int i = 0; i < inc.size(); i++) {
                if (i == inc.size() - 1)
                    lastErr.append(inc.get(i));
                else {
                    lastErr.append(inc.get(i));
                    lastErr.append(", ");
                }
            }
            lastErr.append(" are ").append(checkType).append(".\n");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransitionSystem that = (TransitionSystem) o;
        return clocks.equals(that.clocks);
    }
}