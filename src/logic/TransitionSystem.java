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

    public abstract Automaton getAutomaton();

    public List<Clock> getClocks() {
        return clocks;
    }

    public State getInitialState() {
        //System.out.println("clocks " + clocks);
        Zone zone = new Zone(clocks.size() + 1, true);
        List<Zone> zoneList = new ArrayList<>();
        zoneList.add(zone);
        Federation initFed = new Federation(zoneList);
        State state = new State(getInitialLocation(), initFed);
        state.applyInvariants(clocks);
        //System.out.println("Initial Zone size first " + state.getInvFed().size() + " " + initFed.size());
        return state;
    }

    public State getInitialStateRef(List<Clock> allClocks, List<List<Guard>> invs) {
        Zone zone = new Zone(allClocks.size() + 1, true);
        List<Zone> zoneList = new ArrayList<>();
        zoneList.add(zone);
        Federation initFed = new Federation(zoneList);
        State state = new State(getInitialLocation(), initFed);
        state.applyInvariants(allClocks);
        state.applyGuards(invs, allClocks);
        //System.out.println("Initial Zone size second" + initFed.size());

        return state;
    }

    protected abstract SymbolicLocation getInitialLocation();

    SymbolicLocation getInitialLocation(TransitionSystem[] systems) {
        // build ComplexLocation with initial location from each TransitionSystem
        return new ComplexLocation(Arrays.stream(systems).map(TransitionSystem::getInitialLocation).collect(Collectors.toList()));
    }

    List<Transition> createNewTransitions(State currentState, List<Move> moves, List<Clock> allClocks) {
        List<Transition> transitions = new ArrayList<>();
        //System.out.println(currentState + " " + moves.size());
        // loop through moves
        for (Move move : moves) {

            // gather all the guards and resets of one move
            List<List<Guard>> guards = move.getGuards();
            List<Update> updates = move.getUpdates();
            //System.out.println("reached createNewTransitions1");
            // TODO: just turned zones into feds, need to check whether there is some special behaviour.
            // need to make a copy of the zone. Arrival zone of target state is invalid right now

            State targetState = new State(move.getTarget(), currentState.getInvFed());
            //System.out.println("************************************************************************************************"+currentState.getInvFed().getZones().size());
           // currentState.getInvFed().getZones().get(0).printDBM(true,true);
            if (!guards.isEmpty()) targetState.applyGuards(guards, allClocks);
            //System.out.println("reached createNewTransitions8");
            //System.out.println(move.getGuards());
            //System.out.println(move.getEdges().get(0));
           // currentState.getInvFed().getZones().get(0).printDBM(true,true);
            //targetState.getInvFed().getZones().get(0).printDBM(true,true);
            if (!targetState.getInvFed().isValid()) continue;
            //System.out.println("reached createNewTransitions9");

            Federation guardFed = new Federation(targetState.getInvFed().getZones());
            //System.out.println(guardFed.getZones().size());
            if (!updates.isEmpty()) targetState.applyResets(updates, allClocks);
            //System.out.println("reached createNewTransitions2");
            //System.out.println(guardFed.getZones().size());
            targetState.getInvFed().delay();
            //System.out.println("reached createNewTransitions5");
            targetState.applyInvariants(allClocks);
            //System.out.println("reached createNewTransitions3");
            if (!targetState.getInvFed().isValid()) continue;
            //System.out.println("reached createNewTransitions4");

            assert(guardFed.getZones().size()!=0);
            transitions.add(new Transition(currentState, targetState, move, guardFed));
        }
        //System.out.println(transitions.size());
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
       // System.out.println("reached isdeterm1");
        boolean isDeterministic = true;
        List<String> nondetermTs = new ArrayList<>();

        List<SimpleTransitionSystem> systems = getSystems();

        //System.out.println("reached isdeterm2");
        for (SimpleTransitionSystem ts : systems){
            if(!ts.isDeterministicHelper()){
                isDeterministic = false;
                nondetermTs.add(ts.getName());
            }
        }

        //System.out.println("reached isdeterm3");
        if(!isDeterministic) buildErrMessage(nondetermTs, "non-deterministic");


        return isDeterministic;
    }

    public boolean isLeastConsistent(){
        //System.out.println("reached isleastcons1");
        return isConsistent(true);
    }

    public boolean isFullyConsistent(){
        return isConsistent(false);
    }

    private boolean isConsistent(boolean canPrune) {
        boolean isDeterm = isDeterministic();
        boolean isConsistent = true;
        List<String> inconsistentTs = new ArrayList<>();
        //System.out.println("reached cons 0");
        List<SimpleTransitionSystem> systems = getSystems();
        //System.out.println("reached cons 1");
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
        //System.out.println("reached moveProduct");
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